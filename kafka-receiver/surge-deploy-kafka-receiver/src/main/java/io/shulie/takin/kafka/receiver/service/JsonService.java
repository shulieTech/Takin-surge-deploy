package io.shulie.takin.kafka.receiver.service;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Json服务
 *
 * @author <a href="mailto:472546172@qq.com">张天赐</a>
 */
public interface JsonService {
    /**
     * 序列化
     *
     * @param obj 待序列化对象
     * @return Json字符串
     */
    String writeValueAsString(Object obj);

    /**
     * 反序列化
     *
     * @param jsonString json字符串
     * @param valueType  解析类型
     * @param <T>        解析类型 - 泛型
     * @return 反序列化结果
     */
    <T> T readValue(String jsonString, Class<T> valueType);

    /**
     * 反序列化
     *
     * @param jsonString   json字符串
     * @param valueTypeRef 解析类型
     * @param <T>          解析类型 - 泛型
     * @return 反序列化结果
     */
    <T> T readValue(String jsonString, TypeReference<T> valueTypeRef);
}
