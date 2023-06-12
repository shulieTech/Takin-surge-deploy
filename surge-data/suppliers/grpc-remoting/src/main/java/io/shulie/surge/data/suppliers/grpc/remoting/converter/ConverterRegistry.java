package io.shulie.surge.data.suppliers.grpc.remoting.converter;

import com.google.common.collect.Maps;
import io.shulie.surge.data.suppliers.grpc.remoting.MiddlewareType;
import io.shulie.surge.data.suppliers.grpc.remoting.converter.trace.middleware.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2022/8/19 16:56
 */
public class ConverterRegistry {

    private final static int CONVERTER_MATCHES_ALL = -99;
    private Map<Integer, List<MiddlewareConverter>> middlewareConverters = Maps.newHashMap();

    public ConverterRegistry() {
        registerMiddlewareConverter(MiddlewareType.TYPE_WEB_SERVER.getType(), new HttpServerMiddlewareConverter());
        registerMiddlewareConverter(MiddlewareType.TYPE_HTTP_CLIENT.getType(), new HttpClientMiddlewareConverter());
        registerMiddlewareConverter(MiddlewareType.TYPE_DB.getType(), new DatabaseMiddlewareConverter());
        registerMiddlewareConverter(MiddlewareType.TYPE_RPC.getType(), new RpcMiddlewareConverter());
        registerMiddlewareConverter(MiddlewareType.TYPE_MQ.getType(), new MQMiddlewareConverter());
        registerMiddlewareConverter(MiddlewareType.TYPE_CACHE.getType(), new DatabaseMiddlewareConverter());
        registerMiddlewareConverter(MiddlewareType.TYPE_SEARCH.getType(), new DatabaseMiddlewareConverter());
        registerMiddlewareConverter(MiddlewareType.TYPE_JOB.getType(), new JobMiddlewareConverter());
        registerMiddlewareConverter(MiddlewareType.TYPE_FS.getType(), new RpcMiddlewareConverter());
        registerMiddlewareConverter(MiddlewareType.TYPE_LOCAL.getType(), new LocalMiddlewareConverter());
        registerMiddlewareConverter(MiddlewareType.TYPE_CUSTOMER.getType(),new CustomerMiddlewareConverter());
        registerMiddlewareConverter(MiddlewareType.TYPE_UNKNOW.getType(), new RpcMiddlewareConverter());
        registerMiddlewareConverter(CONVERTER_MATCHES_ALL, new ResultCodeConverter());
        registerMiddlewareConverter(CONVERTER_MATCHES_ALL, new CommonConverter());
        registerMiddlewareConverter(CONVERTER_MATCHES_ALL, new ServiceTypeConverter());

    }

    /**
     * 注册中间件转换器
     *
     * @param middlewareType
     * @param converter
     */
    public synchronized void registerMiddlewareConverter(int middlewareType, MiddlewareConverter converter) {
        List<MiddlewareConverter> list = middlewareConverters.get(middlewareType);
        if (list == null) {
            list = new ArrayList<>();
            middlewareConverters.put(middlewareType, list);
        }
        list.add(converter);
    }

    /**
     * 查找中间件转换器
     *
     * @param middlewareType
     * @return
     */
    public List<MiddlewareConverter> findMiddlewareConverters(MiddlewareType middlewareType) {
        if (middlewareType == null) {
            return Collections.EMPTY_LIST;
        }
        return findMiddlewareConverters(middlewareType.getType());
    }

    /**
     * 查找中间件转换器
     *
     * @param middlewareType
     * @return
     */
    public List<MiddlewareConverter> findMiddlewareConverters(int middlewareType) {
        List<MiddlewareConverter> ret = new ArrayList<>();
        List<MiddlewareConverter> list = middlewareConverters.get(middlewareType);
        if (list != null) {
            ret.addAll(list);
        }
        List<MiddlewareConverter> allConverters = middlewareConverters.get(CONVERTER_MATCHES_ALL);
        if (allConverters != null) {
            ret.addAll(allConverters);
        }
        return ret;
    }
}
