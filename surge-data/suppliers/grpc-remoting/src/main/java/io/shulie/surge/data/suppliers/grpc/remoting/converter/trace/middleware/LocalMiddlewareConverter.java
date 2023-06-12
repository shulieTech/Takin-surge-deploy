package io.shulie.surge.data.suppliers.grpc.remoting.converter.trace.middleware;

import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.shulie.surge.data.suppliers.grpc.remoting.converter.ConvertUtils;
import io.shulie.surge.data.suppliers.grpc.remoting.trace.TraceProtoBean;

import java.util.List;
import java.util.Map;

/**
 * LocalC中间件转换类
 *
 * @author vincent
 * @date 2022/07/02 15:30
 **/
public class LocalMiddlewareConverter implements MiddlewareConverter {

    private static final String CODE_FUNCTION = "code.function";
    private static final String CODE_NAMESPACE = "code.namespace";

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
         * code.function对应method code.namespace对应 service
         */
        List<KeyValue> keyValues = span.getAttributesList();
        Map<String, String> attributes = ConvertUtils.attributeToMap(keyValues);

        if (attributes.containsKey(CODE_NAMESPACE)) {
            traceBean.setService(attributes.get(CODE_NAMESPACE));
        } else {
            throw new IllegalArgumentException(CODE_NAMESPACE + " is null" + span);
        }

        if (attributes.containsKey(CODE_FUNCTION)) {
            traceBean.setMethod(attributes.get(CODE_FUNCTION));
        } else {
            throw new IllegalArgumentException(CODE_FUNCTION + " is null" + span);
        }
    }
}
