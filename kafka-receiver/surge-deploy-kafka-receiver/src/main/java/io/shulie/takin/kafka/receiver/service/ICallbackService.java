package io.shulie.takin.kafka.receiver.service;

import io.shulie.takin.kafka.receiver.constant.cloud.CallbackType;
import io.shulie.takin.kafka.receiver.entity.Callback;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 回调表 服务类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
public interface ICallbackService extends IService<Callback> {

    void create(String toString, CallbackType callbackType, String writeValueAsString);
}
