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
import io.shulie.takin.sdk.kafka.DataType;
import io.shulie.takin.sdk.kafka.entity.MessageEntity;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author vincent
 */
public final class KafkaSupplier extends DefaultSupplier {

    private static final Logger logger = LoggerFactory.getLogger(KafkaSupplier.class);
    private static final long POLL_TIMEOUT = 300L;

    @Inject
    private ApiProcessor apiProcessor;

    private ExecutorService messageFetcher;

    private String bootstrap;
    private String kafkaAuthFlag;
    private String securityProtocol;
    private String saslMechanism;
    private String saslJaasConfig;

    private String topic;

    private KafkaConsumer consumer;

    public KafkaSupplier(String bootstrap, String topic, String kafkaAuthFlag, String securityProtocol,
                         String saslMechanism, String saslJaasConfig) {
        this.bootstrap = bootstrap;
        this.topic = topic;
        this.kafkaAuthFlag = kafkaAuthFlag;
        this.securityProtocol = securityProtocol;
        this.saslMechanism = saslMechanism;
        this.saslJaasConfig = saslJaasConfig;
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
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        properties.put("group.id", "KafkaSupplierReceiver");
        properties.put("enable.auto.commit", "true");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        if ("true".equals(kafkaAuthFlag)) {
            properties.put("security.protocol", securityProtocol);
            properties.put("sasl.mechanism", saslMechanism);
            properties.put("sasl.jaas.config", saslJaasConfig);
        }

        consumer = new KafkaConsumer<String, byte[]>(properties);
        consumer.subscribe(Lists.newArrayList(topic.split(",")));

        messageFetcher = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "Kafka-Message-Fetcher");
            t.setDaemon(true);
            return t;
        });
        messageFetcher.execute(() -> {
            try {
                while (isRunning()) {
                    fetchMessages();
                }
            } finally {
                /**
                 * consumer程序结束后一定要显示关闭consumer以释放KafkaConuser运行过程中占用的各种系统资源
                 * KafkaConsumer.close()：关闭consumer并等待30秒
                 * KafkaConsumer.close(timeout): 关闭consumer并最多等待给定的timeout秒
                 */
                consumer.close();
            }
        });
    }

    private void fetchMessages() {
        try {
            /**
             * 指定超时时间，通常情况下consumer拿到了足够多的可用数据，会立即从该方法返回，但若当前没有足够多数据
             * consumer会处于阻塞状态，但当到达设定的超时时间，则无论数据是否足够都为立即返回
             */
            ConsumerRecords<String, byte[]> records = consumer.poll(POLL_TIMEOUT);
            Iterator<ConsumerRecord<String, byte[]>> iterator = records.iterator();
            while (iterator.hasNext()) {
                ConsumerRecord<String, byte[]> record = iterator.next();
                publishMessage(record);
            }
        } catch (Exception e) {
            logger.error("Publish message error.");
        }
    }

    private void publishMessage(ConsumerRecord<String, byte[]> record) throws InterruptedException {
        byte[] value = record.value();
        ObjectSerializer objectSerializer = ObjectSerializerFactory.getObjectSerializer("thrift");
        MessageEntity messageEntity = objectSerializer.deserialize(value);
        if (messageEntity != null) {
            Map<String, Object> header = messageEntity.getHeaders();
            String message = null;
            if (MapUtils.isNotEmpty(messageEntity.getBody()) && messageEntity.getBody().containsKey("content")
                    && messageEntity.getBody().get("content") != null) {
                message = ObjectUtils.toString(messageEntity.getBody().get("content"));
            }
            if (StringUtils.isBlank(message)) {
                return;
            }
            header.put("dataVersion", header.get("version"));
            header.put("receiveHttpTime", System.currentTimeMillis());
            Object dataType = header.get("dataType");
            if (dataType != null && DataType.PRESSURE_ENGINE_TRACE_LOG == (byte) dataType) {
                header.put("dataType", DataType.TRACE_LOG);
                queue.publish(header, queue.splitLog(message, DataType.TRACE_LOG));
            } else if (dataType != null && (DataType.MONITOR_LOG == (byte) dataType
                    || DataType.TRACE_LOG == (byte) dataType)) {
                queue.publish(header, queue.splitLog(message, (byte) dataType));
            } else {
                queue.publish(header, message);
            }

        }
    }


    /**
     * 停止获取数据
     *
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        super.stop();
        messageFetcher.shutdown();
    }

    @Override
    public void addObserver(LifecycleObserver<Supplier> observer) {
        super.addObserver(observer);
    }

    public KafkaConsumer getConsumer() {
        return consumer;
    }
}
