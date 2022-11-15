package io.shulie.surge.data.deploy.pradar.common;

import java.io.Serializable;

/**
 * 序列化接口
 *
 * @author vincent
 * @date 2022/11/15 14:06
 **/
public interface ObjectSerializer extends Serializable {

    /**
     * 序列化工具名称
     *
     * @return
     */
    String name();

    /**
     * 序列化，将对象转换为字节数组
     *
     * @param t
     * @param <T>
     * @return
     */
    <T> byte[] serialize(T t);

    /**
     * 反序列化，将字节数组转换为对象
     *
     * @param value
     * @param <T>
     * @return
     */
    <T> T deserialize(byte[] value);
}
