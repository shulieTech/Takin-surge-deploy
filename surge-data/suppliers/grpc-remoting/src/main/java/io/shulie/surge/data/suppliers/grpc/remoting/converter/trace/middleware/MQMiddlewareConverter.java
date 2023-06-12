package io.shulie.surge.data.suppliers.grpc.remoting.converter.trace.middleware;

import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.shulie.surge.data.suppliers.grpc.remoting.trace.TraceProtoBean;

import java.util.List;
import java.util.Map;

import static io.shulie.surge.data.suppliers.grpc.remoting.converter.ConvertUtils.attributeToMap;


/**
 * RPC中间件转换类
 *
 * @author vincent
 * @date 2022/07/02 15:30
 **/
//TODO
public class MQMiddlewareConverter implements MiddlewareConverter {

    private static final String MESSAGING_SYSTEM = "messaging.system";
    private static final String MESSAGING_DESTINATION = "messaging.destination";
    private static final String MESSAGING_OPERATION = "messaging.operation";
    private static final String MESSAGING_DESTINATION_KIND = "messaging.destination_kind";
    private static final String MESSAGING_CONSUMER_ID = "messaging.consumer_id";
    private static final String MESSAGING_RABBITMQ_ROUTING_KEY = "messaging.rabbitmq.routing_key";
    private static final String MESSAGING_KAFKA_MESSAGE_KEY = "messaging.kafka.message_key";
    private static final String MESSAGING_KAFKA_CONSUMER_GROUP = "messaging.kafka.consumer_group";
    private static final String MESSAGING_KAFKA_CLIENT_ID = "messaging.kafka.client_id";
    private static final String MESSAGING_ROCKETMQ_NAMESPACE = "messaging.rocketmq.namespace";
    private static final String MESSAGING_ROCKETMQ_CLIENT_GROUP = "messaging.rocketmq.client_group";
    private static final String MESSAGING_ROCKETMQ_CLIENT_ID = "messaging.rocketmq.client_id";
    private static final String MESSAGING_ROCKETMQ_MESSAGE_TAG = "messaging.rocketmq.message_tag";

    /**
     * 属性转换
     *
     * @param span
     * @param traceBean
     */
    @Override
    public void convert(Span span, TraceProtoBean traceBean) {
        List<KeyValue> keyValues = span.getAttributesList();
        Map<String, String> attributes = attributeToMap(keyValues);

        if (attributes.containsKey(MESSAGING_DESTINATION)) {
            traceBean.setService(attributes.get(MESSAGING_DESTINATION));
        } else {
            throw new IllegalArgumentException(MESSAGING_DESTINATION + " is null" + span);
        }

        //如果是生产者，方法直接填充"DEFAULT"
        if (traceBean.getKind() == 4) {
            traceBean.setMethod("DEFAULT");
        }

        //如果是消费者，则校验是否存在consumer_id
        if (traceBean.getKind() == 5) {
            if (attributes.containsKey(MESSAGING_CONSUMER_ID)) {
                traceBean.setMethod(attributes.get(MESSAGING_CONSUMER_ID));
            } else {
                throw new IllegalArgumentException(MESSAGING_CONSUMER_ID + " is null" + span);
            }
        }
    }
}
