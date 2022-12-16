package io.shulie.takin.kafka.receiver.service;

import io.shulie.takin.kafka.receiver.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receiver.entity.ShadowJobConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 影子JOB任务配置 服务类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
public interface IShadowJobConfigService extends IService<ShadowJobConfig> {

    void dealMessage(String body, TenantCommonExt dealHeader);
}
