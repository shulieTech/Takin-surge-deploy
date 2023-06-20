package io.shulie.surge.data.suppliers.grpc.remoting.converter.trace;

import com.alibaba.fastjson.JSON;
import io.opentelemetry.proto.resource.v1.Resource;
import io.opentelemetry.proto.trace.v1.Span;
import io.shulie.surge.data.suppliers.grpc.remoting.TenantCodeCache;
import io.shulie.surge.data.suppliers.grpc.remoting.converter.ConvertUtils;
import io.shulie.surge.data.suppliers.grpc.remoting.trace.TraceProtoBean;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TimeZone;

/**
 * trace基本信息转换类
 *
 * @author vincent
 * @date 2022/07/02 14:13
 **/
public class OpentelemetryTraceBaseConverter {

    private final static Logger logger = LoggerFactory.getLogger(OpentelemetryTraceBaseConverter.class);
    public static final String AGENT_ID = "agent.id";

    public static final String APP_NAME = "service.name";

    public static final String USER_APP_KEY = "takin.tenant.app.key";

    public static final String ENV_CODE_KEY = "takin.envcode";

    /**
     * @param traceBean
     */
    public void resourceConvert(Resource resource, TraceProtoBean traceBean) {
        Map<String, String> attributes = ConvertUtils.attributeToMap(resource.getAttributesList());
        if (attributes.containsKey(APP_NAME)) {
            traceBean.setAppName(attributes.get(APP_NAME));
        } else {
            throw new IllegalArgumentException(APP_NAME + " is null" + resource);
        }
        if (attributes.containsKey(AGENT_ID)) {
            traceBean.setAgentId(attributes.get(AGENT_ID));
        } else {
            throw new IllegalArgumentException(AGENT_ID + " is null" + resource);
        }
        if (attributes.containsKey(USER_APP_KEY)) {
            String tenantCode = TenantCodeCache.getInstance().get(attributes.get(USER_APP_KEY));
            if (StringUtils.isBlank(tenantCode)) {
                throw new IllegalArgumentException(
                        "Tenant code is not register.UserAppKey:" + attributes.get(USER_APP_KEY));
            }
            traceBean.setTenantCode(tenantCode);
        } else {
            throw new IllegalArgumentException(USER_APP_KEY + " is null" + resource);
        }

        if (attributes.containsKey(ENV_CODE_KEY) && attributes.get(ENV_CODE_KEY) != null) {
            traceBean.setEnvCode(attributes.get(ENV_CODE_KEY));
        } else {
            throw new IllegalArgumentException(ENV_CODE_KEY + " is not exits or value is null" + resource);
        }

    }

    private static final String CHAIN_CODE = "chain.code";

    private static final String SERVICE_CODE = "service.code";
    /**
     * 自耗时
     */
    private static final String SEVER_SELF_TAKE_TIME = "self.take.time";

    public static final String REQUEST = "biz.request";

    public static final String RESPONSE = "biz.response";

    private static final String THREAD_ASYNC = "thread.async";

    private static final String REMOTE_IP = "net.peer.ip";

    private static final String CLUSTER_HOST = "net.peer.name";

    private static final String REMOTE_PORT = "net.peer.port";

    private static final String MIDDLEWARE_TYPE = "middleware.type";

    private static final String MIDDLEWARE_NAME = "middleware.name";
    private static final String RESULT_CODE = "result.code";

    public static final String EXCEPTION = "exception";
    public static final String EXCEPTION_STACKTRACE = "exception.stacktrace";

    private static final String PLUGIN_NAME = "plugin.name";

