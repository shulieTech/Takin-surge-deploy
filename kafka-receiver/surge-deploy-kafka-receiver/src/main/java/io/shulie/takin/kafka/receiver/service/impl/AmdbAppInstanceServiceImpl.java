package io.shulie.takin.kafka.receiver.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.shulie.takin.kafka.receiver.dao.amdb.AmdbAppInstanceMapper;
import io.shulie.takin.kafka.receiver.dto.amdb.AppInstanceExtDTO;
import io.shulie.takin.kafka.receiver.dto.amdb.InstanceModel;
import io.shulie.takin.kafka.receiver.dto.amdb.ModuleLoadDetailDTO;
import io.shulie.takin.kafka.receiver.entity.AmdbAgentConfig;
import io.shulie.takin.kafka.receiver.entity.AmdbApp;
import io.shulie.takin.kafka.receiver.entity.AmdbAppInstance;
import io.shulie.takin.kafka.receiver.entity.AmdbAppInstanceStatus;
import io.shulie.takin.kafka.receiver.service.IAmdbAgentConfigService;
import io.shulie.takin.kafka.receiver.service.IAmdbAppInstanceService;
import io.shulie.takin.kafka.receiver.service.IAmdbAppInstanceStatusService;
import io.shulie.takin.kafka.receiver.service.IAmdbAppService;
import io.shulie.takin.kafka.receiver.util.FlagUtil;
import io.shulie.takin.kafka.receiver.util.Md5Utils;
import io.shulie.takin.kafka.receiver.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-14
 */
