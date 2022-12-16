package io.shulie.takin.kafka.receiver.service;

import io.shulie.takin.kafka.receiver.entity.AmdbAppInstance;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-14
 */
public interface IAmdbAppInstanceService extends IService<AmdbAppInstance> {

    void dealPradarClientMessage(Map entityBody);
}
