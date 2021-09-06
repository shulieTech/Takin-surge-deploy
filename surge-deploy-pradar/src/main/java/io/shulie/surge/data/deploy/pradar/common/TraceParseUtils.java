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

package io.shulie.surge.data.deploy.pradar.common;

import com.pamirs.pradar.log.parser.metrics.MetricsBased;
import com.pamirs.pradar.log.parser.trace.LogParseExtends;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.common.utils.TopicFormatUtils;
import io.shulie.surge.data.deploy.pradar.parser.MiddlewareType;
import io.shulie.surge.data.deploy.pradar.parser.PradarLogType;
import io.shulie.surge.data.deploy.pradar.parser.RpcBasedParser;
import io.shulie.surge.data.deploy.pradar.parser.RpcBasedParserFactory;
import io.shulie.surge.data.runtime.common.utils.ApiProcessor;

/**
 * @Author: xingchen
 * @ClassName: TraceParse
 * @Package: io.shulie.surge.data.deploy.pradar.common
 * @Date: 2020/12/322:30
 * @Description:
 */
public class TraceParseUtils {
    /**
     * 通过链路日志，解析调用中的event和callEvent
     *
     * @param rpcBased
     * @return
     */
    //public static MetricsBased parseTrace(RpcBased rpcBased) {
    //    final MetricsBased metricsBased = new MetricsBased();
    //    // 链路入口
    //    String event = "";
    //    // 入口类型
    //    String type = "";
    //    // 是否入口
    //    boolean entry = false;
    //    // 链路调用
    //    String callEvent = "";
    //    // 调用类型
    //    String callType = "";
    //    int logType = rpcBased.getLogType();
    //    int rpcType = rpcBased.getRpcType();
    //    String middlewareName = rpcBased.getMiddlewareName();
    //    // 处理trace日志和http服务端日志
    //    if ((rpcType == MiddlewareType.TYPE_WEB_SERVER && logType == PradarLogType.LOG_TYPE_RPC_SERVER) || PradarLogType.LOG_TYPE_TRACE == logType) {
    //        String serverName = ApiProcessor.merge(rpcBased.getAppName(), rpcBased.getServiceName(), rpcBased.getMethodName());
    //        event = ApiProcessor.parsePath(serverName);
    //        entry = true;
    //        type = "entry-http";
    //    }
    //    // 处理http客户端日志
    //    if (rpcType == MiddlewareType.TYPE_RPC && logType == PradarLogType.LOG_TYPE_RPC_CLIENT
    //            && (middlewareName.toLowerCase().contains("http") || middlewareName.toLowerCase().contains("undertow") ||
    //            middlewareName.toLowerCase().contains("tomcat"))) {
    //        String serverName = io.shulie.surge.data.runtime.common.utils.ApiProcessor.merge(rpcBased.getAppName(), rpcBased.getServiceName(), rpcBased.getMethodName());
    //        callEvent = ApiProcessor.parsePath(serverName);
    //        callType = CallTypeEnum.CALL_HTTP.getValue();
    //    }
    //    // 处理dubbo客户端
    //    if (rpcType == MiddlewareType.TYPE_RPC && logType == PradarLogType.LOG_TYPE_RPC_CLIENT
    //            && ("dubbo".equalsIgnoreCase(middlewareName) || "apache-dubbo".equalsIgnoreCase(middlewareName))) {
    //        callEvent = rpcBased.getServiceName() + "#" + rpcBased.getMethodName();
    //        callType = CallTypeEnum.CALL_DUBBO.getValue();
    //    }
    //    // 处理dubbo服务端
    //    if (rpcType == MiddlewareType.TYPE_RPC && logType == PradarLogType.LOG_TYPE_RPC_SERVER
    //            && ("dubbo".equalsIgnoreCase(middlewareName) || "apache-dubbo".equalsIgnoreCase(middlewareName))) {
    //        event = rpcBased.getServiceName() + "#" + rpcBased.getMethodName();
    //        entry = true;
    //        type = "entry-dubbo";
    //    }
    //    // 处理DB日志
    //    if (rpcType == MiddlewareType.TYPE_DB) {
    //        callEvent = rpcBased.getRemoteIp() + ":" + rpcBased.getPort();
    //        callType = CallTypeEnum.CALL_DB.getValue();
    //    }
    //    // 处理缓存
    //    if (rpcType == MiddlewareType.TYPE_CACHE) {
    //        callType = "call-cache";
    //        callEvent = rpcBased.getRemoteIp() + ":" + rpcBased.getPort();
    //    }
    //    // 处理rocketMQ客户端
    //    if (rpcType == MiddlewareType.TYPE_MQ && logType == PradarLogType.LOG_TYPE_RPC_CLIENT
    //            && ("apache-rocketmq".equalsIgnoreCase(middlewareName) || "rocketmq".equalsIgnoreCase(middlewareName)
    //            || "ons".equalsIgnoreCase(middlewareName))) {
    //        callEvent = TopicFormatUtils.format(rpcBased.getServiceName());
    //        callType = CallTypeEnum.CALL_ROCKETMQ.getValue();
    //    }
    //    // kafka客户端
    //    if (rpcType == MiddlewareType.TYPE_MQ && logType == PradarLogType.LOG_TYPE_RPC_CLIENT
    //            && ("apache-kafka".equalsIgnoreCase(middlewareName) || "kafka".equalsIgnoreCase(middlewareName))) {
    //        callEvent = TopicFormatUtils.format(rpcBased.getServiceName());
    //        callType = CallTypeEnum.CALL_KAFKA.getValue();
    //    }
    //    // activemq 客户端
    //    if (rpcType == MiddlewareType.TYPE_MQ && logType == PradarLogType.LOG_TYPE_RPC_CLIENT
    //            && ("apache-activemq".equalsIgnoreCase(middlewareName) || "activemq".equalsIgnoreCase(middlewareName))) {
    //        callEvent = TopicFormatUtils.format(rpcBased.getServiceName());
    //        callType = CallTypeEnum.CALL_ACTIVEMQ.getValue();
    //    }
    //    // rabbitmq客户端
    //    if (rpcType == MiddlewareType.TYPE_MQ && logType == PradarLogType.LOG_TYPE_RPC_CLIENT
    //            && "rabbitmq".equalsIgnoreCase(middlewareName)) {
    //        callEvent = TopicFormatUtils.format(rpcBased.getServiceName());
    //        callType = CallTypeEnum.CALL_RABBITMQ.getValue();
    //    }
    //    // 处理消费端
    //    if (rpcType == MiddlewareType.TYPE_MQ && logType == PradarLogType.LOG_TYPE_RPC_SERVER) {
    //        event = TopicFormatUtils.format(rpcBased.getServiceName());
    //        if (LogParseExtends.checkMq12Version(rpcBased.getVersion())) {
    //            event = LogParseExtends.getMq12Event(rpcBased);
    //            event = TopicFormatUtils.format(event);
    //        }
    //        entry = true;
    //    }
    //
    //    metricsBased.setType(type);
    //    metricsBased.setEvent(event);
    //    metricsBased.setCallEvent(callEvent);
    //    metricsBased.setEntry(entry);
    //    metricsBased.setCallType(callType);
    //    return metricsBased;
    //}

