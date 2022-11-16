package io.shulie.surge.data.runtime.common;

import com.google.common.collect.Maps;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * kryo 序列化工厂
 *
 * @author vincent
 * @date 2022/11/15 14:00
 **/
public class ObjectSerializerFactory {

    private Map<String, ObjectSerializer> OBJECT_SERIALIZERS = Maps.newHashMap();
    private static ObjectSerializerFactory INSTANCE = new ObjectSerializerFactory();

    public ObjectSerializerFactory() {
        ServiceLoader<ObjectSerializer> serviceLoader = ServiceLoader.load(ObjectSerializer.class);
        Iterator<ObjectSerializer> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            ObjectSerializer objectSerializer = iterator.next();
            OBJECT_SERIALIZERS.put(objectSerializer.name(), objectSerializer);
        }
    }

    public static ObjectSerializer getObjectSerializer(String serializerName) {
        return INSTANCE.OBJECT_SERIALIZERS.get(serializerName);
    }
}
