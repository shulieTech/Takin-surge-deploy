/*
 * Copyright 2021 Shulie Technology, Co.Ltd
 * Email: shulie@shulie.io
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.shulie.surge.data.suppliers.grpc.remoting;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.pamirs.pradar.log.parser.DataType;
import io.shulie.surge.data.common.lifecycle.LifecycleObserver;
import io.shulie.surge.data.runtime.supplier.DefaultMultiProcessorSupplier;
import io.shulie.surge.data.runtime.supplier.Supplier;
import io.shulie.surge.data.suppliers.grpc.remoting.services.MetricsAcceptorProtoImpl;
import io.shulie.surge.data.suppliers.grpc.remoting.services.NodeAcceptorProtoImpl;
import io.shulie.surge.data.suppliers.grpc.remoting.services.TraceAcceptorProtoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * @Author: xingchen
 * @ClassName: JettySupplier
 * @Package: io.shulie.surge.data
 * @Date: 2020/11/1610:59
 * @Description:
 */
public class GrpcSupplier extends DefaultMultiProcessorSupplier {
    private static Logger logger = LoggerFactory.getLogger(GrpcSupplier.class);
    private GrpcServer server;

    public static ArrayList<Integer> registedPort = Lists.newArrayList();

    /**
     * Grpc启动端口
     */
    @Inject
    @Named("grpc.server.port")
    private Integer grpcServerPort = 21990;
    /**
     * Grpc最大并行数
     */
    @Inject
    @Named("grpc.server.maxConcurrentCalls")
    private Integer maxConcurrentCalls = Math.max(4, Runtime.getRuntime().availableProcessors());

    /**
     * 跳过异常日志
     */
    @Inject
    @Named("takin.acceptor.grpc.skipErrorLog")
    private Boolean skipErrorLog = false;

    /**
     * 跳过静默日志
     */
    @Inject
    @Named("takin.acceptor.grpc.skipSilentLog")
    private Boolean skipSilentLog = false;


    private int port = 39900;

    private int maxPort = 65535;

    /**
     * 初始化http服务
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        // 获取启动的端口
        this.port = grpcServerPort;
        for (int index = port; index <= maxPort; index++) {
            try {
                port = index;
                server = createServerAndStart(port);
                logger.info("current started port is {}", port);
                registedPort.add(port);
            } catch (Throwable e) {
                logger.error("start current port {} catch exception:{},{},next port start {} ", index, e, e.getStackTrace(), index + 1);
                continue;
            }
            // 启动成功以后就停止掉
            break;
        }
        super.start();

        logger.info("JETTY supplier started success.port is {}", port);
    }

    /**
     * 停止获取数据
     *
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    private GrpcServerConfig buildGrpcServerConfig(int port) {
        GrpcServerConfig grpcServerConfig = new GrpcServerConfig();
        grpcServerConfig.setSkipErrorLog(skipErrorLog);
        grpcServerConfig.setSkipSilentLog(skipSilentLog);
        grpcServerConfig.setGrpcServerPort(port);
        grpcServerConfig.setMaxConcurrentCalls(maxConcurrentCalls);
        return grpcServerConfig;
    }

    /**
     * 获取服务
     *
     * @param port
     * @return
     * @throws Exception
     */
    private GrpcServer createServerAndStart(int port) throws Exception {
        GrpcServerConfig grpcServerConfig = buildGrpcServerConfig(port);
        this.server = new GrpcServer(
                grpcServerConfig,
                new TraceAcceptorProtoImpl(queueMap.get(String.valueOf(DataType.TRACE_LOG))),
                new MetricsAcceptorProtoImpl(),
                new NodeAcceptorProtoImpl(queueMap.get(String.valueOf(DataType.NODE_LOG)))

        );
        this.server.start();
        return server;
    }

    @Override
    public void addObserver(LifecycleObserver<Supplier> observer) {
        super.addObserver(observer);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
