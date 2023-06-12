package io.shulie.surge.data.suppliers.grpc.remoting.converter.trace.middleware;

import io.opentelemetry.proto.trace.v1.Span;
import io.shulie.surge.data.suppliers.grpc.remoting.trace.TraceProtoBean;


/**
 * 基准中间件转换类
 *
 * @author vincent
 * @date 2022/07/02 15:30
 **/
public interface MiddlewareConverter {

    /**
     * 属性转换
     *
     * @param span
     * @param traceProtoBean
     */
    void convert(Span span, TraceProtoBean traceProtoBean);

}
