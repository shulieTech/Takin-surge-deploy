package io.shulie.surge.data.deploy.pradar;

import com.google.common.collect.Lists;
import io.shulie.surge.data.common.aggregation.Aggregation;
import io.shulie.surge.data.deploy.pradar.agg.DefaultAggregationReceiver;
import io.shulie.surge.data.runtime.common.ObjectSerializer;
import io.shulie.surge.data.runtime.common.ObjectSerializerFactory;
import io.shulie.takin.sdk.kafka.util.PropertiesReader;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * kafka聚合数据接收器
 *
 * @author vincent
 * @date 2022/11/15 11:16
 **/
public class KafkaAggregationReceiver extends DefaultAggregationReceiver {

    private static final Logger logger = LoggerFactory.getLogger(KafkaAggregationReceiver.class);
    private String topic;
    private String bootstraps;
    private final long pollTimeout = 300L;
    private Thread messageFetcher;
    private String kafkaAuthFlag;
    private String securityProtocol;
    private String saslMechanism;
    private String saslJaasConfig;

    private final List<TopicPartition> partitions = new ArrayList<>();

    public KafkaAggregationReceiver(String topic, String bootstraps, String kafkaAuthFlag, String securityProtocol,
                                    String saslMechanism, String saslJaasConfig) {
        this.topic = topic;
        this.bootstraps = bootstraps;
        this.kafkaAuthFlag = kafkaAuthFlag;
        this.securityProtocol = securityProtocol;
        this.saslMechanism = saslMechanism;
        this.saslJaasConfig = saslJaasConfig;
    }

    /**
     * 初始化
     *
     * @param aggregation
     */
    @Override
    public void init(Aggregation aggregation) {
        super.init(aggregation);
        Properties properties = new Properties();
        properties.put("bootstrap.servers", bootstraps);
        properties.put("group.id", "KafkaAggregationReceiver");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        properties.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        properties.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        if ("true".equals(kafkaAuthFlag)) {
            properties.put("security.protocol", securityProtocol);
            properties.put("sasl.mechanism", saslMechanism);
            properties.put("sasl.jaas.config", saslJaasConfig);
        }

        ExecutorService executorService = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "kafka_aggregation_receiver");
            t.setDaemon(true);
            return t;
        });
        executorService.execute(()->fastConsumer(properties));
    }

    private void normalConsumer(Properties properties) {
        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Lists.newArrayList(topic));
        messageFetcher = new Thread(() -> {
            try {
                while (true) {
                    try {
                        ConsumerRecords<String, byte[]> records = consumer.poll(pollTimeout);
                        Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
                        for (ConsumerRecord<String, byte[]> record : records) {
                            ObjectSerializer objectSerializer = ObjectSerializerFactory.getObjectSerializer("kryo");
                            execute(NumberUtils.toLong(record.key()), objectSerializer.deserialize(record.value()));
                            offsets.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset() + 1));
                        }
                        // 提交偏移量
                        consumer.commitSync(offsets);
                    } catch (Exception e) {
                        logger.error("拉取数据异常", e);
                    }
                }
            } finally {
                consumer.close();
            }
        }, "kafka_aggregation_receiver_thread");
        messageFetcher.setDaemon(true);
        messageFetcher.start();
    }

    private void fastConsumer(Properties properties) {
        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList(topic), new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                consumer.commitSync();
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                KafkaAggregationReceiver.this.partitions.clear();
                KafkaAggregationReceiver.this.partitions.addAll(partitions);
            }
        });

        try {
            while (true) {
                try {
                    ConsumerRecords<String, byte[]> records = consumer.poll(pollTimeout);
                    if (records.isEmpty()) {
                        continue;
                    }
                    for (TopicPartition partition : partitions) {
                        ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                        List<ConsumerRecord<String, byte[]>> partitionRecords = records.records(partition);
                        for (ConsumerRecord<String, byte[]> record : partitionRecords) {
                            ObjectSerializer objectSerializer = ObjectSerializerFactory.getObjectSerializer("kryo");
                            executorService.submit(() -> execute(NumberUtils.toLong(record.key()), objectSerializer.deserialize(record.value())));
                        }
                        if (!partitionRecords.isEmpty()) {
                            long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                            consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1)));
                        }
                    }
                } catch (Exception e) {
                    logger.error("load message is error", e);
                }
            }
        } finally {
            consumer.close();
        }
    }



    /**
     * 停止运行。如果已经停止，则应该不会有任何效果。
     * 建议实现使用同步方式执行。
     */
    @Override
    public void stop() throws Exception {
        //停止线程
        messageFetcher.interrupt();
    }
}
