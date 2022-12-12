package io.shulie.takin.kafka.receive.service.impl;

import io.shulie.takin.kafka.receive.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receive.entity.AppAgentConfigReport;
import io.shulie.takin.kafka.receive.dao.web.AppAgentConfigReportMapper;
import io.shulie.takin.kafka.receive.service.IAppAgentConfigReportService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * agent配置上报详情 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Service
public class AppAgentConfigReportServiceImpl extends ServiceImpl<AppAgentConfigReportMapper, AppAgentConfigReport> implements IAppAgentConfigReportService {

    @Override
    public void dealMessage(String body, TenantCommonExt dealHeader) {

    }
}
