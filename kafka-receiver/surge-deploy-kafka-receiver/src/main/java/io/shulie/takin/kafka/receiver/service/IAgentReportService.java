package io.shulie.takin.kafka.receiver.service;

import io.shulie.takin.kafka.receiver.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receiver.entity.AgentReport;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 探针心跳数据 服务类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
public interface IAgentReportService extends IService<AgentReport> {

    void dealMessage(String toJSONString, TenantCommonExt dealHeader);
}
