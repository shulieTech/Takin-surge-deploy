package io.shulie.surge.data.suppliers.grpc.remoting.service;

import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.proto.resource.v1.Resource;
import io.shulie.surge.data.suppliers.grpc.remoting.AcceptorDispatcherException;
import io.shulie.surge.data.suppliers.grpc.remoting.converter.ConvertUtils;
import io.shulie.surge.data.suppliers.grpc.remoting.converter.trace.OpenTelemetryTraceToTraceBeanConverter;
import io.shulie.surge.data.suppliers.grpc.remoting.trace.TraceAcceptorProto;
import io.shulie.surge.data.suppliers.grpc.remoting.trace.TraceProtoBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @author angju
 * @date 2022/9/1 19:46
 */
@Slf4j
public class TracesServiceImpl extends TraceServiceGrpc.TraceServiceImplBase {

    private OpenTelemetryTraceToTraceBeanConverter openTelemetryTraceToTraceBeanConverter;
    private TraceAcceptorProto traceAcceptorProto;

    private boolean skipSilentLog;

    public TracesServiceImpl(TraceAcceptorProto traceAcceptorProto, boolean skipErrorLog, boolean skipSilentLog) {
        this.openTelemetryTraceToTraceBeanConverter = new OpenTelemetryTraceToTraceBeanConverter(skipErrorLog, skipSilentLog);
        this.traceAcceptorProto = traceAcceptorProto;
        this.skipSilentLog = skipSilentLog;
    }

    public static final String AGENT_ID = "agent.id";

    public static final String APP_NAME = "service.name";


    @Override
    public void export(ExportTraceServiceRequest request,
                       StreamObserver<ExportTraceServiceResponse> responseObserver) {
        this.handle(request);
        responseObserver.onNext(ExportTraceServiceResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    /**
     * 脱敏方法用
     * @param request
     * @param responseObserver
     */
    @Override
    public void export1(ExportTraceServiceRequest request, StreamObserver<ExportTraceServiceResponse> responseObserver) {
        this.handle(request);
        responseObserver.onNext(ExportTraceServiceResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    private void handle(ExportTraceServiceRequest request) {
        long startTime = System.currentTimeMillis();
        String agentId = "";
        String appName = "";
        if (CollectionUtils.isNotEmpty(request.getResourceSpansList())) {
            Resource resource = request.getResourceSpansList().get(0).getResource();
            if (CollectionUtils.isNotEmpty(resource.getAttributesList())) {
                Map<String, String> map = ConvertUtils.attributeToMap(resource.getAttributesList());
                agentId = map.get(AGENT_ID);
                appName = map.get(APP_NAME);
            }
        }
        if(skipSilentLog) {
            log.info("Trace entry begin.appName:{},agentId:{},size:{}", appName, agentId, request.toByteArray().length);
        }
        try {
            if (request.toByteArray().length > 4194304) {
                if (skipSilentLog) {
                    log.info("grpc:{}", request);
                }
            }
            List<TraceProtoBean> traceProtoBeans = openTelemetryTraceToTraceBeanConverter.convertTrace(request);
            for (TraceProtoBean traceProtoBean : traceProtoBeans) {
                traceProtoBean.setProtocol("opentelemetry");
            }
            if (CollectionUtils.isNotEmpty(traceProtoBeans)) {
                try {
                    traceAcceptorProto.batchPush(traceProtoBeans);
                } catch (AcceptorDispatcherException e) {
                    if (!skipSilentLog) {
                        log.error("grpc push trace to mq: send {}", traceProtoBeans.size(), e);
                    }
                }
            }
        } catch (Exception e) {
            if (!skipSilentLog) {
                log.error("grpc push trace:{}", request, e);
            }
        }
        if(skipSilentLog) {
            log.info("Trace entry end.appName:{},agentId:{},cost:{},size:{}", appName, agentId, (System.currentTimeMillis() - startTime), request.toByteArray().length);
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
    public StreamObserver<ExportTraceServiceRequest> export(StreamObserver<ExportTraceServiceResponse> responseObserver) {

        return new StreamObserver<ExportTraceServiceRequest>(){
            @Override
            public void onNext(ExportTraceServiceRequest request) {
                long startTime = System.currentTimeMillis();
                String agentId = "";
                String appName = "";
                if (CollectionUtils.isNotEmpty(request.getResourceSpansList())) {
                    Resource resource = request.getResourceSpansList().get(0).getResource();
                    if (CollectionUtils.isNotEmpty(resource.getAttributesList())) {
                        Map<String, String> map = ConvertUtils.attributeToMap(resource.getAttributesList());
                        agentId = map.get(AGENT_ID);
                        appName = map.get(APP_NAME);
                    }
                }
                if(skipSilentLog) {
                    log.info("Trace entry begin.appName:{},agentId:{},size:{}", appName, agentId, request.toByteArray().length);
                }
                try {
                    if (request.toByteArray().length > 4194304) {
                        if (skipSilentLog) {
                            log.info("grpc:{}", request);
                        }
                    }
                    List<TraceProtoBean> traceProtoBeans = openTelemetryTraceToTraceBeanConverter.convertTrace(request);
                    for (TraceProtoBean traceProtoBean : traceProtoBeans) {
                        traceProtoBean.setProtocol("opentelemetry");
                    }
                    if (CollectionUtils.isNotEmpty(traceProtoBeans)) {
                        try {
                            traceAcceptorProto.batchPush(traceProtoBeans);
                        } catch (AcceptorDispatcherException e) {
                            if (!skipSilentLog) {
                                log.error("grpc push trace to mq: send {}", traceProtoBeans.size(), e);
                            }
                        }
                    }
                } catch (Exception e) {
                    if (!skipSilentLog) {
                        log.error("grpc push trace:{}", request, e);
                    }
                }
                if(skipSilentLog) {
                    log.info("Trace entry end.appName:{},agentId:{},cost:{},size:{}", appName, agentId, (System.currentTimeMillis() - startTime), request.toByteArray().length);
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(ExportTraceServiceResponse.newBuilder().build());
                responseObserver.onCompleted();
            }
        };
    }
}
