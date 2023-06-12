package io.shulie.surge.data.suppliers.grpc.remoting.service;

import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.collector.metrics.v1.MetricsServiceGrpc;
import io.shulie.surge.data.suppliers.grpc.remoting.AcceptorDispatcherException;
import io.shulie.surge.data.suppliers.grpc.remoting.converter.metrics.OpenTelemetryMetricsToMetricsBeanConverter;
import io.shulie.surge.data.suppliers.grpc.remoting.metrics.MetricsAcceptorProto;
import io.shulie.surge.data.suppliers.grpc.remoting.metrics.MetricsProtoBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Slf4j
public class MetricsServiceImpl extends MetricsServiceGrpc.MetricsServiceImplBase {

    private OpenTelemetryMetricsToMetricsBeanConverter openTelemetryMetricsToMetricsBeanConverter;
    private MetricsAcceptorProto metricsAcceptorProto;

    public MetricsServiceImpl(MetricsAcceptorProto metricsAcceptorProto) {
        this.openTelemetryMetricsToMetricsBeanConverter = new OpenTelemetryMetricsToMetricsBeanConverter();
        this.metricsAcceptorProto = metricsAcceptorProto;
    }

    @Override
    public void export(ExportMetricsServiceRequest request,
                       StreamObserver<ExportMetricsServiceResponse> responseObserver) {

        //格式转换
        try {
            List<MetricsProtoBean> metricsProtoBeans = openTelemetryMetricsToMetricsBeanConverter.convertMetric(request);
            for (MetricsProtoBean metricsProtoBean : metricsProtoBeans) {
                if (StringUtils.isBlank(metricsProtoBean.getDescription())) {
                    metricsProtoBean.setDescription(metricsProtoBean.getMetricsName());
                }
                metricsProtoBean.setProtocol("opentelemetry");
            }

            if (CollectionUtils.isNotEmpty(metricsProtoBeans)) {
                try {
                    metricsAcceptorProto.batchPush(metricsProtoBeans);
                } catch (AcceptorDispatcherException e) {
                    log.error("grpc push metric to mq:{}", metricsProtoBeans, e);
                }
            }
            responseObserver.onNext(ExportMetricsServiceResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            log.error("grpc push metric:{}", request, e);
        }
    }

    /**
     * <pre>
     * For performance reasons, it is recommended to keep this RPC
     * alive for the entire life of the application.
     * </pre>
     *
     * @param responseObserver
     */
//    @Override
    public StreamObserver<ExportMetricsServiceRequest> export(StreamObserver<ExportMetricsServiceResponse> responseObserver) {
        return new StreamObserver<ExportMetricsServiceRequest>() {
            @Override
            public void onNext(ExportMetricsServiceRequest request) {
                //格式转换
                try {
                    List<MetricsProtoBean> metricsProtoBeans = openTelemetryMetricsToMetricsBeanConverter.convertMetric(request);
                    for (MetricsProtoBean metricsProtoBean : metricsProtoBeans) {
                        if (StringUtils.isBlank(metricsProtoBean.getDescription())) {
                            metricsProtoBean.setDescription(metricsProtoBean.getMetricsName());
                        }
                        metricsProtoBean.setProtocol("opentelemetry");
                    }

                    if (CollectionUtils.isNotEmpty(metricsProtoBeans)) {
                        try {
                            metricsAcceptorProto.batchPush(metricsProtoBeans);
                        } catch (AcceptorDispatcherException e) {
                            log.error("grpc push metric to mq:{}", metricsProtoBeans, e);
                        }
                    }
                } catch (RuntimeException e) {
                    log.error("grpc push metric:{}", request, e);
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(ExportMetricsServiceResponse.newBuilder().build());
                responseObserver.onCompleted();
            }
        };
    }
}
