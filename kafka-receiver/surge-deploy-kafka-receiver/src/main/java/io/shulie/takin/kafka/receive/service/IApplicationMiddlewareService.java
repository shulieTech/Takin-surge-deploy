package io.shulie.takin.kafka.receive.service;

import io.shulie.takin.kafka.receive.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receive.entity.ApplicationMiddleware;
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

    void dealMessage(String body, TenantCommonExt dealHeader);
}
