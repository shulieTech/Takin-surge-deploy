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

package io.shulie.surge.data.suppliers.nettyremoting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.pamirs.pradar.log.parser.packet.Request;
import com.pamirs.pradar.log.parser.packet.Response;
import com.pamirs.pradar.remoting.RemotingServer;
import com.pamirs.pradar.remoting.netty.NettyCommandProcessor;
import com.pamirs.pradar.remoting.netty.NettyRemotingServer;
import com.pamirs.pradar.remoting.netty.NettyServerConfigurator;
import com.pamirs.pradar.remoting.protocol.*;
import com.pamirs.pradar.remoting.utils.RemotingThreadFactory;
import io.netty.channel.ChannelHandlerContext;
import io.shulie.surge.data.common.lifecycle.LifecycleObserver;
import io.shulie.surge.data.common.pool.DataPoolExecutors;
import io.shulie.surge.data.common.utils.IpAddressUtils;
import io.shulie.surge.data.common.zk.ZkClient;
import io.shulie.surge.data.runtime.common.utils.ApiProcessor;
import io.shulie.surge.data.runtime.disruptor.RingBufferIllegalStateException;
import io.shulie.surge.data.runtime.processor.DataQueue;
import io.shulie.surge.data.runtime.supplier.DefaultMultiProcessorSupplier;
import io.shulie.surge.data.runtime.supplier.Supplier;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author vincent
 */
public final class NettyRemotingSupplier extends DefaultMultiProcessorSupplier {

    private static final Logger logger = LoggerFactory.getLogger(NettyRemotingSupplier.class);

    private int port;
    private static final String MIN = "MIN";
    private static final String MAX = "MAX";

    @Inject
    private ApiProcessor apiProcessor;

    @Inject
    @Named("netty.remoting.server.ports")
    protected String pradarServerPorts;

    @Inject
    @Named("netty.remoting.server.processCores")
    private int coreSize = 4;

    private RemotingServer remotingServer;

    // 外部设置的端口段映射
    private Map<String, String> inputPortMap = Maps.newHashMap();
    private String work = "";

    @Inject
    @Named("config.log.pradar.server")
    protected transient String pradarServerPath;

    @Inject
    private ZkClient zkClient;

    /**
     * 开始获取数据
     *
     * @throws IllegalStateException Queue 尚未设置时抛出此异常
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        // 获取启动的端口
        Map<String, Integer> parsePortRange = parsePort();
        if ("k8s".equals(work)) {
            startK8s(parsePortRange);
        } else {
            for (int index = parsePortRange.get(MIN); index <= parsePortRange.get(MAX); index++) {
                try {
                    port = index;
                    remotingServer = getRemotingServer(port);
                } catch (Throwable e) {
                    logger.error("next port start " + index);
                    continue;
                }
                // 启动成功以后就停止掉
                break;
            }
        }
        super.start();
        apiProcessor.init();
    }

    /**
     * k8s里面,多个容器占用的端口是一样的,端口占用不会报错,尽量不要有相同端口
     *
     * @param parsePortRange
     */
    private void startK8s(Map<String, Integer> parsePortRange) {
        for (int index = parsePortRange.get(MIN); index <= parsePortRange.get(MAX); index++) {
            try {
                TimeUnit.SECONDS.sleep(RandomUtils.nextInt(1, 10));
                List<String> children = zkClient.listChildren(pradarServerPath);
                List<String> ports = Lists.newArrayList();
                if (CollectionUtils.isNotEmpty(children)) {
                    ports = children.stream().map(sub -> sub.split(":")[1]).collect(Collectors.toList());
                }
                port = index;
                // 如果已经存在此端口了,继续下一个端口
                if (ports.contains(String.valueOf(port))) {
                    continue;
                }
                // 判断zk里面是否存在
                remotingServer = getRemotingServer(port);
            } catch (Throwable e) {
                logger.error("next port start " + index);
                continue;
            }
            // 启动成功以后就停止掉
            break;
        }
    }

