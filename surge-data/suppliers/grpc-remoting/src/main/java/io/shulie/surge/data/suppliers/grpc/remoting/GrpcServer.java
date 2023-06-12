package io.shulie.surge.data.suppliers.grpc.remoting;

import io.grpc.DecompressorRegistry;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.shulie.surge.data.common.pool.NamedThreadFactory;
import io.shulie.surge.data.suppliers.grpc.remoting.metrics.MetricsAcceptorProto;
import io.shulie.surge.data.suppliers.grpc.remoting.node.NodeAcceptorProto;
import io.shulie.surge.data.suppliers.grpc.remoting.service.MetricsServiceImpl;
import io.shulie.surge.data.suppliers.grpc.remoting.service.NodeServiceImpl;
import io.shulie.surge.data.suppliers.grpc.remoting.service.TracesServiceImpl;
import io.shulie.surge.data.suppliers.grpc.remoting.trace.TraceAcceptorProto;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.*;

/**
 * @author angju
 * @date 2022/9/2 11:02
 */
@Slf4j
public class GrpcServer {

    private Server server;
    private GrpcServerConfig grpcServerConfig;

    private int threadPoolSize = Runtime.getRuntime().availableProcessors() * 4;
    private int threadPoolQueueSize = 10000;

    private TraceAcceptorProto traceAcceptorProto;
    private MetricsAcceptorProto metricsAcceptorProto;
    private NodeAcceptorProto nodeAcceptorProto;

    public GrpcServer(GrpcServerConfig config, TraceAcceptorProto traceAcceptorProto,
                      MetricsAcceptorProto metricsAcceptorProto, NodeAcceptorProto nodeAcceptorProto) {
        this.grpcServerConfig = config;
        this.traceAcceptorProto = traceAcceptorProto;
        this.metricsAcceptorProto = metricsAcceptorProto;
        this.nodeAcceptorProto = nodeAcceptorProto;
    }

    public void start() throws IOException {
        /* The port on which the server should run */

        InetSocketAddress address = new InetSocketAddress(grpcServerConfig.getGrpcServerPort());
        ArrayBlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(threadPoolQueueSize);

        ExecutorService executor = new ThreadPoolExecutor(
                threadPoolSize, threadPoolSize, 60, TimeUnit.SECONDS, blockingQueue,
                new NamedThreadFactory("grpcServerPool"), new CustomRejectedExecutionHandler()
        );
        server = NettyServerBuilder.forAddress(address)
                .maxConcurrentCallsPerConnection(grpcServerConfig.getMaxConcurrentCalls())
                .decompressorRegistry(DecompressorRegistry.getDefaultInstance())
                .addService(new MetricsServiceImpl(metricsAcceptorProto))
                .addService(new TracesServiceImpl(traceAcceptorProto, grpcServerConfig.isSkipErrorLog(), grpcServerConfig.isSkipSilentLog()))
                .addService(new NodeServiceImpl(nodeAcceptorProto))
                // 接受体最大大小200M
                .maxInboundMessageSize(200 * 1024 * 2024)
                .executor(executor)
                .build()
                .start();
        log.info("GrpcServer Server started, listening on " + grpcServerConfig.getGrpcServerPort());


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            log.info("*** shutting down gRPC server since JVM is shutting down");
            try {
                stop();
            } catch (InterruptedException e) {
                log.error("shutting down gRPC server InterruptedException");
            }
            log.info("*** server shut down");
        }));
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    static class CustomRejectedExecutionHandler implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.warn("Grpc server thread pool is full, rejecting the task");
        }
    }

}
