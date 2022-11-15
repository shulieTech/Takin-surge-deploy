package io.shulie.surge.data.deploy.pradar;

import com.google.common.collect.Lists;
import io.shulie.surge.data.common.aggregation.Aggregation;
import io.shulie.surge.data.deploy.pradar.agg.DefaultAggregationReceiver;
import io.shulie.surge.data.deploy.pradar.common.ObjectSerializer;
import io.shulie.surge.data.deploy.pradar.common.ObjectSerializerFactory;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Iterator;
import java.util.Properties;

/**
 * kafka聚合数据接收器
 *
 * @author vincent
 * @date 2022/11/15 11:16
 **/
public class KafkaAggregationReceiver extends DefaultAggregationReceiver {

    private String topic;
    private String bootstraps;
    private long pollTimeout = 100l;
    private Thread messageFetcher;

    public KafkaAggregationReceiver(String topic, String bootstraps) {
        this.topic = topic;
        this.bootstraps = bootstraps;
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
        properties.put("bootstrap", bootstraps);
        properties.put("group.id", "KafkaAggregationReceiver");
        properties.put("enable.auto.commit", "true");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<String, byte[]>(properties);
        consumer.subscribe(Lists.newArrayList(topic));
        messageFetcher = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        /**
                         * 指定超时时间，通常情况下consumer拿到了足够多的可用数据，会立即从该方法返回，但若当前没有足够多数据
                         * consumer会处于阻塞状态，但当到达设定的超时时间，则无论数据是否足够都为立即返回
                         */
                        ConsumerRecords<String, byte[]> records = consumer.poll(pollTimeout);
                        Iterator<ConsumerRecord<String, byte[]>> iterator = records.iterator();
                        while (iterator.hasNext()) {
                            ConsumerRecord<String, byte[]> record = iterator.next();
                            String messageKey = record.key();
                            byte[] value = record.value();
                            ObjectSerializer objectSerializer = ObjectSerializerFactory.getObjectSerializer("kryo");
                            execute(NumberUtils.toLong(messageKey), objectSerializer.deserialize(value));
                        }
                    }
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
     * 停止运行。如果已经停止，则应该不会有任何效果。
     * 建议实现使用同步方式执行。
     */
    @Override
    public void stop() throws Exception {
        //停止线程
        messageFetcher.interrupt();
    }
}
