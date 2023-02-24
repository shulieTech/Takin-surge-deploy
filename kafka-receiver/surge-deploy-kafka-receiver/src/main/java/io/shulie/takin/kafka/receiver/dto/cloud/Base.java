package io.shulie.takin.kafka.receiver.dto.cloud;


import io.shulie.takin.kafka.receiver.constant.cloud.CallbackType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 基类
 *
 * @author <a href="mailto:472546172@qq.com">张天赐</a>
 */
@Data
@Accessors(chain = true)
public abstract class Base<T> {
    protected Base() {
        setSign("");
        setTime(new Date());
        setCallbackTime(getTime());
    }

    /**
     * 事件发生的时间
     */
    private Date time;
    /**
     * 发起回调的时间
     */
    private Date callbackTime;
    /**
     * 签名
     */
    private String sign;
    /**
     * 数据
     */
    private T data;

    /**
     * 上报类型
     *
     * @return 上报类型
     */
    public abstract CallbackType getType();
}
