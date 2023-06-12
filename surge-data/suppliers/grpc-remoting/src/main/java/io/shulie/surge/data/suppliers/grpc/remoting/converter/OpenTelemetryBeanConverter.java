package io.shulie.surge.data.suppliers.grpc.remoting.converter;

import com.google.protobuf.InvalidProtocolBufferException;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.shulie.surge.data.suppliers.grpc.remoting.ProtoBean;

import java.util.List;

/**
 * opentelemetry converter 接口
 *
 * @author vincent
 * @date 2022/07/13 11:08
 **/
public interface OpenTelemetryBeanConverter<T extends ProtoBean> {

    /**
     * 转换为协议类
     *
     * @param json
     * @return
     */
    List<T> convert(String json) throws InvalidProtocolBufferException;

    /**
     * trace 转化
     *
     * @param request
     * @return
     */
    List<T> convertTrace(ExportTraceServiceRequest request);

    /**
     * metric 转化
     *
     * @param request
     * @return
     */
    List<T> convertMetric(ExportMetricsServiceRequest request);

    /**
     * log 转化
     *
     * @param request
     * @return
     */
    List<T> convertLog(ExportLogsServiceRequest request);

}
