package io.shulie.takin.kafka.receiver.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.shulie.takin.kafka.receiver.dao.amdb.AmdbAppInstanceStatusMapper;
import io.shulie.takin.kafka.receiver.dto.amdb.InstanceStatusModel;
import io.shulie.takin.kafka.receiver.entity.AmdbAppInstanceStatus;
import io.shulie.takin.kafka.receiver.service.IAmdbAppInstanceStatusService;
import io.shulie.takin.kafka.receiver.util.Md5Utils;
import io.shulie.takin.kafka.receiver.util.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 应用实例探针状态表 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-14
 */
@Service
public class AmdbAppInstanceStatusServiceImpl extends ServiceImpl<AmdbAppInstanceStatusMapper, AmdbAppInstanceStatus> implements IAmdbAppInstanceStatusService, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(AmdbAppInstanceStatusServiceImpl.class);
    private static final String INSTANCE_STATUS_PATH = "/config/log/pradar/status/";
    Map<String, String> instancePathMap = new HashMap<>(100);

    @Value("${default.user.id: 1}")
    private String defaultUserId;
    @Value("${default.tenant.appKey: ed45ef6b-bf94-48fa-b0c0-15e0285365d2}")
    private String defaultTenantAppKey;
    @Value("${default.env.code: test}")
    private String defaultEnvCode;


    @Override
    public void dealPradarStatusMessage(Map entityBody) {
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
        String instancePath = INSTANCE_STATUS_PATH + appName + "/" + agentId + "/" + tenantAppKey + "/" + envCode;

        String body = JSON.toJSONString(entityBody);
        InstanceStatusModel instanceStatusModel = JSON.parseObject(body, InstanceStatusModel.class);
        String md5 = Md5Utils.md5(body);
        if (instancePathMap.containsKey(instancePath) && md5.equals(instancePathMap.get(instancePath))) {
            //如果应用节点信息没有任何更新，不重复处理本条数据
            this.updateTime(instanceStatusModel);
            return;
        }
        this.process(instanceStatusModel);
        instancePathMap.put(instancePath, md5);
    }


    private void updateTime(InstanceStatusModel model) {
        QueryWrapper<AmdbAppInstanceStatus> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AmdbAppInstanceStatus::getAppName, model.getAppName());
        queryWrapper.lambda().eq(AmdbAppInstanceStatus::getIp, model.getAddress());
        queryWrapper.lambda().eq(AmdbAppInstanceStatus::getPid, model.getPid());
        queryWrapper.lambda().eq(AmdbAppInstanceStatus::getUserAppKey, model.getTenantAppKey());
        queryWrapper.lambda().eq(AmdbAppInstanceStatus::getEnvCode, model.getEnvCode());
        List<AmdbAppInstanceStatus> appInstanceStatuses = this.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(appInstanceStatuses)) {
            appInstanceStatuses.forEach(amdbAppInstanceStatus -> {
                amdbAppInstanceStatus.setGmtModify(LocalDateTime.now());
            });
            this.updateBatchById(appInstanceStatuses);
        }
    }

    public void process(InstanceStatusModel instanceStatusModel) {
        if (instanceStatusModel == null) {
            return;
        }
        //租户参数设置默认值
        if (instanceStatusModel.getUserId() == null) {
            instanceStatusModel.setUserId(defaultUserId);
        }
        if (instanceStatusModel.getTenantAppKey() == null) {
            instanceStatusModel.setTenantAppKey(defaultTenantAppKey);
        }
        if (instanceStatusModel.getEnvCode() == null) {
            instanceStatusModel.setEnvCode(defaultEnvCode);
        }

        updateOrInsertInstanceStatus(instanceStatusModel);
    }

    public void updateOrInsertInstanceStatus(InstanceStatusModel instanceStatusModel) {
        QueryWrapper<AmdbAppInstanceStatus> appInstanceStatusQueryWrapper = new QueryWrapper<>();
        appInstanceStatusQueryWrapper.lambda().eq(AmdbAppInstanceStatus::getAppName, instanceStatusModel.getAppName());
        appInstanceStatusQueryWrapper.lambda().eq(AmdbAppInstanceStatus::getIp, instanceStatusModel.getAddress());
        appInstanceStatusQueryWrapper.lambda().eq(AmdbAppInstanceStatus::getAgentId, instanceStatusModel.getAgentId());
        List<AmdbAppInstanceStatus> appInstanceStatuses = this.list(appInstanceStatusQueryWrapper);
        LocalDateTime curr = LocalDateTime.now();
        if (CollectionUtils.isEmpty(appInstanceStatuses)) {
            AmdbAppInstanceStatus instanceStatus = getInsertInstanceStatusModel(instanceStatusModel, curr);
            this.save(instanceStatus);
        } else {
            AmdbAppInstanceStatus instanceStatus = getUpdateInstanceStatusModel(appInstanceStatuses.get(0), instanceStatusModel, curr);
            this.updateById(instanceStatus);
        }
    }

    /**
     * 创建实例状态对象
     *
     * @param instanceStatusModel
     * @param curr
     * @return
     */
    private AmdbAppInstanceStatus getInsertInstanceStatusModel(InstanceStatusModel instanceStatusModel, LocalDateTime curr) {
        AmdbAppInstanceStatus appInstanceStatus = new AmdbAppInstanceStatus();
        appInstanceStatus.setAppName(instanceStatusModel.getAppName());
        appInstanceStatus.setAgentId(instanceStatusModel.getAgentId());
        appInstanceStatus.setIp(instanceStatusModel.getAddress());
        appInstanceStatus.setPid(instanceStatusModel.getPid());
        appInstanceStatus.setHostname(instanceStatusModel.getHost());
        appInstanceStatus.setAgentLanguage(instanceStatusModel.getAgentLanguage());
        appInstanceStatus.setAgentVersion(instanceStatusModel.getAgentVersion());

        dealWithInstanceStatusModel(instanceStatusModel);

        appInstanceStatus.setProbeVersion(instanceStatusModel.getSimulatorVersion());
        appInstanceStatus.setErrorCode(instanceStatusModel.getErrorCode());
        appInstanceStatus.setErrorMsg(instanceStatusModel.getErrorMsg());
        appInstanceStatus.setProbeStatus(instanceStatusModel.getAgentStatus());

        //租户相关
        appInstanceStatus.setUserId(instanceStatusModel.getUserId());
        appInstanceStatus.setUserAppKey(instanceStatusModel.getTenantAppKey());
        appInstanceStatus.setEnvCode(instanceStatusModel.getEnvCode());

        appInstanceStatus.setJvmArgs(instanceStatusModel.getJvmArgs());
        appInstanceStatus.setJdk(instanceStatusModel.getJdk());
        appInstanceStatus.setGmtCreate(curr);
        appInstanceStatus.setGmtModify(curr);
        return appInstanceStatus;
    }

    private void dealWithInstanceStatusModel(InstanceStatusModel instanceStatusModel) {
        //探针状态转换
        if (instanceStatusModel != null) {
            if (instanceStatusModel.getSimulatorVersion() == null) {
                log.info("探针版本为null");
                instanceStatusModel.setSimulatorVersion("未知版本");
            }
            switch (StringUtil.parseStr(instanceStatusModel.getAgentStatus())) {
                case "INSTALLED":
                    instanceStatusModel.setAgentStatus("0");
                    break;
                case "UNINSTALL":
                    instanceStatusModel.setAgentStatus("1");
                    break;
                case "INSTALLING":
                    instanceStatusModel.setAgentStatus("2");
                    break;
                case "UNINSTALLING":
                    instanceStatusModel.setAgentStatus("3");
                    break;
                case "INSTALL_FAILED":
                    instanceStatusModel.setAgentStatus("4");
                    break;
                case "UNINSTALL_FAILED":
                    instanceStatusModel.setAgentStatus("5");
                    break;
                default:
                    //do nothing
                    log.info("未知状态:{}", StringUtil.parseStr(instanceStatusModel.getAgentStatus()));
                    instanceStatusModel.setAgentStatus("99");
            }
        }
    }


    /**
     * 更新实例状态对象
     *
     * @param oldInstanceStatusDO
     * @param newInstanceStatusModel
     * @param curr
     * @return
     */
    private AmdbAppInstanceStatus getUpdateInstanceStatusModel(AmdbAppInstanceStatus oldInstanceStatusDO, InstanceStatusModel newInstanceStatusModel, LocalDateTime curr) {
        oldInstanceStatusDO.setAgentId(newInstanceStatusModel.getAgentId());
        oldInstanceStatusDO.setIp(newInstanceStatusModel.getAddress());
        oldInstanceStatusDO.setPid(newInstanceStatusModel.getPid());
        oldInstanceStatusDO.setHostname(newInstanceStatusModel.getHost());
        oldInstanceStatusDO.setAgentLanguage(newInstanceStatusModel.getAgentLanguage());
        oldInstanceStatusDO.setAgentVersion(newInstanceStatusModel.getAgentVersion());

        dealWithInstanceStatusModel(newInstanceStatusModel);

        //租户相关
        oldInstanceStatusDO.setUserId(newInstanceStatusModel.getUserId());
        oldInstanceStatusDO.setUserAppKey(newInstanceStatusModel.getTenantAppKey());
        oldInstanceStatusDO.setEnvCode(newInstanceStatusModel.getEnvCode());

        oldInstanceStatusDO.setProbeVersion(newInstanceStatusModel.getSimulatorVersion());
        oldInstanceStatusDO.setErrorCode(newInstanceStatusModel.getErrorCode());
        oldInstanceStatusDO.setErrorMsg(newInstanceStatusModel.getErrorMsg());
        oldInstanceStatusDO.setProbeStatus(newInstanceStatusModel.getAgentStatus());
        oldInstanceStatusDO.setGmtModify(curr);
        return oldInstanceStatusDO;
    }


    private void batchOffline() {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.MINUTE, -3);
        QueryWrapper<AmdbAppInstanceStatus> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().lt(AmdbAppInstanceStatus::getGmtModify, instance.getTime());
        List<AmdbAppInstanceStatus> appInstanceStatuses = this.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(appInstanceStatuses)) {
            List<Long> ids = appInstanceStatuses.stream().map(AmdbAppInstanceStatus::getId).collect(Collectors.toList());
            this.removeByIds(ids);
            instancePathMap.clear();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate((new Runnable() {
            @Override
            public void run() {
                try {
                    batchOffline();
                } catch (Exception e) {
                    log.error("状态下线节点出现异常", e);
                }
            }
        }), 20, 60, TimeUnit.SECONDS);
    }
}
