package io.shulie.surge.data.deploy.pradar;

import com.google.common.collect.Lists;
import io.shulie.surge.data.common.aggregation.Aggregation;
import io.shulie.surge.data.deploy.pradar.agg.DefaultAggregationReceiver;
import io.shulie.surge.data.runtime.common.ObjectSerializer;
import io.shulie.surge.data.runtime.common.ObjectSerializerFactory;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Properties;

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
    private long pollTimeout = 100l;
    private Thread messageFetcher;
    private String kafkaAuthFlag;
    private String securityProtocol;
    private String saslMechanism;
    private String saslJaasConfig;

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
        properties.put("enable.auto.commit", "true");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

        if ("true".equals(kafkaAuthFlag)) {
            properties.put("security.protocol", securityProtocol);
            properties.put("sasl.mechanism", saslMechanism);
            properties.put("sasl.jaas.config", saslJaasConfig);
        }
        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<String, byte[]>(properties);
        consumer.subscribe(Lists.newArrayList(topic));
        messageFetcher = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        try {
                            ConsumerRecords<String, byte[]> records = consumer.poll(pollTimeout);
                            Iterator<ConsumerRecord<String, byte[]>> iterator = records.iterator();
                            while (iterator.hasNext()) {
                                ConsumerRecord<String, byte[]> record = iterator.next();
                                String messageKey = record.key();
                                byte[] value = record.value();
                                ObjectSerializer objectSerializer = ObjectSerializerFactory.getObjectSerializer("kryo");
                                try {
                                    execute(NumberUtils.toLong(messageKey), objectSerializer.deserialize(value));
                                } catch (Exception e) {
                                    logger.error("Execute aggregation error.", e);
                                }
                            }
                        } catch (Exception e){
                            logger.error("拉取数据异常:" + e.getMessage());
                        }
                    }
                } finally {

                    consumer.close();
                }
            }
        });
        messageFetcher.setDaemon(true);
        messageFetcher.start();
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