    ///**
    // * 获取入口服务名称
    // *
    // * @param rpcBased
    // * @return
    // */
    //public static String parseEntranceServiceName(RpcBased rpcBased) {
    //    RpcBasedParser rpcBasedParser = RpcBasedParserFactory.getInstance(rpcBased.getLogType(), rpcBased.getRpcType());
    //    String appName = rpcBased.getAppName();
    //    String service = rpcBased.getServiceName();
    //    /**
    //     * 处理服务端日志信息
    //     * 处理服务端日志
    //     */
    //    int logType = rpcBased.getLogType();
    //    int rpcType = rpcBased.getRpcType();
    //    if (logType == PradarLogType.LOG_TYPE_RPC_SERVER && rpcType == MiddlewareType.TYPE_MQ) {
    //        service = TopicFormatUtils.format(rpcBased.getServiceName());
    //        if (LogParseExtends.checkMq12Version(rpcBased.getVersion())) {
    //            String methodTmp = rpcBasedParser.methodParse(rpcBased);
    //            service = service + "#" + methodTmp;
    //            service = TopicFormatUtils.format(service);
    //        }
    //    }
    //    // 解析HTTP服务和trace日志
    //    else if (rpcType == MiddlewareType.TYPE_WEB_SERVER || PradarLogType.LOG_TYPE_TRACE == logType) {
    //        String serverName = ApiProcessor.merge(appName, rpcBased.getServiceName(), rpcBased.getMethodName());
    //        service = ApiProcessor.parsePath(serverName);
    //    }
    //    // 处理dubbo服务端
    //    else if (rpcType == MiddlewareType.TYPE_RPC && logType == PradarLogType.LOG_TYPE_RPC_SERVER
    //            && ("dubbo".equalsIgnoreCase(rpcBased.getMiddlewareName())
    //            || "apache-dubbo".equalsIgnoreCase(rpcBased.getMiddlewareName()))) {
    //        String method = rpcBased.getMethodName();
    //        if (method.contains("(")) {
    //            method = method.substring(0, method.indexOf("("));
    //        }
    //        service = rpcBased.getServiceName() + "#" + method;
    //    } else if (rpcType == MiddlewareType.TYPE_JOB) {
    //        service = rpcBased.getServiceName();
    //    } else {
    //        service = "";
    //    }
    //    return service;
    //}

