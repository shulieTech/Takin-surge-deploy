package io.shulie.takin.kafka.receiver.service;

import io.shulie.takin.kafka.receiver.entity.PressureExample;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 任务实例 服务类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
public interface IPressureExampleService extends IService<PressureExample> {

    void onHeartbeat(Long pressureExampleId);
}
