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

package io.shulie.surge.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.shulie.surge.data.common.lifecycle.LifecycleObserver;
import io.shulie.surge.data.runtime.common.utils.ApiProcessor;
import io.shulie.surge.data.runtime.supplier.DefaultMultiProcessorSupplier;
import io.shulie.surge.data.runtime.supplier.Supplier;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: xingchen
 * @ClassName: JettySupplier
 * @Package: io.shulie.surge.data
 * @Date: 2020/11/1610:59
 * @Description:
 */
public class JettySupplier extends DefaultMultiProcessorSupplier {
    private static Logger logger = LoggerFactory.getLogger(JettySupplier.class);
    private Server server;
    private ServletContextHandler contextHandler = new ServletContextHandler();

    private List<Pair<String, Servlet>> servletMap = Lists.newArrayList();

    private static final String MIN = "MIN";
    private static final String MAX = "MAX";


    @Inject
    @Named("jetty.server.ports")
    protected String pradarServerPorts;


    @Inject
    private ApiProcessor apiProcessor;


    /**
     * 获取开放的端口信息
     *
     * @return
     */
    private Map<String, Integer> parsePort() {
        Map<String, Integer> parseMap = Maps.newHashMap();
        try {
            String rangeStr = pradarServerPorts.substring(pradarServerPorts.indexOf("[") + 1, pradarServerPorts.lastIndexOf("]"));
            String[] rangeSplit = rangeStr.split(",");
            parseMap.put(MIN, Integer.parseInt(rangeSplit[0]));
            parseMap.put(MAX, Integer.parseInt(rangeSplit[1]));
        } catch (Throwable e) {
            logger.error("parse port fail");
            throw new RuntimeException("解析端口失败");
        }
        return parseMap;
    }

    /**
     * 初始化http服务
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        // 获取启动的端口
        Map<String, Integer> parsePortRange = parsePort();
        for (int index = parsePortRange.get(MIN); index <= parsePortRange.get(MAX); index++) {
            try {
                int port = index;
                server = getServer(port);
            } catch (Throwable e) {
                logger.error("next port start " + index);
                continue;
            }
            // 启动成功以后就停止掉
            break;
        }


        // 初始化聚合接口
        apiProcessor.init();
        super.start();

        logger.info("JETTY服务启动成功");
    }

    /**
     * 获取服务
     * @param port
     * @return
     * @throws Exception
     */
    private Server getServer(int port) throws Exception {
        Server server = new Server();
        server.setStopAtShutdown(true);
        ServerConnector serverConnector = new ServerConnector(server);
        serverConnector.setPort(port);
        server.setConnectors(new Connector[]{serverConnector});
        server.setHandler(contextHandler);
        contextHandler.setContextPath("/");
        for (Pair<String, Servlet> pair : servletMap) {
            contextHandler.addServlet(new ServletHolder(pair.getValue()), pair.getKey());
        }
        server.start();
        return server;
    }

    /**
     * 添加servlet
     * @param path
     * @param servlet
     */
    public void addServlet(String path, HttpServlet servlet) {
        servletMap.add(Pair.of(path, servlet));
    }

    @Override
    public void addObserver(LifecycleObserver<Supplier> observer) {
        super.addObserver(observer);
    }
}