    /**
     * 获取入口服务类型
     *
     * @param rpcBased
     * @return
     */
    //public static String parseEntranceServiceType(RpcBased rpcBased) {
    //    int rpcType=rpcBased.getRpcType();
    //    int logType = rpcBased.getLogType();
    //    String middlewareName = rpcBased.getMiddlewareName();
    //    if (logType == PradarLogType.LOG_TYPE_TRACE) {
    //        return WhiteListType.HTTP.getName();
    //    }
    //    if (rpcType == MiddlewareType.TYPE_JOB) {
    //        return WhiteListType.TYPE_LOCAL_JOB.getName();
    //    }
    //    if (rpcType == MiddlewareType.TYPE_RPC && logType == PradarLogType.LOG_TYPE_RPC_SERVER
    //            && "dubbo".equalsIgnoreCase(middlewareName)) {
    //        return WhiteListType.DUBBO.getName();
    //    }
    //    if (rpcType == MiddlewareType.TYPE_MQ && logType == PradarLogType.LOG_TYPE_RPC_SERVER
    //            && ("apache-rocketmq".equalsIgnoreCase(middlewareName)
    //            || "rocketmq".equalsIgnoreCase(middlewareName)
    //            || "ons".equalsIgnoreCase(middlewareName))) {
    //        return WhiteListType.ROCKETMQ.getName();
    //    }
    //    if (rpcType == MiddlewareType.TYPE_MQ && logType == PradarLogType.LOG_TYPE_RPC_SERVER
    //            && ("apache-kafka".equalsIgnoreCase(middlewareName)
    //            || "kafka".equalsIgnoreCase(middlewareName))) {
    //        return WhiteListType.KAFKA.getName();
    //    }
    //    if (rpcType == MiddlewareType.TYPE_MQ && logType == PradarLogType.LOG_TYPE_RPC_SERVER
    //            && ("apache-activemq".equalsIgnoreCase(middlewareName)
    //            || "activemq".equalsIgnoreCase(middlewareName))) {
    //        return WhiteListType.ACTIVEMQ.getName();
    //    }
    //    if (rpcType == MiddlewareType.TYPE_MQ && logType == PradarLogType.LOG_TYPE_RPC_SERVER
    //            && "ibmmq".equalsIgnoreCase(middlewareName)) {
    //        return WhiteListType.IBMMQ.getName();
    //    }
    //    if (rpcType == MiddlewareType.TYPE_MQ && logType == PradarLogType.LOG_TYPE_RPC_SERVER
    //            && "rabbitmq".equalsIgnoreCase(middlewareName)) {
    //        return WhiteListType.RABBITMQ.getName();
    //    }
    //    return "";
    //}
}
