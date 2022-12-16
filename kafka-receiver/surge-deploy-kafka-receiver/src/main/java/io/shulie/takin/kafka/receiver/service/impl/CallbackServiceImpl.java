package io.shulie.takin.kafka.receiver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.shulie.takin.kafka.receiver.constant.cloud.CallbackType;
import io.shulie.takin.kafka.receiver.dao.cloud.CallbackMapper;
import io.shulie.takin.kafka.receiver.entity.Callback;
import io.shulie.takin.kafka.receiver.service.ICallbackService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * <p>
 * 回调表 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@Service
public class CallbackServiceImpl extends ServiceImpl<CallbackMapper, Callback> implements ICallbackService {

    @Override
    public void create(String url, CallbackType type, String content) {
        int typeValue = type == null ? -1 : type.getCode();
        Callback callback = new Callback();
        callback.setType(typeValue);
        callback.setUrl(url);
        callback.setContext(content.getBytes(StandardCharsets.UTF_8));
        this.save(callback);
    }
}
