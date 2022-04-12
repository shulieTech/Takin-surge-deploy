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

package io.shulie.surge.data.deploy.pradar.report;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;

import io.shulie.surge.data.common.utils.IpAddressUtils;
import io.shulie.surge.data.runtime.common.DataRuntime;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import io.shulie.surge.data.sink.redis.RedisSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.tuple.Values;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import static io.shulie.surge.data.deploy.pradar.report.PradarReportTaskTopology.ACTIVITY_STREAM_ID;
import static io.shulie.surge.data.deploy.pradar.report.ReportActivityBolt.REDIS_REPORT_PREFIX;

public class ReportTaskServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportTaskServer.class);

    private static final String REPORT_TASK_PATH = "/report/start";
    private static final ServletContextHandler contextHandler = new ServletContextHandler();

    private final int port;
    private final SpoutOutputCollector collector;
    private final DataRuntime dataRuntime;
    private final Map<String, String> netMap;
    private final Map<String, String> hostNameMap;
    private final RedisSupport redisSupport;

    public ReportTaskServer(ReportTaskServerSpec serverSpec) {
        this.port = serverSpec.getPort();
        this.collector = serverSpec.getCollector();
        this.dataRuntime = serverSpec.getDataRuntime();
        this.redisSupport = dataRuntime.getInstance(RedisSupport.class);
        this.netMap = serverSpec.getNetMap();
        this.hostNameMap = serverSpec.getHostNameMap();
    }

    public void startServer() throws Exception {
        Server server = new Server();
        server.setStopAtShutdown(true);
        ServerConnector serverConnector = new ServerConnector(server);
        serverConnector.setPort(port);
        server.setConnectors(new Connector[] {serverConnector});
        server.setHandler(contextHandler);
        contextHandler.setContextPath("/");
        contextHandler.addServlet(new ServletHolder(new ReportTaskServlet(collector, dataRuntime)), REPORT_TASK_PATH);
        server.start();
        registerUrlToRedis();
    }

    private void registerUrlToRedis() {
        String host = parseHostName();
        if (!CollectionUtils.isEmpty(netMap) && netMap.containsKey(host)) {
            host = netMap.get(host);
        }
        String url = "http://" + host + ":" + port + REPORT_TASK_PATH;
        redisSupport.setString(REDIS_REPORT_PREFIX + "url", url);
    }

    private String parseHostName() {
        // 获取当前系统名
        String osName = IpAddressUtils.getLocalHostName();
        String host = IpAddressUtils.getLocalAddress();
        if (CollectionUtils.isEmpty(hostNameMap) || !hostNameMap.containsKey(osName)) {
            return host;
        }
        return hostNameMap.get(osName);
    }

    public static class ReportTaskServlet extends HttpServlet {

        private final SpoutOutputCollector collector;
        private final MysqlSupport mysqlSupport;
        private final RedisSupport redisSupport;

        public ReportTaskServlet(SpoutOutputCollector collector, DataRuntime dataRuntime) {
            this.collector = collector;
            this.mysqlSupport = dataRuntime.getInstance(MysqlSupport.class);
            this.redisSupport = dataRuntime.getInstance(RedisSupport.class);
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType("application/json");
            String reportId = req.getParameter("reportId");
            if (StringUtils.isBlank(reportId)) {
                resp.getOutputStream().write(JSON.toJSONBytes(Response.fail("reportId参数不能为空!")));
                return;
            }
            if (reportProceed(reportId)) {
                resp.getOutputStream().write(JSON.toJSONBytes(Response.fail("压测报告[" + reportId + "]已处理!")));
                return;
            }
            LOGGER.info("启动压测报告[{}]分析任务", reportId);
            collector.emit(ACTIVITY_STREAM_ID, new Values(reportId));
            resp.getOutputStream().write(JSON.toJSONBytes(Response.SUCCESS));
        }

        private boolean reportProceed(String reportId) {
            if (redisSupport.hasKey(redisKey(reportId))) {
                return true;
            }
            List<String> ids = mysqlSupport.queryForList(String.format(ReportActivityModel.EXISTS_SQL, reportId),
                String.class);
            if (!CollectionUtils.isEmpty(ids)) {
                return true;
            }
            Boolean notExists = redisSupport.setNXString(redisKey(reportId), "1", 1, TimeUnit.HOURS);
            return notExists != null && !notExists;
        }

        public static String redisKey(String reportId) {
            return REDIS_REPORT_PREFIX + "processing:" + reportId;
        }
    }

    private static class Response implements Serializable {

        private final Boolean data;
        private final Boolean success;
        private String message;

        public Response(Boolean data) {
            this.data = data;
            this.success = data;
        }

        public Response(Boolean data, String message) {
            this.data = data;
            this.success = data;
            this.message = message;
        }

        public static Response SUCCESS = new Response(true);

        public static Response fail(String message) {
            return new Response(false, message);
        }

        public Boolean getData() {
            return data;
        }

        public String getMessage() {
            return message;
        }

        public Boolean getSuccess() {
            return success;
        }
    }
}
