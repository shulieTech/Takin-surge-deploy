package io.shulie.surge.data.suppliers.grpc.remoting.converter.trace.middleware;

import io.opentelemetry.proto.trace.v1.Span;
import io.shulie.surge.data.suppliers.grpc.remoting.trace.TraceProtoBean;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author hezhongqi
 * @Package io.shulie.takin.streams.acceptor.receiver.opentelemetry.opentelmetry.converter.trace.middleware
 * @ClassName: ServiceTypeConverter
 * @description:serviceCode 转化
 * @date 2022/9/8 11:35
 */
public class ServiceTypeConverter implements MiddlewareConverter {
    private static String[] REDIS = {"redis-jedis", "redis-jedis-pipeline", "redis-lettuce", "lettuce", "jedis", "redis-lettuce", "redisson", "redis-redisson"};
    private static String[] ROCKETMQ = {"apache-rocketmq", "alibaba-rocketmq"};

    private static String[] DUBBO = {"apache-dubbo", "alibaba-dubbo"};

    @Override
    public void convert(Span span, TraceProtoBean traceProtoBean) {
        if (StringUtils.isNotBlank(traceProtoBean.getServiceType())) {
            if (Arrays.stream(REDIS).anyMatch(t -> t.equals(traceProtoBean.getServiceType()))) {
                traceProtoBean.setServiceType("redis");
            }
            if (Arrays.stream(ROCKETMQ).anyMatch(t -> t.equals(traceProtoBean.getServiceType()))) {
                traceProtoBean.setServiceType("rocketmq");
            }
            if (Arrays.stream(DUBBO).anyMatch(t -> t.equals(traceProtoBean.getServiceType()))) {
                traceProtoBean.setServiceType("dubbo");
            }
        }
    }

}
