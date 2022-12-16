package io.shulie.takin.kafka.receiver.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.shulie.takin.kafka.receiver.constant.web.*;
import io.shulie.takin.kafka.receiver.dto.web.AgentHeartbeatRequest;
import io.shulie.takin.kafka.receiver.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receiver.entity.AgentReport;
import io.shulie.takin.kafka.receiver.dao.web.AgentReportMapper;
import io.shulie.takin.kafka.receiver.entity.ApplicationMnt;
import io.shulie.takin.kafka.receiver.entity.ApplicationPluginUpgrade;
import io.shulie.takin.kafka.receiver.service.IAgentReportService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.shulie.takin.kafka.receiver.service.IApplicationMntService;
import io.shulie.takin.kafka.receiver.service.IApplicationPluginUpgradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 探针心跳数据 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Service
public class AgentReportServiceImpl extends ServiceImpl<AgentReportMapper, AgentReport> implements IAgentReportService {

    private static final Logger log = LoggerFactory.getLogger(AgentReportServiceImpl.class);
    @Resource
    private IApplicationMntService iApplicationMntService;
    @Resource
    private IApplicationPluginUpgradeService iApplicationPluginUpgradeService;

    @Override
    public void dealMessage(String message, TenantCommonExt dealHeader) {
        AgentHeartbeatRequest commandRequest = JSONObject.parseObject(message, AgentHeartbeatRequest.class);
        ApplicationMnt application = iApplicationMntService.getApplicationByTenantIdAndName(commandRequest.getProjectName(), dealHeader);
        if (application == null || application.getApplicationId() == null) {
            log.error("应用名不存在,{}", commandRequest.getProjectName());
            return;
        }

        AgentReport agentReport = new AgentReport();
        agentReport.setApplicationId(application.getApplicationId());
        BeanUtils.copyProperties(commandRequest, agentReport);

        // 获取节点当前状态
        AgentReportStatusEnum statusEnum = getAgentReportStatus(commandRequest, application.getApplicationId(), dealHeader);
        agentReport.setStatus(statusEnum.getVal());
        saveHeartbeatData(agentReport);
    }

    private AgentReportStatusEnum getAgentReportStatus(AgentHeartbeatRequest agentHeartBeatBO, Long appId, TenantCommonExt dealHeader) {
        // 判断是否已卸载  探针上报已卸载
        if (BooleanEnum.TRUE.getCode().equals(agentHeartBeatBO.getUninstallStatus())) {
            return AgentReportStatusEnum.UNINSTALL;
        }

        // 判断是否已休眠 探针上报已休眠
        if (BooleanEnum.TRUE.getCode().equals(agentHeartBeatBO.getDormantStatus())) {
            return AgentReportStatusEnum.SLEEP;
        }

        // 判断是否为启动中，agent状态为成功，simulator状态为null或者空字符串
        if (AgentStatusEnum.INSTALLED.getCode().equals(agentHeartBeatBO.getAgentStatus()) && StringUtils.isEmpty(
                agentHeartBeatBO.getSimulatorStatus())) {
            return AgentReportStatusEnum.STARTING;
        }

        // 判断是否为刚启动，agent状态为未安装，simulator状态为null或者空字符串
        if (AgentStatusEnum.UNINSTALL.getCode().equals(agentHeartBeatBO.getAgentStatus()) && StringUtils.isEmpty(
                agentHeartBeatBO.getSimulatorStatus())) {
            return AgentReportStatusEnum.BEGIN;
        }

        // 判断是否异常 agent状态异常 或 simulator状态不是安装成功
        if (AgentStatusEnum.INSTALL_FAILED.getCode().equals(agentHeartBeatBO.getAgentStatus())
                || !ProbeStatusEnum.INSTALLED.getCode().equals(agentHeartBeatBO.getSimulatorStatus())) {
            return AgentReportStatusEnum.ERROR;
        }

        //查询当前应用的升级单批次
        if (AgentStatusEnum.INSTALLED.getCode().equals(agentHeartBeatBO.getAgentStatus())
                && ProbeStatusEnum.INSTALLED.getCode().equals(agentHeartBeatBO.getSimulatorStatus())) {

            //查询最新的升级单
            QueryWrapper<ApplicationPluginUpgrade> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(ApplicationPluginUpgrade::getApplicationId, appId);
            queryWrapper.lambda().eq(ApplicationPluginUpgrade::getType, 1);
            queryWrapper.lambda().eq(ApplicationPluginUpgrade::getPluginUpgradeStatus, AgentUpgradeEnum.UPGRADE_SUCCESS.getVal());
            queryWrapper.lambda().orderByDesc(ApplicationPluginUpgrade::getId);
            queryWrapper.last("limit 1");
            List<ApplicationPluginUpgrade> pluginUpgrades = iApplicationPluginUpgradeService.list(queryWrapper);

            if (CollectionUtils.isEmpty(pluginUpgrades) || pluginUpgrades.get(0) == null
                    || pluginUpgrades.get(0).getUpgradeBatch().equals(agentHeartBeatBO.getCurUpgradeBatch())) {
                return AgentReportStatusEnum.RUNNING;
            } else {
                return AgentReportStatusEnum.WAIT_RESTART;
            }
        }

        return AgentReportStatusEnum.UNKNOWN;
    }


    private void saveHeartbeatData(AgentReport agentReport) {

        this.save(agentReport);
    }

}
