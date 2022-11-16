package io.shulie.surge.data.runtime.common;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * kryo 序列化方法
 *
 * @author vincent
 * @date 2022/11/15 14:00
 **/
public class KryoObjectSerializer implements ObjectSerializer {

    private static final Logger logger = LoggerFactory.getLogger(KryoObjectSerializer.class);

    public static ThreadLocal<Kryo> threadLocal = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();

            /**
             * 不要轻易改变这里的配置！更改之后，序列化的格式就会发生变化，
             * 上线的同时就必须清除 Redis 里的所有缓存，
             * 否则那些缓存再回来反序列化的时候，就会报错
             */
            //支持对象循环引用（否则会栈溢出）
            kryo.setReferences(true); //默认值就是 true，添加此行的目的是为了提醒维护者，不要改变这个配置

            //不强制要求注册类（注册行为无法保证多个 JVM 内同一个类的注册编号相同；而且业务系统中大量的 Class 也难以一一注册）
            kryo.setRegistrationRequired(false);

            ((Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy()).setFallbackInstantiatorStrategy(
                    new StdInstantiatorStrategy());//设定默认的实例化器
            return kryo;
        }
    };

    /**
     * 反序列化
     *
     * @param value
     * @param <T>
     * @return
     */
    public <T> T deserialize(byte[] value) {
        try {
            T data = (T) threadLocal.get().readClassAndObject(new Input(value));
            return data;
        } catch (Exception e) {
            logger.error("deserializeKeyAndValue failed.", e);
        }
        return null;
    }

    /**
     * 序列化工具名称
     *
     * @return
     */
    @Override
    public String name() {
        return "kryo";
    }

    /**
     * 序列化
     *
     * @param t
     * @param <T>
     * @return
     */
    public <T> byte[] serialize(T t) {
        ByteArrayOutputStream byteArrayOutputStream = null;
        Output output = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            output = new Output(byteArrayOutputStream);
            if (t == null) {
                logger.warn("message is null");
            }
            threadLocal.get().writeClassAndObject(output, t);
            output.flush();
            return byteArrayOutputStream.toByteArray();
        } finally {
            try {
                output.close();
                byteArrayOutputStream.close();
            } catch (IOException e) {
                logger.error("close output error.");
            }
        }
    }
}
