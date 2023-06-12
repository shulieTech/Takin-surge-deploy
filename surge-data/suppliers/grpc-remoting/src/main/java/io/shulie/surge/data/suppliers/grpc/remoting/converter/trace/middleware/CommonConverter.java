package io.shulie.surge.data.suppliers.grpc.remoting.converter.trace.middleware;

import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.shulie.surge.data.suppliers.grpc.remoting.converter.ConvertUtils;
import io.shulie.surge.data.suppliers.grpc.remoting.trace.TraceProtoBean;

import java.util.List;
import java.util.Map;

import static io.shulie.surge.data.suppliers.grpc.remoting.converter.trace.OpentelemetryTraceBaseConverter.*;


/**
 * @author hezhongqi
 * @Package io.shulie.takin.streams.acceptor.receiver.opentelemetry.opentelmetry.converter.trace.middleware
 * @ClassName: CommonConverter
 * @description:
 * @date 2022/8/23 14:33
 */
public class CommonConverter implements MiddlewareConverter {
    /**
     * setAttribute
     * setEvent
     *
     * @param span
     * @param traceProtoBean
     */

    @Override
    public void convert(Span span, TraceProtoBean traceProtoBean) {
        List<KeyValue> keyValues = span.getAttributesList();
        Map<String, String> attributes = ConvertUtils.attributeToMap(keyValues);
        // 仅移除request response
        ConvertUtils.removeAttribute(attributes, REQUEST, RESPONSE);
        traceProtoBean.setAttributes(ConvertUtils.exportAttribute(attributes));

        // 事件
        Map<String, Map<String, String>> eventMap = ConvertUtils.eventToMap(span.getEventsList());
        // 移除异常堆栈
        if (eventMap.containsKey(EXCEPTION)) {
            eventMap.get(EXCEPTION).remove(EXCEPTION_STACKTRACE);
        }
        traceProtoBean.setEvent(ConvertUtils.exportEvents(eventMap));

    }
}
