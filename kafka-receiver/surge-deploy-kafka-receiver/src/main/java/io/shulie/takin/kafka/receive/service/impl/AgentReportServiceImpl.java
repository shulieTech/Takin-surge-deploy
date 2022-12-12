package io.shulie.takin.kafka.receive.service.impl;

import io.shulie.takin.kafka.receive.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receive.entity.AgentReport;
import io.shulie.takin.kafka.receive.dao.web.AgentReportMapper;
import io.shulie.takin.kafka.receive.service.IAgentReportService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

    @Override
    public void dealMessage(String toJSONString, TenantCommonExt dealHeader) {

    }
}
