package io.shulie.takin.kafka.receiver.service;

import io.shulie.takin.kafka.receiver.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receiver.entity.AppAgentConfigReport;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * agent配置上报详情 服务类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
public interface IAppAgentConfigReportService extends IService<AppAgentConfigReport> {

    void dealMessage(String body, TenantCommonExt dealHeader);
}
