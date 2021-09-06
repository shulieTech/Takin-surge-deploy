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


import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于 RocketMQSupport
 *
 * @author xingchen
 */
public class DefaultRocketMQSupport implements RocketMQSupport {
    private static final Logger logger = LoggerFactory.getLogger(DefaultRocketMQSupport.class);

    private DefaultMQProducer defaultMQProducer;

    public DefaultRocketMQSupport(String nameSrv, String producerGroup) throws MQClientException {
        try {
            defaultMQProducer = new DefaultMQProducer(producerGroup);
            defaultMQProducer.setNamesrvAddr(nameSrv);
            defaultMQProducer.setVipChannelEnabled(false);
            defaultMQProducer.setInstanceName(String.valueOf(System.currentTimeMillis()));
            defaultMQProducer.start();
        } catch (Throwable e) {
            logger.error("rocketmq init fail");
            throw e;
        }
    }

    /**
     * 发送消息
     *
     * @param topic
     * @param msg          发送消息
     * @param sendCallback
     * @throws Exception
     */
    @Override
    public void sendMq(String topic, String tag, String key, String msg, SendCallback sendCallback) throws Exception {
        this.defaultMQProducer.send(new Message(topic, tag, key, msg.getBytes(RemotingHelper.DEFAULT_CHARSET)), sendCallback);
    }

    /**
     * 单向发送
     *
     * @param topic
     * @param tag
     * @param msg   发送消息
     * @throws Exception
     */
    @Override
    public void sendOneWay(String topic, String tag, String key, String msg) throws Exception {
        this.defaultMQProducer.sendOneway(new Message(topic, tag, key, msg.getBytes(RemotingHelper.DEFAULT_CHARSET)));
    }

    /**
     * 停止
     */
    @Override
    public void stop() {
        try {
            this.defaultMQProducer.shutdown();
        } catch (Throwable e) {
            logger.error("close mqProducer fail" + ExceptionUtils.getStackTrace(e));
        }
    }
}
