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

package io.shulie.surge.data.suppliers.kafka;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.shulie.surge.data.common.lifecycle.LifecycleObserver;
import io.shulie.surge.data.runtime.common.ObjectSerializer;
import io.shulie.surge.data.runtime.common.ObjectSerializerFactory;
import io.shulie.surge.data.runtime.common.utils.ApiProcessor;
import io.shulie.surge.data.runtime.supplier.DefaultSupplier;
import io.shulie.surge.data.runtime.supplier.Supplier;
import io.shulie.takin.sdk.kafka.entity.MessageEntity;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * @author vincent
 */
public final class KafkaSupplier extends DefaultSupplier {

    private static final Logger logger = LoggerFactory.getLogger(KafkaSupplier.class);
    private static final long POLL_TIMEOUT = 200l;

    @Inject
    private ApiProcessor apiProcessor;

    private Thread messageFetcher;

    private String bootstrap;

    private String topic;

    private KafkaConsumer consumer;

    public KafkaSupplier(String bootstrap, String topic) {
        this.bootstrap = bootstrap;
        this.topic = topic;
    }

    /**
     * 开始获取数据
     *
     * @throws IllegalStateException Queue 尚未设置时抛出此异常
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        //启动
        super.start();
        apiProcessor.init();

        Properties properties = new Properties();
        properties.put("bootstrap.servers", bootstrap);
        properties.put("group.id", "KafkaAggregationReceiver");
        properties.put("enable.auto.commit", "true");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

        consumer = new KafkaConsumer<String, byte[]>(properties);
        consumer.subscribe(Lists.newArrayList(topic.split(",")));
        messageFetcher = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        /**
                         * 指定超时时间，通常情况下consumer拿到了足够多的可用数据，会立即从该方法返回，但若当前没有足够多数据
                         * consumer会处于阻塞状态，但当到达设定的超时时间，则无论数据是否足够都为立即返回
                         */
                        ConsumerRecords<String, byte[]> records = consumer.poll(POLL_TIMEOUT);
                        Iterator<ConsumerRecord<String, byte[]>> iterator = records.iterator();
                        while (iterator.hasNext()) {
                            ConsumerRecord<String, byte[]> record = iterator.next();
                            byte[] value = record.value();
                            ObjectSerializer objectSerializer = ObjectSerializerFactory.getObjectSerializer("thrift");
                            MessageEntity messageEntity = objectSerializer.deserialize(value);
                            if (messageEntity != null) {
                                Map<String, Object> header = messageEntity.getHeaders();
                                String message = null;
                                if (MapUtils.isNotEmpty(messageEntity.getBody()) && messageEntity.getBody().containsKey("content")) {
                                    message = ObjectUtils.toString(messageEntity.getBody().get("content"));
                                }
                                header.put("dataVersion", header.get("version"));
                                header.put("receiveHttpTime", System.currentTimeMillis());
                                queue.publish(header, message);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    logger.error("Publish message error.");
                } finally {
                    /**
                     * consumer程序结束后一定要显示关闭consumer以释放KafkaConuser运行过程中占用的各种系统资源
                     * KafkaConsumer.close()：关闭consumer并等待30秒
                     * KafkaConsumer.close(timeout): 关闭consumer并最多等待给定的timeout秒
                     */
                    consumer.close();
                }
            }
        });
        messageFetcher.setDaemon(true);
        messageFetcher.start();
    }


    /**
     * 停止获取数据
     *
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        super.stop();
    }

    /**
     * 检查当前是否在运行状态
     */
    @Override
    public boolean isRunning() {
        return super.isRunning();
    }


    @Override
    public void addObserver(LifecycleObserver<Supplier> observer) {
        super.addObserver(observer);
    }

    public KafkaConsumer getConsumer() {
        return consumer;
    }
}
