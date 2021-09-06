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


import io.shulie.surge.data.deploy.pradar.parser.MiddlewareType;
import io.shulie.surge.data.deploy.pradar.parser.PradarLogType;

/**
 * 白名单枚举
 *
 * @Author: xingchen
 * @ClassName: WhiteListType
 * @Package: io.shulie.surge.data.common.constant
 * @Date: 2020/11/1917:04
 * @Description:
 */
////TODO P1 remove
//public enum WhiteListType {
//    RABBITMQ("RABBITMQ"),
//    KAFKA("KAFKA"),
//    ROCKETMQ("ROCKETMQ"),
//    ACTIVEMQ("ACTIVEMQ"),
//    IBMMQ("IBMMQ"),
//    HTTP("HTTP"),
//    DUBBO("DUBBO"),
//    TYPE_LOCAL_JOB("LOCAL_JOB");
//
//    /**
//     * 白名单类型
//     */
//    public String name;
//
//    WhiteListType(String name) {
//        this.name = name;
//    }
//
//    /**
//     * @param logType        logType
//     * @param rpcType        rpcType
//     * @param middlewareName
//     * @return
//     */
//    public static String getWhiteListByType(int logType, int rpcType, String middlewareName) {
//        if (logType == PradarLogType.LOG_TYPE_TRACE) {
//            return WhiteListType.HTTP.getName();
//        }
//        if (rpcType == MiddlewareType.TYPE_JOB) {
//            return WhiteListType.TYPE_LOCAL_JOB.getName();
//        }
//        if (rpcType == MiddlewareType.TYPE_WEB_SERVER && logType == PradarLogType.LOG_TYPE_RPC_SERVER) {
//            return WhiteListType.HTTP.getName();
//        }
//        if (rpcType == MiddlewareType.TYPE_RPC && logType == PradarLogType.LOG_TYPE_RPC_SERVER
//                && "dubbo".equalsIgnoreCase(middlewareName)) {
//            return WhiteListType.DUBBO.getName();
//        }
//        if (rpcType == MiddlewareType.TYPE_MQ && logType == PradarLogType.LOG_TYPE_RPC_SERVER
//                && ("apache-rocketmq".equalsIgnoreCase(middlewareName)
//                || "rocketmq".equalsIgnoreCase(middlewareName)
//                || "ons".equalsIgnoreCase(middlewareName))) {
//            return WhiteListType.ROCKETMQ.getName();
//        }
//        if (rpcType == MiddlewareType.TYPE_MQ && logType == PradarLogType.LOG_TYPE_RPC_SERVER
//                && ("apache-kafka".equalsIgnoreCase(middlewareName)
//                || "kafka".equalsIgnoreCase(middlewareName))) {
//            return WhiteListType.KAFKA.getName();
//        }
//        if (rpcType == MiddlewareType.TYPE_MQ && logType == PradarLogType.LOG_TYPE_RPC_SERVER
//                && ("apache-activemq".equalsIgnoreCase(middlewareName)
//                || "activemq".equalsIgnoreCase(middlewareName))) {
//            return WhiteListType.ACTIVEMQ.getName();
//        }
//        if (rpcType == MiddlewareType.TYPE_MQ && logType == PradarLogType.LOG_TYPE_RPC_SERVER
//                && "ibmmq".equalsIgnoreCase(middlewareName)) {
//            return WhiteListType.IBMMQ.getName();
//        }
//        if (rpcType == MiddlewareType.TYPE_MQ && logType == PradarLogType.LOG_TYPE_RPC_SERVER
//                && "rabbitmq".equalsIgnoreCase(middlewareName)) {
//            return WhiteListType.RABBITMQ.getName();
//        }
//        return "";
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//}