    /**
     * span转换器
     *
     * @param span
     * @param traceBean
     */
    public void spanConvert(Span span, TraceProtoBean traceBean) {
        if (span.getStartTimeUnixNano() <= 0 || span.getEndTimeUnixNano() <= 0) {
            //日志太多，改成 debug 级别
            if (logger.isDebugEnabled()) {
                logger.debug("starttime or endtime is invalid. {}", span);
            }
        }

        traceBean.setInvokeId(span.getSpanId());
        traceBean.setTraceId(span.getTraceId());
        if (StringUtils.isNotBlank(span.getSpanId())) {
            int[] segments = ConvertUtils.getInvokeIdArray(span.getSpanId());
            int level = segments.length;
            int index = segments[segments.length - 1];
            traceBean.setLevel(level);
            traceBean.setIndex(index);
        }


        traceBean.setStartTime(timeConvert(span.getStartTimeUnixNano()));
        long endTimeUnixNano = span.getEndTimeUnixNano();
        long startTimeUnixNano = span.getStartTimeUnixNano();
        if (endTimeUnixNano - startTimeUnixNano < 0) {
            traceBean.setCost(0L);
        } else {
            traceBean.setCost(endTimeUnixNano - startTimeUnixNano);
        }

        spanKindConvert(span.getKind(), traceBean);
        Map<String, String> attributes = ConvertUtils.attributeToMap(span.getAttributesList());

        if (attributes.containsKey(CHAIN_CODE)) {
            traceBean.setChainCode(attributes.get(CHAIN_CODE));
        } else {
            throw new IllegalArgumentException(CHAIN_CODE + " is null" + span);
        }
        if (attributes.containsKey(SERVICE_CODE)) {
            traceBean.setServiceCode(attributes.get(SERVICE_CODE));
        } else {
            throw new IllegalArgumentException(SERVICE_CODE + " is null" + span);
        }
        // 自耗时
        if (attributes.containsKey(SEVER_SELF_TAKE_TIME)) {
            Long selfCost = Long.valueOf(attributes.get(SEVER_SELF_TAKE_TIME));
            if (selfCost != null && selfCost >= 0) {
                traceBean.setSelfCost(selfCost);
            } else {
                traceBean.setSelfCost(0L);
            }
        } else {
            traceBean.setSelfCost(0L);
        }

        if (attributes.containsKey(REQUEST)) {
            traceBean.setRequest(attributes.get(REQUEST));
            traceBean.setRequestSize((long) StringUtils.length(attributes.get(REQUEST)));
        }

        if (attributes.containsKey(RESPONSE)) {
            traceBean.setResponse(attributes.get(RESPONSE));
            traceBean.setResponseSize((long) StringUtils.length(attributes.get(RESPONSE)));
        }
        // 如果异常 直接覆盖 RESPONSE
        Map<String, Map<String, String>> eventMap = ConvertUtils.eventToMap(span.getEventsList());
        if (eventMap.containsKey(EXCEPTION)) {
            traceBean.setResponse(JSON.toJSONString(eventMap.get(EXCEPTION).get(EXCEPTION_STACKTRACE)));
            traceBean.setResponseSize((long) StringUtils.length(traceBean.getResponse()));
        }
        if (attributes.containsKey(THREAD_ASYNC)) {
            traceBean.setAsync(BooleanUtils.toInteger(BooleanUtils.toBoolean(attributes.get(THREAD_ASYNC))));
        } else {
            throw new IllegalArgumentException(THREAD_ASYNC + " is null" + span);
        }
        if (attributes.containsKey(REMOTE_IP)) {
            traceBean.setHostIp(attributes.get(REMOTE_IP));
        } else {
            throw new IllegalArgumentException(REMOTE_IP + " is null" + span);
        }
        if (attributes.containsKey(REMOTE_PORT)) {
            if (NumberUtils.isNumber(attributes.get(REMOTE_PORT))) {
                traceBean.setPort(NumberUtils.toInt(attributes.get(REMOTE_PORT)));
            } else {
                throw new IllegalArgumentException(REMOTE_PORT + " is not number" + span);
            }
        } else {
            throw new IllegalArgumentException(REMOTE_PORT + " is null" + span);
        }

        //集群host地址
        if (attributes.containsKey(CLUSTER_HOST)) {
            traceBean.setClusterHost(attributes.get(CLUSTER_HOST));
        } else {
            traceBean.setClusterHost(traceBean.getHostIp() + ":" + traceBean.getPort());
        }

        if (attributes.containsKey(MIDDLEWARE_TYPE)) {
            traceBean.setMiddlewareType(NumberUtils.toInt(attributes.get(MIDDLEWARE_TYPE)));
        } else {
            throw new IllegalArgumentException(MIDDLEWARE_TYPE + " is null" + span);
        }
        if (attributes.containsKey(RESULT_CODE)) {
            traceBean.setResultCode(attributes.get(RESULT_CODE));
        } else {
            throw new IllegalArgumentException(RESULT_CODE + " is null" + span);
        }
        if (attributes.containsKey(MIDDLEWARE_NAME)) {
            traceBean.setMiddlewareName(attributes.get(MIDDLEWARE_NAME));
            /**
             * @see io.shulie.takin.streams.acceptor.receiver.opentelemetry.opentelmetry.converter.trace.middleware.ServiceTypeConverter
             */
            traceBean.setServiceType(attributes.get(MIDDLEWARE_NAME));
        } else {
            throw new IllegalArgumentException(MIDDLEWARE_NAME + " is null" + span);
        }
        traceBean.setClusterTest(0);

        // 插入插件来源
        if (attributes.containsKey(PLUGIN_NAME)) {
            traceBean.setMiddlewareModule(attributes.get(PLUGIN_NAME));
        }

    }

    /**
     * nanoTimeStamp转换
     *
     * @param nanoTimeStamp
     * @return
     */
    private LocalDateTime timeConvert(long nanoTimeStamp) {
        long seconds = (nanoTimeStamp / 1_000_000_000L);
        long fraction = (nanoTimeStamp % 1_000_000_000L);
        Instant instant = Instant.ofEpochSecond(seconds, fraction);
        return LocalDateTime.ofInstant(instant, TimeZone.getDefault().toZoneId());
    }

    /**
     * span类型转换
     * <p>
     * SPAN_KIND_UNSPECIFIED = 0;
     * SPAN_KIND_INTERNAL = 1;
     * SPAN_KIND_SERVER = 2;
     * SPAN_KIND_CLIENT = 3;
     * SPAN_KIND_PRODUCER = 4;
     * SPAN_KIND_CONSUMER = 5;
     * <p>
     * 1 内部调用
     * 2 客户端
     * 3 服务端
     * 4 生产者
     * 5 消费者
     *
     * @param spanKind
     * @param traceProtoBean
     */
    public void spanKindConvert(Span.SpanKind spanKind, TraceProtoBean traceProtoBean) {

        /**
         * 1 内部调用
         * 2 客户端
         * 3 服务端
         * 4 生产者
         * 5 消费者
         */
        switch (spanKind.getNumber()) {
            case 1:
                traceProtoBean.setKind(1);
                break;
            case 2:
                traceProtoBean.setKind(3);
                break;
            case 3:
                traceProtoBean.setKind(2);
                break;
            case 4:
                traceProtoBean.setKind(4);
                break;
            case 5:
                traceProtoBean.setKind(5);
                break;
            default:
                traceProtoBean.setKind(1);
        }
    }
}
