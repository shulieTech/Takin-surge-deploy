package io.shulie.takin.kafka.receiver.service;

import io.shulie.takin.kafka.receiver.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receiver.entity.ApplicationMiddleware;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 应用中间件 服务类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
public interface IApplicationMiddlewareService extends IService<ApplicationMiddleware> {

    /**
     * 当前处理逻辑没有引入对比逻辑
     * @param body
     * @param dealHeader
     */
    void dealMessage(String body, TenantCommonExt dealHeader);
}
