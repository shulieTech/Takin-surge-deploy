package io.shulie.surge.data.suppliers.grpc.remoting.converter.trace.middleware;

import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.shulie.surge.data.suppliers.grpc.remoting.converter.ConvertUtils;
import io.shulie.surge.data.suppliers.grpc.remoting.trace.TraceProtoBean;

import java.util.List;
import java.util.Map;

/**
 * http client 转换器
 *
 * @author vincent
 * @date 2022/07/02 15:42
 **/
public class JobMiddlewareConverter implements MiddlewareConverter {

    private static final String JOB_SYSTEM = "job.system";
    private static final String JOB_SERVICE = "job.service";
    private static final String JOB_METHOD = "job.method";

    /**
     * 属性转换
     *
     * @param span
     * @param traceBean
     */
    @Override
    public void convert(Span span, TraceProtoBean traceBean) {
        /**
         * traceBean.setAttributes("");
         * traceBean.setMethod();
         * traceBean.setService();
         * traceBean.setResultCode();
         * traceBean.setComment();
         */
        List<KeyValue> keyValues = span.getAttributesList();
        Map<String, String> attributes = ConvertUtils.attributeToMap(keyValues);

        if (attributes.containsKey(JOB_SERVICE)) {
            traceBean.setService(attributes.get(JOB_SERVICE));
        } else {
            throw new IllegalArgumentException(JOB_SERVICE + " is null" + span);
        }

        if (attributes.containsKey(JOB_METHOD)) {
            traceBean.setMethod(attributes.get(JOB_METHOD));
        } else {
            throw new IllegalArgumentException(JOB_METHOD + " is null" + span);
        }
    }
}
