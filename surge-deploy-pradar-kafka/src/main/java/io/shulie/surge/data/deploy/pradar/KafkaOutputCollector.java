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

    public KafkaOutputCollector(String bootstrap, String topic, String kafkaAuthFlag, String securityProtocol,
                                String saslMechanism, String saslJaasConfig) {
        this.topic = topic;
        this.bootstrap = bootstrap;
        Properties properties = new Properties();
        properties.put("bootstrap.servers", bootstrap);
        properties.put("group.id", "KafkaAggregationReceiver");
        properties.put("enable.auto.commit", "true");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        if ("true".equals(kafkaAuthFlag)) {
            properties.put("security.protocol", securityProtocol);
            properties.put("sasl.mechanism", saslMechanism);
            properties.put("sasl.jaas.config", saslJaasConfig);
        }
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
        ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<String, byte[]>(topic, partition, "" + values[0], objectSerializer.serialize(values[1]));
        kafkaProducer.send(producerRecord);
    }
}
