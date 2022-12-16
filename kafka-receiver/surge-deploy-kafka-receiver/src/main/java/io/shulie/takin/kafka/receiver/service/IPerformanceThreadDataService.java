package io.shulie.takin.kafka.receiver.service;

import io.shulie.takin.kafka.receiver.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receiver.entity.PerformanceThreadData;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-09
 */
public interface IPerformanceThreadDataService extends IService<PerformanceThreadData> {

    void dealMessage(String string, TenantCommonExt dealHeader);
}
