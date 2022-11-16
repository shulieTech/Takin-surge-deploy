package io.shulie.surge.data.deploy.pradar;

import io.shulie.surge.data.deploy.pradar.collector.OutputCollector;
import io.shulie.surge.data.runtime.common.ObjectSerializer;
import io.shulie.surge.data.runtime.common.ObjectSerializerFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * kafka output collector
 *
 * @author vincent
 * @date 2022/11/16 11:59
 **/
public class KafkaOutputCollector implements OutputCollector {

    private KafkaProducer<String, byte[]> kafkaProducer;
    private String topic;
    private String bootstrap;

    public KafkaOutputCollector(String bootstrap, String topic) {
        this.topic = topic;
        this.bootstrap = bootstrap;
        Properties properties = new Properties();
        properties.put("bootstrap", bootstrap);
        properties.put("group.id", "KafkaAggregationReceiver");
        properties.put("enable.auto.commit", "true");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        kafkaProducer = new KafkaProducer<String, byte[]>(properties);
    }


    @Override
    public List<Integer> getReduceIds() {
        List<PartitionInfo> list = kafkaProducer.partitionsFor(topic);
        return list.stream().map(partitionInfo -> {
            return partitionInfo.partition();
        }).collect(Collectors.toList());
    }

    @Override
    public void emit(int partition, String streamId, Object... values) {
        ObjectSerializer objectSerializer = ObjectSerializerFactory.getObjectSerializer("kryo");
        ProducerRecord producerRecord = new ProducerRecord(topic, partition, streamId, objectSerializer.serialize(values));
        kafkaProducer.send(producerRecord);
    }
}