    /**
     * 初始化remoting server
     *
     * @return
     */
    private RemotingServer getRemotingServer(int port) {
        ProtocolFactorySelector selector = new DefaultProtocolFactorySelector();
        NettyServerConfigurator config = new NettyServerConfigurator();
        config.setListenPort(port);
        config.setServerSocketRcvBufSize(32 * 1024);
        config.setServerSocketSndBufSize(32 * 1024);
        ProtocolFactorySelector protocolFactorySelector = new DefaultProtocolFactorySelector();
        RemotingServer remotingServer = new NettyRemotingServer(protocolFactorySelector, config);
        remotingServer.registerDefaultProcessor(new NettyCommandProcessor() {
            @Override
            public RemotingCommand processCommand(ChannelHandlerContext ctx, RemotingCommand req) {
                long receiveHttpTime = System.currentTimeMillis();
                RemotingCommand responseCommand = new RemotingCommand();
                /**
                 * 设置response
                 */
                responseCommand.setCode(CommandCode.SUCCESS);
                responseCommand.setVersion(req.getVersion());
                responseCommand.setProtocolCode(ProtocolCode.KRYO);

                Response response = new Response();
                response.setSuccess(true);
                ProtocolFactory factory = selector.select(req.getProtocolCode());
                try {
                    Request request = factory.decode(Request.class, req);
                    if (request == null) {
                        logger.warn("agent push version is error " + req.toString());
                        responseCommand.setVersion(CommandVersion.V1);
                        return responseCommand;
                    }
                    String content = new String(request.getBody());
                    String hostIp = request.getHostIp();
                    String dataVersion = request.getVersion();
                    Byte dataType = request.getDataType();
                    DataQueue queue = queueMap.get(String.valueOf(dataType));
                    Map<String, Object> header = Maps.newHashMap();
                    header.put("hostIp", hostIp);
                    header.put("dataVersion", dataVersion);
                    header.put("dataType", dataType);
                    header.put("receiveHttpTime", receiveHttpTime);
                    queue.publish(header, queue.splitLog(content, dataType));
                } catch (RingBufferIllegalStateException e) {
                    logger.error(e.getMessage());
                    response.setSuccess(false);
                    response.setErrorMsg(e.getMessage());
                    responseCommand.setCode(CommandCode.SYSTEM_BUSY);
                } catch (Throwable e) {
                    logger.error("logProcessor fail " + ExceptionUtils.getStackTrace(e));
                    response.setSuccess(false);
                    response.setErrorMsg(e.getMessage());
                    responseCommand.setCode(CommandCode.SYSTEM_ERROR);
                }
                return responseCommand;
            }

            /**
             * 拒绝
             *
             * @param ctx
             * @param request
             * @return
             */
            @Override
            public boolean reject(ChannelHandlerContext ctx, RemotingCommand request) {
                try {
                    for (DataQueue dataQueue : queueMap.values()) {
                        dataQueue.canPublish(1000);
                    }
                    return false;
                } catch (RingBufferIllegalStateException e) {
                    logger.error(e.getMessage());
                    return true;
                }
            }
        }, Executors.newCachedThreadPool());
        remotingServer.start();
        return remotingServer;
    }

    private ExecutorService getDefaultExecutors(int coreSize, AtomicBoolean rejector) {
        return DataPoolExecutors.newDefaultNoQueueExecutors(coreSize, coreSize * 2, 3, TimeUnit.SECONDS, RemotingThreadFactory.
                newThreadFactory("RemotingServerProcessThreadPoolExecutor-%d", false), new NettyRejectPolicy(rejector));
    }

    public class NettyRejectPolicy implements RejectedExecutionHandler {
        private AtomicBoolean rejector;

        public NettyRejectPolicy(AtomicBoolean rejector) {
            this.rejector = rejector;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            rejector.set(true);
        }
    }

    /**
     * 停止获取数据
     *
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        super.stop();
        remotingServer.shutdown();
    }

    /**
     * 检查当前是否在运行状态
     */
    @Override
    public boolean isRunning() {
        return super.isRunning();
    }

    /**
     * 获取开放的端口信息
     *
     * @return
     */
    private Map<String, Integer> parsePort() {
        Map<String, Integer> parseMap = Maps.newHashMap();
        try {
            String host = IpAddressUtils.getLocalAddress();
            String rangeStr = inputPortMap != null ? inputPortMap.getOrDefault(host, pradarServerPorts) : pradarServerPorts;
            rangeStr = rangeStr.substring(rangeStr.indexOf("[") + 1, rangeStr.lastIndexOf("]"));
            logger.info("当前主机ip为 " + host + "发布的端口段为 " + rangeStr);
            String[] rangeSplit = rangeStr.split(",");
            parseMap.put(MIN, Integer.parseInt(rangeSplit[0]));
            parseMap.put(MAX, Integer.parseInt(rangeSplit[1]));
        } catch (Throwable e) {
            logger.error("parse port fail" + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("解析端口失败");
        }
        return parseMap;
    }


    @Override
    public void addObserver(LifecycleObserver<Supplier> observer) {
        super.addObserver(observer);
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setInputPortMap(Map<String, String> inputPortMap) {
        this.inputPortMap = inputPortMap;
    }

    public void setWork(String work) {
        this.work = work;
    }
}
