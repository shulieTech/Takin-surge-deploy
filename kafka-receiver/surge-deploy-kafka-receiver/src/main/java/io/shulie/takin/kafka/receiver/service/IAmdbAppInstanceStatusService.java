package io.shulie.takin.kafka.receiver.service;

import io.shulie.takin.kafka.receiver.entity.AmdbAppInstanceStatus;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 应用实例探针状态表 服务类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-14
 */
public interface IAmdbAppInstanceStatusService extends IService<AmdbAppInstanceStatus> {

    void dealPradarStatusMessage(Map entityBody);
}
