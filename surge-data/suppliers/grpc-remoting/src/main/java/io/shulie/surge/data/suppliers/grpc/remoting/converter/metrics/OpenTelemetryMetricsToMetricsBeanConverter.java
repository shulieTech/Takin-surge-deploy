package io.shulie.surge.data.suppliers.grpc.remoting.converter.metrics;

import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.MetricsData;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.ScopeMetrics;
import io.opentelemetry.proto.resource.v1.Resource;
import io.shulie.surge.data.suppliers.grpc.remoting.converter.OpenTelemetryBeanConverter;
import io.shulie.surge.data.suppliers.grpc.remoting.metrics.MetricsProtoBean;
import io.shulie.takin.data.stream.acceptor.proto.ProtoJsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Opentelemetry trace转换为MetricsProtoBean
 *
 * @author vincent
 * @date 2022/07/02 13:45
 **/
@Slf4j
public class OpenTelemetryMetricsToMetricsBeanConverter implements OpenTelemetryBeanConverter<MetricsProtoBean> {
    private OpentelemetryMetricsBaseConverter opentelemetryMetricsBaseConverter;

    public OpenTelemetryMetricsToMetricsBeanConverter() {
        opentelemetryMetricsBaseConverter = new OpentelemetryMetricsBaseConverter();
    }

    @Override
    public List<MetricsProtoBean> convert(String json) throws InvalidProtocolBufferException {
        MetricsData metricsData = (MetricsData)ProtoJsonUtil.toObject(MetricsData.newBuilder(), json);
        List<MetricsProtoBean> metricsProtoBeans = Lists.newArrayList();
        List<ResourceMetrics> resourceMetricses = metricsData.getResourceMetricsList();
        for (ResourceMetrics resourceMetrics : resourceMetricses) {
            Resource resource = resourceMetrics.getResource();
            List<ScopeMetrics> scopeMetricses = resourceMetrics.getScopeMetricsList();
            for (ScopeMetrics scopeMetrics : scopeMetricses) {
                InstrumentationScope instrumentationScope = scopeMetrics.getScope();
                List<Metric> metrics = scopeMetrics.getMetricsList();
                for (Metric metric : metrics) {
                    try {
                        metricsProtoBeans.addAll(opentelemetryMetricsBaseConverter.sumConvert(resource, metric));
                        check();
                    } catch (Exception e) {
                        log.error("Convert to metricsProtoBean failed.", e);
                    }
                }
            }
        }
        return metricsProtoBeans;
    }

    @Override
    public List<MetricsProtoBean> convertMetric(ExportMetricsServiceRequest request) {
        List<MetricsProtoBean> metricsProtoBeans = Lists.newArrayList();
        List<ResourceMetrics> resourceMetricses = request.getResourceMetricsList();
        for (ResourceMetrics resourceMetrics : resourceMetricses) {
            Resource resource = resourceMetrics.getResource();
            List<ScopeMetrics> scopeMetricses = resourceMetrics.getScopeMetricsList();
            for (ScopeMetrics scopeMetrics : scopeMetricses) {
                InstrumentationScope instrumentationScope = scopeMetrics.getScope();
                List<Metric> metrics = scopeMetrics.getMetricsList();
                for (Metric metric : metrics) {
                    try {
                        metricsProtoBeans.addAll(opentelemetryMetricsBaseConverter.sumConvert(resource, metric));
                        check();
                    } catch (Exception e) {
                        log.error("Convert to traceProtoBean failed.", e);
                    }
                }
            }
        }
        return metricsProtoBeans;
    }



    public void check() {

    }

    @Override
    public List<MetricsProtoBean> convertTrace(ExportTraceServiceRequest request) {
        return null;
    }



    @Override
    public List<MetricsProtoBean> convertLog(ExportLogsServiceRequest request) {
        return null;
    }
}
