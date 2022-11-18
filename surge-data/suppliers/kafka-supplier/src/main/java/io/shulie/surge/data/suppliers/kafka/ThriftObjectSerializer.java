package io.shulie.surge.data.suppliers.kafka;

import io.shulie.surge.data.runtime.common.ObjectSerializer;
import io.shulie.takin.sdk.kafka.impl.MessageDeserializer;

/**
 * thrift对象序列化工具
 *
 * @author vincent
 * @date 2022/11/18 13:59
 **/
public class ThriftObjectSerializer implements ObjectSerializer {

    private static final MessageDeserializer messageDeserializer = new MessageDeserializer();

    /**
     * 序列化工具名称
     *
     * @return
     */
    @Override
    public String name() {
        return "thrift";
    }

    /**
     * 序列化，将对象转换为字节数组
     *
     * @param t
     * @return
     */
    @Override
    public <T> byte[] serialize(T t) {
        throw new UnsupportedOperationException("Thrift serialize is not supported.");
    }

    /**
     * 反序列化，将字节数组转换为对象
     *
     * @param value
     * @return
     */
    @Override
    public <T> T deserialize(byte[] value) {
        return (T) messageDeserializer.deserialize(value);
    }
}
