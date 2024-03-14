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

package io.shulie.surge.data.sink.kafka;


import org.apache.commons.lang3.exception.ExceptionUtils;;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于 RocketMQSupport
 *
 * @author xingchen
 */
public class DefaultKafkaSupport implements KafkaSupport {
    private static final Logger logger = LoggerFactory.getLogger(DefaultKafkaSupport.class);

    private KafkaProducer<String,String> kafkaProducer;

    public DefaultKafkaSupport(String nameSrv, String producerGroup)  {
        try {
            Map<String, Object> configs = new HashMap<>();
            configs.put("bootstrap.servers", nameSrv);
            configs.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            configs.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            configs.put("acks","0");
            configs.put("retries",0);
            configs.put("batch.size",5120);
            kafkaProducer = new KafkaProducer<>(configs);
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
     * @throws Exception
     */
    @Override
    public void sendMq(String topic, String key, String msg) throws Exception {
        this.kafkaProducer.send(new ProducerRecord<String, String>(topic, key, msg), new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if(e != null) {
                    logger.error("send kafka message failure", e);
                }
            }
        });
    }

    /**
     * 单向发送
     *
     * @param topic
     * @param msg   发送消息
     * @throws Exception
     */
    @Override
    public void sendOneWay(String topic, String key, String msg) throws Exception {
        this.kafkaProducer.send(new ProducerRecord<String,String>(topic, key, msg));
    }

    /**
     * 停止
     */
    @Override
    public void stop() {
        try {
            this.kafkaProducer.close();
        } catch (Throwable e) {
            logger.error("close mqProducer fail" + ExceptionUtils.getStackTrace(e));
        }
    }
}
