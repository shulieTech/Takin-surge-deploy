package io.shulie.surge.data.suppliers.grpc.remoting.service;


import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import io.opentelemetry.proto.collector.logs.v1.LogsServiceGrpc;


public class LogsServiceImpl extends LogsServiceGrpc.LogsServiceImplBase {

    @Override
    public void export(ExportLogsServiceRequest request,
                       StreamObserver<ExportLogsServiceResponse> responseObserver) {
        responseObserver.onNext(ExportLogsServiceResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    /**
     * 流式服务端写法
     * <pre>
     * For performance reasons, it is recommended to keep this RPC
     * alive for the entire life of the application.
     * </pre>
     *
     * @param responseObserver
     */
//    @Override
    public StreamObserver<ExportLogsServiceRequest> export(StreamObserver<ExportLogsServiceResponse> responseObserver) {
        return new StreamObserver<ExportLogsServiceRequest>() {
            @Override
            public void onNext(ExportLogsServiceRequest exportLogsServiceRequest) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(ExportLogsServiceResponse.newBuilder().build());
                responseObserver.onCompleted();
            }
        };
    }
}
