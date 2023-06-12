package io.shulie.surge.data.suppliers.grpc.remoting.converter.trace.middleware;

import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.shulie.surge.data.suppliers.grpc.remoting.converter.ConvertUtils;
import io.shulie.surge.data.suppliers.grpc.remoting.trace.TraceProtoBean;

import java.util.List;
import java.util.Map;

/**
 * @author guann1n9
 * @date 2023/2/24 5:48 PM
 */
public class CustomerMiddlewareConverter implements MiddlewareConverter {


    public static final String NAMESPACE = "code.namespace";
    public static final String FUNCTION = "code.function";

    @Override
    public void convert(Span span, TraceProtoBean traceProtoBean) {
        List<KeyValue> keyValues = span.getAttributesList();
        Map<String, String> attributes = ConvertUtils.attributeToMap(keyValues);

        if (attributes.containsKey(NAMESPACE)) {
            traceProtoBean.setService(attributes.get(NAMESPACE));
        } else {
            throw new IllegalArgumentException(NAMESPACE + " is null" + span);
        }

        if (attributes.containsKey(FUNCTION)) {
            traceProtoBean.setMethod(attributes.get(FUNCTION));
        } else {
            throw new IllegalArgumentException(FUNCTION + " is null" + span);
        }
    }
}
