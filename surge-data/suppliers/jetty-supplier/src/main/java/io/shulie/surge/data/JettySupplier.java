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
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private List<Pair<String, Servlet>> servletMap = Lists.newArrayList();

    private static final String MIN = "MIN";
    private static final String MAX = "MAX";

    public static ArrayList<Integer> registedPort = Lists.newArrayList();


    @Inject
    @Named("jetty.server.ports")
    protected String pradarServerPorts;

    @Inject
    @Named("jetty.server.threads")
    protected int threads;


    @Inject
    private ApiProcessor apiProcessor;

    private int port = 39900;


    /**
     * ???????????????????????????
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
            throw new RuntimeException("??????????????????");
        }
        return parseMap;
    }

    /**
     * ?????????http??????
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        // ?????????????????????
        Map<String, Integer> parsePortRange = parsePort();
        for (int index = parsePortRange.get(MIN); index <= parsePortRange.get(MAX); index++) {
            try {
                port = index;
                server = getServer(port);
                logger.info("current started port is {}", port);
                registedPort.add(port);
            } catch (Throwable e) {
                logger.error("start current port {} catch exception:{},{},next port start {} ", index, e, e.getStackTrace(), index + 1);
                continue;
            }
            // ??????????????????????????????
            break;
        }

        // ?????????????????????
        // apiProcessor.init();
        super.start();

        logger.info("JETTY supplier started success.port is {}", port);
    }

    /**
     * ??????????????????
     *
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        logger.info("jetty???????????????,??????:{}", server.getURI().getPort());
        super.stop();
        server.stop();
    }

    /**
     * ????????????
     *
     * @param port
     * @return
     * @throws Exception
     */
    private Server getServer(int port) throws Exception {
        //?????????????????????200
        //Server server = new Server();
        Server server = new Server(new QueuedThreadPool(threads, threads / 2, new BlockingArrayQueue()));
        server.setStopAtShutdown(true);
        ServerConnector serverConnector = new ServerConnector(server);
        serverConnector.setPort(port);
        server.setConnectors(new Connector[]{serverConnector});

        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setContextPath("/");
        for (Pair<String, Servlet> pair : servletMap) {
            contextHandler.addServlet(new ServletHolder(pair.getValue()), pair.getKey());
        }
        server.setHandler(contextHandler);
        server.start();
        return server;
    }

    /**
     * ??????servlet
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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
