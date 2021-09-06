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

public enum MiddlewareTypeEnum {
    /**
     * 默认(空)
     */
    DEFAULT(""),
    /**
     * APP
     */
    APP("APP"),
    /**
     * HTTP
     */
    HTTP("HTTP"),
    /**
     * DUBBO
     */
    DUBBO("DUBBO"),
    /**
     * FEIGN
     */
    FEIGN("FEIGN"),
    /**
     * GRPC
     */
    GRPC("GRPC"),
    /**
     * MYSQL
     */
    MYSQL("MYSQL"),
    /**
     * ORACLE
     */
    ORACLE("ORACLE"),
    /**
     * SQLSERVER
     */
    SQLSERVER("SQLSERVER"),
    /**
     * CASSANDRA
     */
    CASSANDRA("CASSANDRA"),
    /**
     * MONGODB
     */
    MONGODB("MONGODB"),
    /**
     * HBASE
     */
    HBASE("HBASE"),
    /**
     * HESSIAN
     */
    HESSIAN("HESSIAN"),
    /**
     * CACHE
     */
    CACHE("CACHE"),
    /**
     * REDIS
     */
    REDIS("REDIS"),
    /**
     * MEMCACHE
     */
    MEMCACHE("MEMCACHE"),
    /**
     * ROCKETMQ
     */
    ROCKETMQ("ROCKETMQ"),
    /**
     * KAFKA
     */
    KAFKA("KAFKA"),
    /**
     * ACTIVEMQ
     */
    ACTIVEMQ("ACTIVEMQ"),
    /**
     * IBMMQ
     */
    IBMMQ("IBMMQ"),
    /**
     * RABBITMQ
     */
    RABBITMQ("RABBITMQ"),
    /**
     * ES
     */
    ES("ES"),
    /**
     * ELASTIC-JOB
     */
    ELASTICJOB("ELASTICJOB"),
    /**
     * OSS
     */
    OSS("OSS"),
    /**
     * UNKNOWN
     */
    UNKNOWN("UNKNOWN");

    String type;

    MiddlewareTypeEnum(String type) {
        this.type = type;
    }

    public static MiddlewareTypeEnum getNodeType(String middlewareName) {
        if (middlewareName == null || "".equals(middlewareName.trim())) {
            return MiddlewareTypeEnum.UNKNOWN;
        }
        if (middlewareName.toLowerCase().contains("http")) {
            middlewareName = "http";
        }
        switch (middlewareName.toLowerCase()) {
            case "app":
                return MiddlewareTypeEnum.APP;
            case "dubbo":
            case "apache-dubbo":
                return MiddlewareTypeEnum.DUBBO;
            case "feign":
                return MiddlewareTypeEnum.FEIGN;
            case "apache-rocketmq":
            case "rocketmq":
            case "ons":
                return MiddlewareTypeEnum.ROCKETMQ;
            case "apache-kafka":
            case "kafka":
            case "sf-kafka":
                return MiddlewareTypeEnum.KAFKA;
            case "apache-activemq":
            case "activemq":
                return MiddlewareTypeEnum.ACTIVEMQ;
            case "ibmmq":
                return MiddlewareTypeEnum.IBMMQ;
            case "rabbitmq":
                return MiddlewareTypeEnum.RABBITMQ;
            case "hbase":
            case "aliyun-hbase":
                return MiddlewareTypeEnum.HBASE;
            case "hessian":
                return MiddlewareTypeEnum.HESSIAN;
            case "tfs":
                return MiddlewareTypeEnum.OSS;
            case "http":
            case "undertow":
            case "tomcat":
            case "jetty":
            case "jdk-http":
            case "netty-gateway":
            case "webflux":
            case "okhttp":
                return MiddlewareTypeEnum.HTTP;
            case "oss":
                return MiddlewareTypeEnum.OSS;
            case "mysql":
                return MiddlewareTypeEnum.MYSQL;
            case "oracle":
                return MiddlewareTypeEnum.ORACLE;
            case "sqlserver":
                return MiddlewareTypeEnum.SQLSERVER;
            case "cassandra":
                return MiddlewareTypeEnum.CASSANDRA;
            case "mongodb":
                return MiddlewareTypeEnum.MONGODB;
            case "elasticsearch":
                return MiddlewareTypeEnum.ES;
            case "redis":
                return MiddlewareTypeEnum.REDIS;
            case "memcache":
                return MiddlewareTypeEnum.MEMCACHE;
            case "cache":
            case "google-guava":
            case "guava":
            case "caffeine":
                return MiddlewareTypeEnum.CACHE;
            case "search":
                return MiddlewareTypeEnum.ES;
            case "elastic-job":
                return MiddlewareTypeEnum.ELASTICJOB;
            default:
                return buildEnum(middlewareName.toUpperCase());
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static MiddlewareTypeEnum buildEnum(String middlewareName) {
        if (middlewareName.toLowerCase().contains("netty")) {
            return MiddlewareTypeEnum.HTTP;
        }
        if (middlewareName.toLowerCase().contains("jetty")) {
            return MiddlewareTypeEnum.HTTP;
        }
        if (middlewareName.toLowerCase().contains("http")) {
            return MiddlewareTypeEnum.HTTP;
        }
        if (middlewareName.toLowerCase().contains("dubbo")) {
            return MiddlewareTypeEnum.DUBBO;
        }
        if (middlewareName.toLowerCase().contains("feign")) {
            return MiddlewareTypeEnum.FEIGN;
        }
        if (middlewareName.toLowerCase().contains("kafka")) {
            return MiddlewareTypeEnum.KAFKA;
        }
        if (middlewareName.toLowerCase().contains("rocketmq")) {
            return MiddlewareTypeEnum.ROCKETMQ;
        }
        if (middlewareName.toLowerCase().contains("cache")) {
            return MiddlewareTypeEnum.CACHE;
        }
        if (middlewareName.toLowerCase().contains("grpc")) {
            return MiddlewareTypeEnum.GRPC;
        }
        MiddlewareTypeEnum defaultEnum = DEFAULT;
        defaultEnum.setType(middlewareName);
        return defaultEnum;
    }
}
