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

/**
 * @Author: xingchen
 * @ClassName: CallTypeEnum
 * @Package: io.shulie.surge.data.deploy.pradar.common
 * @Date: 2020/12/320:36
 * @Description:
 */
public enum CallTypeEnum {
    /**
     * call-http
     */
    CALL_HTTP("call-http"),
    /**
     * call-dubbo
     */
    CALL_DUBBO("call-dubbo"),
    /**
     * call-db
     */
    CALL_DB("call-db"),
    /**
     * call-cace
     */
    CAll_CACHE("call-cace"),
    /**
     * call-rocketmq
     */
    CALL_ROCKETMQ("call-rocketmq"),
    /**
     * call-activemq
     */
    CALL_ACTIVEMQ("call-activemq"),
    /**
     * call-kafka
     */
    CALL_KAFKA("call-kafka"),
    /**
     * call-rabbitmq
     */
    CALL_RABBITMQ("call-rabbitmq"),
    /**
     * call-ibmmq
     */
    CALL_IBMMQ("call-ibmmq");

    private String value;

    CallTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

