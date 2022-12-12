package io.shulie.takin.kafka.receive.service.impl;

import io.shulie.takin.kafka.receive.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receive.entity.ApplicationMiddleware;
import io.shulie.takin.kafka.receive.dao.web.ApplicationMiddlewareMapper;
import io.shulie.takin.kafka.receive.service.IApplicationMiddlewareService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 应用中间件 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Service
public class ApplicationMiddlewareServiceImpl extends ServiceImpl<ApplicationMiddlewareMapper, ApplicationMiddleware> implements IApplicationMiddlewareService {

    @Override
    public void dealMessage(String body, TenantCommonExt dealHeader) {

    }
}
