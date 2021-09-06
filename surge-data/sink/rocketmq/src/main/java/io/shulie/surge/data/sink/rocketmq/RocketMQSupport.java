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

package io.shulie.surge.data.sink.rocketmq;


import com.google.inject.ImplementedBy;
import io.shulie.surge.data.common.lifecycle.Stoppable;
import org.apache.rocketmq.client.producer.SendCallback;

/**
 * 封装 InfluxDB 的服务的写实现
 *
 * @author xingchen
 */
@ImplementedBy(DefaultRocketMQSupport.class)
public interface RocketMQSupport extends Stoppable {
    /**
     * 发送消息
     *
     * @param msg 发送消息
     */
    void sendMq(String topic, String tag, String key, String msg, SendCallback sendCallback) throws Exception;

    /**
     * 单向发送
     *
     * @param msg 发送消息
     */
    void sendOneWay(String topic, String tag, String key, String msg) throws Exception;
}
