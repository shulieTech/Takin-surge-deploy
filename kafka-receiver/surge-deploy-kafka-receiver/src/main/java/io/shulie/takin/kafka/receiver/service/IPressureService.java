package io.shulie.takin.kafka.receiver.service;

import io.shulie.takin.kafka.receiver.dto.cloud.MetricsInfo;
import io.shulie.takin.kafka.receiver.entity.Pressure;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 任务 服务类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
public interface IPressureService extends IService<Pressure> {

    void upload(List<MetricsInfo> metricsInfos, Long parseLong);
}
