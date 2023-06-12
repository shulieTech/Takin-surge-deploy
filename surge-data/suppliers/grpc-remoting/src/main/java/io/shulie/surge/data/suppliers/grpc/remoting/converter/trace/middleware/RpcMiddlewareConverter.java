package io.shulie.surge.data.suppliers.grpc.remoting.converter.trace.middleware;

import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.shulie.surge.data.suppliers.grpc.remoting.converter.ConvertUtils;
import io.shulie.surge.data.suppliers.grpc.remoting.trace.TraceProtoBean;

import java.util.List;
import java.util.Map;

/**
 * RPC中间件转换类
 *
 * @author vincent
 * @date 2022/07/02 15:30
 **/
public class RpcMiddlewareConverter implements MiddlewareConverter {

    private static final String RPC_SYSTEM = "rpc.system";
    private static final String RPC_SERVICE = "rpc.service";
    private static final String RPC_METHOD = "rpc.method";

    /**
     * 属性转换
     *
     * @param span
     * @param traceBean
     */
    @Override
    public void convert(Span span, TraceProtoBean traceBean) {
        /**
         * traceBean.setMethod();
         * traceBean.setService();
         * traceBean.setComment();
         */
        List<KeyValue> keyValues = span.getAttributesList();
        Map<String, String> attributes = ConvertUtils.attributeToMap(keyValues);

        if (attributes.containsKey(RPC_SERVICE)) {
            traceBean.setService(attributes.get(RPC_SERVICE));
        } else {
            throw new IllegalArgumentException(RPC_SERVICE + " is null" + span);
        }

        if (attributes.containsKey(RPC_METHOD)) {
            traceBean.setMethod(attributes.get(RPC_METHOD));
        } else {
            throw new IllegalArgumentException(RPC_METHOD + " is null" + span);
        }

    }
}
