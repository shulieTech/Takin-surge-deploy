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

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

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

    private static Map<String, MiddlewareTypeEnum> types = new HashMap<>();

    static {
        types.put("app", MiddlewareTypeEnum.APP);
        types.put("dubbo", MiddlewareTypeEnum.DUBBO);
        types.put("apache-dubbo", MiddlewareTypeEnum.DUBBO);
        types.put("feign", MiddlewareTypeEnum.FEIGN);
        types.put("apache-rocketmq", MiddlewareTypeEnum.ROCKETMQ);
        types.put("ons", MiddlewareTypeEnum.ROCKETMQ);
        types.put("rocketmq", MiddlewareTypeEnum.ROCKETMQ);
        types.put("apache-kafka", MiddlewareTypeEnum.KAFKA);
        types.put("kafka", MiddlewareTypeEnum.KAFKA);
        types.put("sf-kafka", MiddlewareTypeEnum.KAFKA);
        types.put("apache-activemq", MiddlewareTypeEnum.ACTIVEMQ);
        types.put("activemq", MiddlewareTypeEnum.ACTIVEMQ);
        types.put("ibmmq", MiddlewareTypeEnum.IBMMQ);
        types.put("rabbitmq", MiddlewareTypeEnum.RABBITMQ);
        types.put("hbase", MiddlewareTypeEnum.HBASE);
        types.put("aliyun-hbase", MiddlewareTypeEnum.HBASE);
        types.put("hessian", MiddlewareTypeEnum.HESSIAN);
        types.put("tfs", MiddlewareTypeEnum.OSS);
        types.put("oss", MiddlewareTypeEnum.OSS);
        types.put("http", MiddlewareTypeEnum.HTTP);
        types.put("undertow", MiddlewareTypeEnum.HTTP);
        types.put("tomcat", MiddlewareTypeEnum.HTTP);
        types.put("jetty", MiddlewareTypeEnum.HTTP);
        types.put("jdk-http", MiddlewareTypeEnum.HTTP);
        types.put("netty-gateway", MiddlewareTypeEnum.HTTP);
        types.put("webflux", MiddlewareTypeEnum.HTTP);
        types.put("okhttp", MiddlewareTypeEnum.HTTP);
        types.put("mysql", MiddlewareTypeEnum.MYSQL);
        types.put("oracle", MiddlewareTypeEnum.ORACLE);
        types.put("sqlserver", MiddlewareTypeEnum.SQLSERVER);
        types.put("cassandra", MiddlewareTypeEnum.CASSANDRA);
        types.put("mongodb", MiddlewareTypeEnum.MONGODB);
        types.put("elasticsearch", MiddlewareTypeEnum.ELASTICJOB);
        types.put("redis", MiddlewareTypeEnum.REDIS);
        types.put("memcache", MiddlewareTypeEnum.MEMCACHE);
        types.put("cache", MiddlewareTypeEnum.CACHE);
        types.put("google-guava", MiddlewareTypeEnum.CACHE);
        types.put("guava", MiddlewareTypeEnum.CACHE);
        types.put("caffeine", MiddlewareTypeEnum.CACHE);
        types.put("search", MiddlewareTypeEnum.ES);
        types.put("elastic-job", MiddlewareTypeEnum.ELASTICJOB);
    }

    public static MiddlewareTypeEnum getNodeType(String middlewareName) {
        String name = StringUtils.lowerCase(StringUtils.trim(middlewareName));
        if (StringUtils.isBlank(name)) {
            return MiddlewareTypeEnum.UNKNOWN;
        }
        if (name.indexOf("http") >= 0) {
            middlewareName = "http";
        }
        MiddlewareTypeEnum typeEnum = types.get(middlewareName);
        if (typeEnum != null) {
            return typeEnum;
        }
        return buildEnum(middlewareName.toUpperCase());
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
