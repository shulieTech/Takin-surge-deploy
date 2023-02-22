package io.shulie.takin.kafka.receiver.service;

import io.shulie.takin.kafka.receiver.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receiver.entity.ApplicationDsWarn;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhaoyong
 * @since 2023-02-15
 */
public interface IApplicationDsWarnService extends IService<ApplicationDsWarn> {

    void dealMessage(String toJSONString, TenantCommonExt dealHeader);
}
