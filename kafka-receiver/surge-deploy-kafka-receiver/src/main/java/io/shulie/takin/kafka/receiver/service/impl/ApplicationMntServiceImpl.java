package io.shulie.takin.kafka.receiver.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.shaded.com.google.gson.Gson;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.shulie.takin.kafka.receiver.constant.web.AppSwitchEnum;
import io.shulie.takin.kafka.receiver.dto.web.ApplicationVo;
import io.shulie.takin.kafka.receiver.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receiver.entity.*;
import io.shulie.takin.kafka.receiver.dao.web.ApplicationMntMapper;
import io.shulie.takin.kafka.receiver.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * <p>
 * 应用管理表 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Service
public class ApplicationMntServiceImpl extends ServiceImpl<ApplicationMntMapper, ApplicationMnt> implements IApplicationMntService, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(ApplicationMntServiceImpl.class);

    private final String AGENT_HTTP_UPDATE_VERSION = "agent.http.update.version";

    @Resource
    private IConfigServerService iConfigServerService;
    @Resource
    private IDataBuildService iDataBuildService;
    @Resource
    private ILinkDetectionService iLinkDetectionService;
    @Resource
    private IApplicationPluginsConfigService iApplicationPluginsConfigService;

    private ConfigService configService;
    @Value("${node.num:1}")
    private String nodeNum;

    @Value("${nacos.server: 192.168.1.99:8848}")
    private String nacosServer;
    @Value("${nacos.namespace:}")
    private String nacosNamespace;
    @Value("${nacos.username:}")
    private String nacosUsername;
    @Value("${nacos.password:}")
    private String nacosPassword;

    private Snowflake snowflake;

    @Override
    public void dealAgentVersionMessage(String appName, String agentVersion, String pradarVersion, TenantCommonExt dealHeader) {
        if (StringUtils.isBlank(agentVersion)
                || StringUtils.isBlank(pradarVersion)
                || "null".equalsIgnoreCase(agentVersion)
                || "null".equalsIgnoreCase(pradarVersion)) {
            log.warn("更新应用版本异常,参数为空");
            return;
        }

        ConfigServer configServer = iConfigServerService.queryByKey(AGENT_HTTP_UPDATE_VERSION);
        if (configServer != null && "false".equals(configServer.getValue())) {
            return;
        }

        ApplicationMnt application = this.getApplicationByTenantIdAndName(appName, dealHeader);
        if (application == null || application.getApplicationId() == null) {
            log.warn("应用查询异常，未查到, updateAppAgentVersion fail!");
            return;
        }
        application.setPradarVersion(pradarVersion);
        application.setAgentVersion(agentVersion);
        application.setUpdateTime(LocalDateTime.now());
        this.updateById(application);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void dealAddApplicationMessage(String message, TenantCommonExt dealHeader) {
        if (StringUtils.isBlank(message)) {
            return;
        }
        ApplicationVo param = JSONObject.parseObject(message, ApplicationVo.class);
        if (param == null || StringUtils.isBlank(param.getApplicationName())) {
            log.warn("新增应用入参为空:{}", message);
            return;
        }

        if (StringUtils.isEmpty(param.getSwitchStutus())) {
            param.setSwitchStutus(AppSwitchEnum.OPENED.getCode());
        }
        if (param.getNodeNum() == null) {
            param.setNodeNum(1);
        }
        ApplicationMnt applicationMnt = this.voToAppEntity(param);

        ApplicationMnt application = this.getApplicationByTenantIdAndName(applicationMnt.getApplicationName(), dealHeader);
        if (application != null) {
            return;
        }

        addApplication(applicationMnt, dealHeader);
        addApplicationToDataBuild(applicationMnt);
        addApplicationToLinkDetection(applicationMnt);
        //应用自动上报需要设置插件管理的redis影子key默认值
        addPluginsConfig(applicationMnt);

        Map<String, Object> configs = new HashMap<>();
        configs.put("datasource", null);
        configs.put("job", null);
        configs.put("mq", null);
        configs.put("whitelist", null);
        configs.put("hbase", null);
        configs.put("redis", null);
        configs.put("es", null);
        configs.put("mock", null);
        configs.put("trace_rule", new HashMap<>());
        configs.put("dynamic_config", new HashMap<>());

        try {
            boolean success = configService.publishConfig(param.getApplicationName(), "APP", new Gson().toJson(configs));
            if (!success) {
                log.error(param.getApplicationName() + "推送nacos失败");
                throw new RuntimeException(param.getApplicationName() + "推送nacos失败");
            }
        } catch (NacosException e) {
            log.error("推送nacos出现异常", e);
            throw new RuntimeException(param.getApplicationName() + "推送nacos出现异常" + e.getErrMsg());
        }
    }

    @Override
    public ApplicationMnt getApplicationByTenantIdAndName(String appName, TenantCommonExt dealHeader) {
        QueryWrapper<ApplicationMnt> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApplicationMnt::getApplicationName, appName);
        queryWrapper.lambda().eq(ApplicationMnt::getEnvCode, dealHeader.getEnvCode());
        queryWrapper.lambda().eq(ApplicationMnt::getTenantId, dealHeader.getTenantId());
        List<ApplicationMnt> applicationMntList = this.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(applicationMntList)) {
            return applicationMntList.get(0);
        }
        return null;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void addPluginsConfig(ApplicationMnt applicationMnt) {
        ApplicationPluginsConfig applicationPluginsConfig = new ApplicationPluginsConfig();
        applicationPluginsConfig.setApplicationId(applicationMnt.getApplicationId());
        applicationPluginsConfig.setApplicationName(applicationMnt.getApplicationName());
        applicationPluginsConfig.setConfigItem("redis影子key有效期");
        applicationPluginsConfig.setConfigKey("redis_expire");
        applicationPluginsConfig.setConfigDesc("可自定义设置redis影子key有效期，默认与业务key有效期一致。若设置时间比业务key有效期长，不生效，仍以业务key有效期为准。");
        applicationPluginsConfig.setConfigValue("-1");
        applicationPluginsConfig.setUserId(applicationMnt.getUserId());
        applicationPluginsConfig.setIsDeleted(false);
        applicationPluginsConfig.setCreateTime(LocalDateTime.now());
        applicationPluginsConfig.setModifieTime(LocalDateTime.now());
        applicationPluginsConfig.setCreatorId(applicationMnt.getUserId());
        applicationPluginsConfig.setModifierId(applicationMnt.getUserId());
        applicationPluginsConfig.setEnvCode(applicationMnt.getEnvCode());
        applicationPluginsConfig.setTenantId(applicationMnt.getTenantId());
        iApplicationPluginsConfigService.save(applicationPluginsConfig);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void addApplicationToLinkDetection(ApplicationMnt tApplicationMnt) {

        LinkDetection linkDetection = new LinkDetection();
        linkDetection.setLinkDetectionId(snowflake.next());
        linkDetection.setApplicationId(tApplicationMnt.getApplicationId());
        linkDetection.setCreateTime(LocalDateTime.now());
        linkDetection.setUpdateTime(LocalDateTime.now());
        linkDetection.setEnvCode(tApplicationMnt.getEnvCode());
        linkDetection.setTenantId(tApplicationMnt.getTenantId());
        iLinkDetectionService.save(linkDetection);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void addApplicationToDataBuild(ApplicationMnt tApplicationMnt) {
        DataBuild dataBuild = new DataBuild();
        dataBuild.setDataBuildId(snowflake.next());
        dataBuild.setApplicationId(tApplicationMnt.getApplicationId());

        if (tApplicationMnt.getCacheExpTime() == 0) {
            dataBuild.setCacheBuildStatus(2);
        }
        dataBuild.setCacheLastSuccessTime(LocalDateTime.now());
        dataBuild.setCreateTime(LocalDateTime.now());
        dataBuild.setUpdateTime(LocalDateTime.now());
        dataBuild.setEnvCode(tApplicationMnt.getEnvCode());
        dataBuild.setTenantId(tApplicationMnt.getTenantId());
        iDataBuildService.save(dataBuild);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void addApplication(ApplicationMnt tApplicationMnt, TenantCommonExt headers) {
        tApplicationMnt.setApplicationId(snowflake.next());
        tApplicationMnt.setCacheExpTime(tApplicationMnt.getCacheExpTime() == null ? 0L : tApplicationMnt.getCacheExpTime());
        tApplicationMnt.setUserId(headers.getUserId());
        tApplicationMnt.setDeptId(headers.getDeptId());
        tApplicationMnt.setTenantId(headers.getTenantId());
        tApplicationMnt.setEnvCode(headers.getEnvCode());
        this.save(tApplicationMnt);

    }

    private ApplicationMnt voToAppEntity(ApplicationVo param) {
        ApplicationMnt dbData = new ApplicationMnt();
        if (StringUtils.isNotEmpty(param.getId())) {
            dbData.setApplicationId(Long.parseLong(param.getId()));
        }
        dbData.setApplicationName(param.getApplicationName());
        dbData.setApplicationDesc(param.getApplicationDesc());
        dbData.setBasicScriptPath(param.getBasicScriptPath());
        dbData.setCacheScriptPath(param.getCacheScriptPath());
        dbData.setCleanScriptPath(param.getCleanScriptPath());
        dbData.setDdlScriptPath(param.getDdlScriptPath());
        dbData.setReadyScriptPath(param.getReadyScriptPath());
        dbData.setNodeNum(param.getNodeNum());
        dbData.setAccessStatus(param.getAccessStatus());
        dbData.setExceptionInfo(param.getExceptionInfo());
        dbData.setSwitchStatus(param.getSwitchStutus());
        dbData.setClusterName(param.getClusterName());
        if (param.getAccessStatus() == null) {
            dbData.setAccessStatus(1);
        } else {
            dbData.setAccessStatus(param.getAccessStatus());
        }
        return dbData;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        snowflake = new Snowflake(Integer.parseInt(nodeNum));

        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, nacosServer);
        if (StringUtils.isNotBlank(nacosNamespace)) {
            properties.put(PropertyKeyConst.NAMESPACE, nacosNamespace);
        }
        if (StringUtils.isNotBlank(nacosUsername)) {
            properties.put(PropertyKeyConst.USERNAME, nacosUsername);
        }
        if (StringUtils.isNotBlank(nacosPassword)) {
            properties.put(PropertyKeyConst.PASSWORD, nacosPassword);
        }
        configService = ConfigFactory.createConfigService(properties);
    }
}