@Service
public class AmdbAppInstanceServiceImpl extends ServiceImpl<AmdbAppInstanceMapper, AmdbAppInstance> implements IAmdbAppInstanceService, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(AmdbAppInstanceServiceImpl.class);
    private static final String INSTANCE_PATH = "/config/log/pradar/client/";

    Map<String, String> instancePathMap = new HashMap<>(100);

    @Value("${default.user.id: 1}")
    private String defaultUserId;
    @Value("${default.tenant.appKey: ed45ef6b-bf94-48fa-b0c0-15e0285365d2}")
    private String defaultTenantAppKey;
    @Value("${default.env.code: test}")
    private String defaultEnvCode;

    @Resource
    private IAmdbAppService iAmdbAppService;
    @Resource
    private IAmdbAppInstanceStatusService iAmdbAppInstanceStatusService;
    @Resource
    private IAmdbAgentConfigService iAmdbAgentConfigService;

    @Override
    public void dealPradarClientMessage(Map entityBody) {
        Object appName = entityBody.get("appName");
        Object agentId = entityBody.get("agentId");
        Object tenantAppKey = entityBody.get("tenantAppKey");
        Object envCode = entityBody.get("envCode");
        if (appName == null) {
            log.warn("接收到的节点信息应用名为空，数据出现问题，接收到的数据为数据为:{}", JSON.toJSONString(entityBody));
            return;
        }
        if (agentId == null) {
            log.warn("接收到的节点信息agentId为空，数据出现问题，接收到的数据为数据为:{}", JSON.toJSONString(entityBody));
            return;
        }

        String instancePath = INSTANCE_PATH + appName + "/" + agentId + "/" + tenantAppKey + "/" + envCode;

        String body = JSON.toJSONString(entityBody);
        InstanceModel instanceModel = JSON.parseObject(body, InstanceModel.class);
        String md5 = Md5Utils.md5(body);
        if (instancePathMap.containsKey(instancePath) && md5.equals(instancePathMap.get(instancePath))) {
            //如果应用节点信息没有任何更新，不重复处理本条数据
            updateTime(instanceModel);
            return;
        }
        process(instanceModel);
        instancePathMap.put(instancePath, md5);
    }

    public void process(InstanceModel instanceModel) {
        if (instanceModel == null) {
            return;
        }
        //租户参数设置默认值
        if (instanceModel.getUserId() == null) {
            instanceModel.setUserId(defaultUserId);
        }
        if (instanceModel.getTenantAppKey() == null) {
            instanceModel.setTenantAppKey(defaultTenantAppKey);
        }
        if (instanceModel.getEnvCode() == null) {
            instanceModel.setEnvCode(defaultEnvCode);
        }

        updateAppAndInstance(instanceModel);
        //配置DO更新
        updateAgentConfig(instanceModel);
    }

    public void updateAppAndInstance(InstanceModel instanceModel) {
        LocalDateTime curr = LocalDateTime.now();
        // 判断APP记录是否存在
        QueryWrapper<AmdbApp> appQueryWrapper = new QueryWrapper<>();
        appQueryWrapper.lambda().eq(AmdbApp::getAppName, instanceModel.getAppName());
        appQueryWrapper.lambda().eq(AmdbApp::getUserAppKey, instanceModel.getTenantAppKey());
        appQueryWrapper.lambda().eq(AmdbApp::getEnvCode, instanceModel.getEnvCode());
        List<AmdbApp> appList = iAmdbAppService.list(appQueryWrapper);
        AmdbApp amdbApp;
        if (CollectionUtil.isEmpty(appList)) {
            amdbApp = getTamdAppCreateModelByInstanceModel(instanceModel.getAppName(), instanceModel, curr);
            // insert,拿到返回ID
            iAmdbAppService.save(amdbApp);
        } else {
            amdbApp = getTamdAppUpdateModelByInstanceModel(instanceModel, appList.get(0), curr);
            // update
            iAmdbAppService.updateById(amdbApp);
        }
        //更新实例信息
        AmdbAppInstance appInstanceDO = queryOldInstance(instanceModel);
        if (appInstanceDO == null) {
            AmdbAppInstance amdbAppInstance = getTamdAppInstanceCreateModelByInstanceModel(amdbApp, instanceModel, curr);
            this.save(amdbAppInstance);
        } else {
            AmdbAppInstance amdbAppInstance = getTamdAppInstanceUpdateModelByInstanceModel(amdbApp, instanceModel, appInstanceDO, curr);
            this.updateById(amdbAppInstance);
        }

        // 处理agent状态映射关系，与 探针 status 处理一致
        dealWithProbeStatusModel(instanceModel);
        AmdbAppInstanceStatus instanceStatus = createInstanceStatus(instanceModel);
        QueryWrapper<AmdbAppInstanceStatus> appInstanceStatusQueryWrapper = new QueryWrapper<>();
        appInstanceStatusQueryWrapper.lambda().eq(AmdbAppInstanceStatus::getAppName, instanceStatus.getAppName());
        appInstanceStatusQueryWrapper.lambda().eq(AmdbAppInstanceStatus::getUserAppKey, instanceStatus.getUserAppKey());
        appInstanceStatusQueryWrapper.lambda().eq(AmdbAppInstanceStatus::getEnvCode, instanceStatus.getEnvCode());
        appInstanceStatusQueryWrapper.lambda().eq(AmdbAppInstanceStatus::getAgentId, instanceStatus.getAgentId());
        appInstanceStatusQueryWrapper.lambda().eq(AmdbAppInstanceStatus::getIp, instanceStatus.getIp());
        appInstanceStatusQueryWrapper.lambda().eq(AmdbAppInstanceStatus::getPid, instanceStatus.getPid());
        List<AmdbAppInstanceStatus> amdbAppInstanceStatuses = iAmdbAppInstanceStatusService.list(appInstanceStatusQueryWrapper);
        if (CollectionUtil.isNotEmpty(amdbAppInstanceStatuses)) {
            amdbAppInstanceStatuses.forEach(amdbAppInstanceStatus -> {
                amdbAppInstanceStatus.setUserId(instanceStatus.getUserId());
                amdbAppInstanceStatus.setAgentStatus(instanceStatus.getAgentStatus());
                amdbAppInstanceStatus.setAgentErrorCode(instanceStatus.getAgentErrorCode());
                amdbAppInstanceStatus.setAgentErrorMsg(instanceStatus.getAgentErrorMsg());
                amdbAppInstanceStatus.setGmtModify(LocalDateTime.now());
            });
            amdbAppInstanceStatuses.forEach(amdbAppInstanceStatus -> {
                iAmdbAppInstanceStatusService.updateById(amdbAppInstanceStatus);
            });
        } else {
            iAmdbAppInstanceStatusService.save(instanceStatus);
        }
    }

    private AmdbAppInstance queryOldInstance(InstanceModel instanceModel) {
        // 判断instance是否存在
        QueryWrapper<AmdbAppInstance> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AmdbAppInstance::getAppName, instanceModel.getAppName());
        queryWrapper.lambda().eq(AmdbAppInstance::getIp, instanceModel.getAddress());
        queryWrapper.lambda().eq(AmdbAppInstance::getAgentId, instanceModel.getAgentId());
        queryWrapper.lambda().eq(AmdbAppInstance::getUserAppKey, instanceModel.getTenantAppKey());
        queryWrapper.lambda().eq(AmdbAppInstance::getEnvCode, instanceModel.getEnvCode());
        List<AmdbAppInstance> appInstances = this.list(queryWrapper);
        if (CollectionUtil.isEmpty(appInstances)) {
            return null;
        }
        return appInstances.get(0);
    }

    /**
     * 创建APP对象
     *
     * @param appName
     * @param instanceModel
     * @param curr
     * @return
     */
    private AmdbApp getTamdAppCreateModelByInstanceModel(String appName, InstanceModel instanceModel, LocalDateTime curr) {
        AmdbApp tAmdbApp = new AmdbApp();
        tAmdbApp.setAppName(appName);
        if (instanceModel.getExt() == null || instanceModel.getExt().length() == 0) {
            instanceModel.setExt("{}");
        }
        Map<String, Object> ext = JSON.parseObject(instanceModel.getExt());
        ext.put("jars", instanceModel.getJars());
        tAmdbApp.setExt(JSON.toJSONString(ext));
        tAmdbApp.setCreator("");
        tAmdbApp.setCreatorName("");
        tAmdbApp.setModifier("");
        tAmdbApp.setModifierName("");
        tAmdbApp.setGmtCreate(curr);
        tAmdbApp.setGmtModify(curr);
        tAmdbApp.setUserAppKey(instanceModel.getTenantAppKey());
        tAmdbApp.setEnvCode(instanceModel.getEnvCode());
        tAmdbApp.setUserId(instanceModel.getUserId());
        return tAmdbApp;
    }

    /**
     * APP更新对象
     *
     * @param instanceModel
     * @param app
     * @param curr
     * @return
     */
    private AmdbApp getTamdAppUpdateModelByInstanceModel(InstanceModel instanceModel, AmdbApp app, LocalDateTime curr) {
        Map<String, Object> ext = JSON.parseObject(app.getExt() == null ? "{}" : app.getExt());
        if (ext == null) {
            ext = new HashMap<>();
        }
        ext.put("jars", instanceModel.getJars());
        app.setExt(JSON.toJSONString(ext));
        app.setGmtModify(curr);
        app.setUserAppKey(instanceModel.getTenantAppKey());
        app.setEnvCode(instanceModel.getEnvCode());
        app.setUserId(instanceModel.getUserId());
        return app;
    }

    /**
     * 实例创建对象
     *
     * @param amdbApp
     * @param instanceModel
     * @param curr
     * @return
     */
    private AmdbAppInstance getTamdAppInstanceCreateModelByInstanceModel(AmdbApp amdbApp, InstanceModel instanceModel, LocalDateTime curr) {
        AmdbAppInstance amdbAppInstance = new AmdbAppInstance();
        amdbAppInstance.setAppName(amdbApp.getAppName());
        amdbAppInstance.setAppId(amdbApp.getId());
        amdbAppInstance.setAgentId(instanceModel.getAgentId());
        amdbAppInstance.setIp(instanceModel.getAddress());
        amdbAppInstance.setPid(instanceModel.getPid());
        amdbAppInstance.setAgentVersion(instanceModel.getAgentVersion());
        amdbAppInstance.setMd5(instanceModel.getMd5());
        amdbAppInstance.setAgentLanguage(instanceModel.getAgentLanguage());

        //租户相关
        amdbAppInstance.setUserId(instanceModel.getUserId());
        amdbAppInstance.setUserAppKey(instanceModel.getTenantAppKey());
        amdbAppInstance.setEnvCode(instanceModel.getEnvCode());


        AppInstanceExtDTO ext = new AppInstanceExtDTO();
        Map<String, String> simulatorConfig = JSON.parseObject(instanceModel.getSimulatorFileConfigs(), new TypeReference<Map<String, String>>() {
        });
        ext.setSimulatorConfigs(simulatorConfig);
        ext.setModuleLoadResult(instanceModel.getModuleLoadResult());
        List<ModuleLoadDetailDTO> moduleLoadDetailDTOS = JSON.parseArray(instanceModel.getModuleLoadDetail(), ModuleLoadDetailDTO.class);
        ext.setModuleLoadDetail(moduleLoadDetailDTOS);
        ext.setErrorMsgInfos("{}");
        ext.setGcType(instanceModel.getGcType());
        ext.setHost(instanceModel.getHost());
        ext.setStartTime(instanceModel.getStartTime());
        ext.setJdkVersion(instanceModel.getJdkVersion());
        amdbAppInstance.setHostname(instanceModel.getHost());
        amdbAppInstance.setExt(JSON.toJSONString(ext));
        amdbAppInstance.setFlag(0);
        amdbAppInstance.setFlag(FlagUtil.setFlag(amdbAppInstance.getFlag(), 1, true));
        if (instanceModel.isStatus()) {
            amdbAppInstance.setFlag(FlagUtil.setFlag(amdbAppInstance.getFlag(), 2, true));
        } else {
            amdbAppInstance.setFlag(FlagUtil.setFlag(amdbAppInstance.getFlag(), 2, false));
        }
        amdbAppInstance.setCreator("");
        amdbAppInstance.setCreatorName("");
        amdbAppInstance.setModifier("");
        amdbAppInstance.setModifierName("");
        amdbAppInstance.setGmtCreate(curr);
        amdbAppInstance.setGmtModify(curr);
        return amdbAppInstance;
    }

    /**
     * 实例更新对象
     *
     * @param amdbApp
     * @param instanceModel
     * @param oldAmdbAppInstance
     * @param curr
     * @return
     */
    private AmdbAppInstance getTamdAppInstanceUpdateModelByInstanceModel(AmdbApp amdbApp, InstanceModel instanceModel, AmdbAppInstance oldAmdbAppInstance, LocalDateTime curr) {
        oldAmdbAppInstance.setAppName(amdbApp.getAppName());
        oldAmdbAppInstance.setAppId(amdbApp.getId());
        oldAmdbAppInstance.setAgentId(instanceModel.getAgentId());
        oldAmdbAppInstance.setIp(instanceModel.getAddress());
        oldAmdbAppInstance.setPid(instanceModel.getPid());
        oldAmdbAppInstance.setAgentVersion(instanceModel.getAgentVersion());
        oldAmdbAppInstance.setMd5(instanceModel.getMd5());
        oldAmdbAppInstance.setAgentLanguage(instanceModel.getAgentLanguage());
        oldAmdbAppInstance.setHostname(instanceModel.getHost());
        AppInstanceExtDTO ext = new AppInstanceExtDTO();
        Map<String, String> simulatorConfig = JSON.parseObject(instanceModel.getSimulatorFileConfigs(), new TypeReference<Map<String, String>>() {
        });
        ext.setSimulatorConfigs(simulatorConfig);
        ext.setModuleLoadResult(instanceModel.getModuleLoadResult());
        List<ModuleLoadDetailDTO> moduleLoadDetailDTOS = JSON.parseArray(instanceModel.getModuleLoadDetail(), ModuleLoadDetailDTO.class);
        ext.setModuleLoadDetail(moduleLoadDetailDTOS);
        ext.setErrorMsgInfos("{}");
        ext.setGcType(instanceModel.getGcType());
        ext.setHost(instanceModel.getHost());
        ext.setStartTime(instanceModel.getStartTime());
        ext.setJdkVersion(instanceModel.getJdkVersion());
        oldAmdbAppInstance.setExt(JSON.toJSONString(ext));
        // 改为在线状态
        oldAmdbAppInstance.setFlag(FlagUtil.setFlag(oldAmdbAppInstance.getFlag(), 1, true));
        // 设置Agent状态
        if (instanceModel.isStatus()) {
            // 设为正常状态
            oldAmdbAppInstance.setFlag(FlagUtil.setFlag(oldAmdbAppInstance.getFlag(), 2, true));
        } else {
            // 设置为异常状态
            oldAmdbAppInstance.setFlag(FlagUtil.setFlag(oldAmdbAppInstance.getFlag(), 2, false));
        }
        oldAmdbAppInstance.setGmtModify(curr);
        return oldAmdbAppInstance;
    }

    private void updateTime(InstanceModel instanceModel) {
        String agentId = instanceModel.getAgentId();
        String envCode = instanceModel.getEnvCode();
        String appName = instanceModel.getAppName();
        String address = instanceModel.getAddress();
        String pid = instanceModel.getPid();
        String tenantAppKey = instanceModel.getTenantAppKey();

        QueryWrapper<AmdbAppInstance> appInstanceQueryWrapper = new QueryWrapper<>();
        appInstanceQueryWrapper.lambda().eq(AmdbAppInstance::getAppName, appName);
        appInstanceQueryWrapper.lambda().eq(AmdbAppInstance::getIp, address);
        appInstanceQueryWrapper.lambda().eq(AmdbAppInstance::getPid, pid);
        List<AmdbAppInstance> appInstances = this.list(appInstanceQueryWrapper);
        if (CollectionUtil.isNotEmpty(appInstances)) {
            appInstances.forEach(amdbAppInstance -> {
                amdbAppInstance.setGmtModify(LocalDateTime.now());
            });
            appInstances.forEach(this::updateById);
        }

        QueryWrapper<AmdbAppInstanceStatus> appInstanceStatusQueryWrapper = new QueryWrapper<>();
        appInstanceStatusQueryWrapper.lambda().eq(AmdbAppInstanceStatus::getAppName, appName);
        appInstanceStatusQueryWrapper.lambda().eq(AmdbAppInstanceStatus::getAgentId, agentId);
        appInstanceStatusQueryWrapper.lambda().eq(AmdbAppInstanceStatus::getEnvCode, envCode);
        appInstanceStatusQueryWrapper.lambda().eq(AmdbAppInstanceStatus::getUserAppKey, tenantAppKey);
        List<AmdbAppInstanceStatus> appInstanceStatuses = iAmdbAppInstanceStatusService.list(appInstanceStatusQueryWrapper);
        if (CollectionUtil.isNotEmpty(appInstanceStatuses)) {
            appInstanceStatuses.forEach(appInstanceStatus -> {
                appInstanceStatus.setGmtModify(LocalDateTime.now());
                iAmdbAppInstanceStatusService.updateById(appInstanceStatus);
            });


        }

        QueryWrapper<AmdbAgentConfig> agentConfigQueryWrapper = new QueryWrapper<>();
        agentConfigQueryWrapper.lambda().eq(AmdbAgentConfig::getAgentId, agentId);
        agentConfigQueryWrapper.lambda().eq(AmdbAgentConfig::getAppName, appName);
        List<AmdbAgentConfig> agentConfigs = iAmdbAgentConfigService.list(agentConfigQueryWrapper);
        if (CollectionUtil.isNotEmpty(agentConfigs)) {
            agentConfigs.forEach(config -> {
                config.setGmtCreate(LocalDateTime.now());
            });
            agentConfigs.forEach(amdbAgentConfig -> iAmdbAgentConfigService.updateById(amdbAgentConfig));
        }
    }

    private void batchOffline() {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.MINUTE, -3);

        QueryWrapper<AmdbAppInstance> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().lt(AmdbAppInstance::getGmtModify, instance.getTime());
        queryWrapper.comment("(flag&1)=1");
        List<AmdbAppInstance> list = this.list(queryWrapper);
        if (CollectionUtil.isNotEmpty(list)) {
            list.forEach(amdbAppInstance -> {
                amdbAppInstance.setFlag(FlagUtil.setFlag(amdbAppInstance.getFlag(), 1, false));
            });
            list.forEach(this::updateById);

        }

        QueryWrapper<AmdbAgentConfig> agentConfigQueryWrapper = new QueryWrapper<>();
        agentConfigQueryWrapper.lambda().lt(AmdbAgentConfig::getGmtCreate, instance.getTime());
        List<AmdbAgentConfig> agentConfigs = iAmdbAgentConfigService.list(agentConfigQueryWrapper);
        if (CollectionUtil.isNotEmpty(agentConfigs)) {
            List<Long> ids = agentConfigs.stream().map(AmdbAgentConfig::getId).collect(Collectors.toList());
            iAmdbAgentConfigService.removeByIds(ids);
        }
        if (CollectionUtil.isNotEmpty(list) || CollectionUtil.isNotEmpty(agentConfigs)) {
            instancePathMap.clear();
        }
    }


    private void dealWithProbeStatusModel(InstanceModel instanceModel) {
        //探针状态转换
        if (instanceModel != null) {
            if (instanceModel.getSimulatorVersion() == null) {
                log.info("探针版本为null");
                instanceModel.setSimulatorVersion("未知版本");
            }
            switch (StringUtil.parseStr(instanceModel.getAgentStatus())) {
                case "INSTALLED":
                    instanceModel.setAgentStatus("0");
                    break;
                case "UNINSTALL":
                    instanceModel.setAgentStatus("1");
                    break;
                case "INSTALLING":
                    instanceModel.setAgentStatus("2");
                    break;
                case "UNINSTALLING":
                    instanceModel.setAgentStatus("3");
                    break;
                case "INSTALL_FAILED":
                    instanceModel.setAgentStatus("4");
                    break;
                case "UNINSTALL_FAILED":
                    instanceModel.setAgentStatus("5");
                    break;
                default:
                    log.info("agent未知状态:{}", StringUtil.parseStr(instanceModel.getAgentStatus()));
                    instanceModel.setAgentStatus("99");
            }
        }
    }

    // 这里应该设置 探针 的状态、错误码、错误信息
    private AmdbAppInstanceStatus createInstanceStatus(InstanceModel instanceModel) {
        AmdbAppInstanceStatus instanceStatus = new AmdbAppInstanceStatus();
        instanceStatus.setAppName(instanceModel.getAppName());
        instanceStatus.setIp(instanceModel.getAddress());
        instanceStatus.setAgentId(instanceModel.getAgentId());
        instanceStatus.setPid(instanceModel.getPid());
        instanceStatus.setAgentErrorCode(instanceModel.getErrorCode());
        instanceStatus.setAgentErrorMsg(instanceModel.getErrorMsg());
        instanceStatus.setAgentStatus(instanceModel.getAgentStatus());
        instanceStatus.setEnvCode(instanceModel.getEnvCode());
        instanceStatus.setUserAppKey(instanceModel.getTenantAppKey());
        instanceStatus.setUserId(instanceModel.getUserId());
        return instanceStatus;
    }

    public void updateAgentConfig(InstanceModel instanceModel) {
        removeConfig(instanceModel.getAppName(), instanceModel.getAgentId());

        List<AmdbAgentConfig> agentConfigs = buildAgentConfig(instanceModel);

        if (agentConfigs != null && !agentConfigs.isEmpty()) {
            agentConfigs.forEach(amdbAgentConfig -> iAmdbAgentConfigService.save(amdbAgentConfig));
        }
    }


    private void removeConfig(String appName, String agentId) {
        Objects.requireNonNull(appName);
        Objects.requireNonNull(agentId);
        QueryWrapper<AmdbAgentConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AmdbAgentConfig::getAppName, appName);
        queryWrapper.lambda().eq(AmdbAgentConfig::getAgentId, agentId);
        List<AmdbAgentConfig> list = iAmdbAgentConfigService.list(queryWrapper);
        if (CollectionUtil.isNotEmpty(list)) {
            List<Long> ids = list.stream().map(AmdbAgentConfig::getId).collect(Collectors.toList());
            iAmdbAgentConfigService.removeByIds(ids);
        }
    }

    private List<AmdbAgentConfig> buildAgentConfig(InstanceModel instanceModel) {
        String agentAndSimulatorFileConfigsCheck = instanceModel.getSimulatorFileConfigsCheck();
        if (StringUtils.isBlank(agentAndSimulatorFileConfigsCheck)) {
            return null;
        }
        // 配置生效状态
        Map<String, String> configCheck = JSON.parseObject(agentAndSimulatorFileConfigsCheck, new TypeReference<Map<String, String>>() {
        });
        List<AmdbAgentConfig> ret = new ArrayList<>();
        String agentId = instanceModel.getAgentId();

        // agent配置项
        String agentConfig = instanceModel.getAgentFileConfigs();
        if (StringUtils.isNotBlank(agentConfig)) {
            Map<String, String> configKeyValues = JSON.parseObject(agentConfig, new TypeReference<Map<String, String>>() {
            });
            configKeyValues.forEach((configKey, configValue) -> {
                AmdbAgentConfig configDO = new AmdbAgentConfig();
                configDO.setAgentId(agentId);
                configDO.setAppName(instanceModel.getAppName());
                configDO.setConfigKey(configKey);
                configDO.setConfigValue(configValue);
                String status = configCheck.get(configKey);
                if (status == null) {
                    status = configCheck.get("status");
                }
                configDO.setStatus(Boolean.parseBoolean(status));
                configDO.setUserAppKey(instanceModel.getTenantAppKey());
                configDO.setEnvCode(instanceModel.getEnvCode());
                configDO.setUserId(instanceModel.getUserId());
                ret.add(configDO);
            });
        }
        // 探针配置项
        String simulatorConfigs = instanceModel.getSimulatorFileConfigs();
        if (StringUtils.isNotBlank(simulatorConfigs)) {
            Map<String, String> simulatorConfigsKeyValues = JSON.parseObject(simulatorConfigs, new TypeReference<Map<String, String>>() {
            });
            simulatorConfigsKeyValues.forEach((configKey, configValue) -> {
                AmdbAgentConfig configDO = new AmdbAgentConfig();
                configDO.setAgentId(agentId);
                configDO.setAppName(instanceModel.getAppName());
                configDO.setConfigKey(configKey);
                configDO.setConfigValue(configValue);
                String status = configCheck.get(configKey);
                if (status == null) {
                    status = configCheck.get("status");
                }
                configDO.setStatus(Boolean.parseBoolean(status));
                configDO.setUserAppKey(instanceModel.getTenantAppKey());
                configDO.setEnvCode(instanceModel.getEnvCode());
                configDO.setUserId(instanceModel.getUserId());
                ret.add(configDO);
            });
        }
        return ret;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate((new Runnable() {
            @Override
            public void run() {
                try {
                    batchOffline();
                } catch (Exception e) {
                    log.error("client下线节点出现异常", e);
                }
            }
        }), 20, 60, TimeUnit.SECONDS);
    }
}
