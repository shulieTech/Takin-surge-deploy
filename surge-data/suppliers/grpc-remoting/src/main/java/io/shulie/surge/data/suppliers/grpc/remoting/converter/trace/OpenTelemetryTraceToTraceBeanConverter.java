package io.shulie.surge.data.suppliers.grpc.remoting.converter.trace;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.resource.v1.Resource;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.TracesData;
import io.shulie.surge.data.suppliers.grpc.remoting.MiddlewareType;
import io.shulie.surge.data.suppliers.grpc.remoting.converter.ConvertUtils;
import io.shulie.surge.data.suppliers.grpc.remoting.converter.ConverterRegistry;
import io.shulie.surge.data.suppliers.grpc.remoting.converter.OpenTelemetryBeanConverter;
import io.shulie.surge.data.suppliers.grpc.remoting.converter.trace.middleware.MiddlewareConverter;
import io.shulie.surge.data.suppliers.grpc.remoting.trace.TraceProtoBean;
import io.shulie.takin.data.stream.acceptor.proto.ProtoJsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * Opentelemetry trace转换为traceBean
 *
 * @author vincent
 * @date 2022/07/02 13:45
 **/
@Slf4j
public class OpenTelemetryTraceToTraceBeanConverter implements OpenTelemetryBeanConverter<TraceProtoBean> {
    private OpentelemetryTraceBaseConverter opentelemetryBaseConverter;
    private ConverterRegistry registry;
    private boolean skipErrorLog;

    /**
     * 静默日志
     */
    private boolean skipSilentLog;

    public OpenTelemetryTraceToTraceBeanConverter(boolean skipErrorLog, boolean skipSilentLog) {
        opentelemetryBaseConverter = new OpentelemetryTraceBaseConverter();
        this.registry = new ConverterRegistry();
        this.skipErrorLog = skipErrorLog;
        this.skipSilentLog = skipSilentLog;
    }

    /**
     * 转换json
     *
     * @param json
     * @return
     * @throws InvalidProtocolBufferException
     */
    @Override
    public List<TraceProtoBean> convert(String json) throws InvalidProtocolBufferException {
        TracesData tracesData = (TracesData) ProtoJsonUtil.toObject(TracesData.newBuilder(), json);
        List<TraceProtoBean> traceBeans = Lists.newArrayList();
        List<ResourceSpans> resourceSpans = tracesData.getResourceSpansList();
        for (ResourceSpans resourceSpan : resourceSpans) {
            Resource resource = resourceSpan.getResource();
            List<ScopeSpans> scopeSpans = resourceSpan.getScopeSpansList();
            for (ScopeSpans scopeSpan : scopeSpans) {
                InstrumentationScope instrumentationScope = scopeSpan.getScope();
                List<Span> spans = scopeSpan.getSpansList();
                for (Span span : spans) {
                    TraceProtoBean traceBean = new TraceProtoBean();
                    try {
                        opentelemetryBaseConverter.resourceConvert(resource, traceBean);
                        opentelemetryBaseConverter.spanConvert(span, traceBean);
                        MiddlewareType middlewareType = MiddlewareType.valueOf(traceBean.getMiddlewareType());
                        List<MiddlewareConverter> converters = registry.findMiddlewareConverters(traceBean.getMiddlewareType());
                        if (CollectionUtils.isEmpty(converters)) {
                            log.error("Not load converter.middlewareType:{}", middlewareType);
                            continue;
                        }
                        for (MiddlewareConverter converter : converters) {
                            converter.convert(span, traceBean);
                        }
                        check(traceBean);
                        traceBeans.add(traceBean);
                    } catch (Throwable e) {
                        if (!skipSilentLog) {
                            log.error("trace check error:{}", traceBean, e);
                        }
                        if (!skipErrorLog) {
                            throw e;
                        }
                    }
                }
            }
        }
        return traceBeans;
    }

    @Override
    public List<TraceProtoBean> convertTrace(ExportTraceServiceRequest request) {
        List<TraceProtoBean> traceBeans = Lists.newArrayList();
        List<ResourceSpans> resourceSpans = request.getResourceSpansList();
        for (ResourceSpans resourceSpan : resourceSpans) {
            Resource resource = resourceSpan.getResource();
            List<ScopeSpans> scopeSpans = resourceSpan.getScopeSpansList();
            for (ScopeSpans scopeSpan : scopeSpans) {
                InstrumentationScope instrumentationScope = scopeSpan.getScope();
                List<Span> spans = scopeSpan.getSpansList();
                for (Span span : spans) {
                    TraceProtoBean traceBean = new TraceProtoBean();
                    try {
                        opentelemetryBaseConverter.resourceConvert(resource, traceBean);
                        opentelemetryBaseConverter.spanConvert(span, traceBean);
                        MiddlewareType middlewareType = MiddlewareType.valueOf(traceBean.getMiddlewareType());
                        List<MiddlewareConverter> converters = registry.findMiddlewareConverters(traceBean.getMiddlewareType());
                        if (CollectionUtils.isEmpty(converters)) {
                            log.error("Not load converter.middlewareType:{}", middlewareType);
                            continue;
                        }
                        for (MiddlewareConverter converter : converters) {
                            converter.convert(span, traceBean);
                        }
                        check(traceBean);
                        traceBeans.add(traceBean);
                    } catch (Throwable e) {
                        if (!skipSilentLog) {
                            log.error("trace check error:{}", traceBean, e);
                        }
                        if (!skipErrorLog) {
                            throw e;
                        }
                    }
                }
            }
        }
        return traceBeans;
    }

    private void checkStringColumn(TraceProtoBean traceProtoBean, String column, String message) {
        if (ConvertUtils.isBlank(column)) {
            if (!skipSilentLog) {
                log.error(message + ",traceBean:{}", traceProtoBean);
            }
            throw new IllegalArgumentException(message);
        }
    }

    private void checkIntegerColumn(TraceProtoBean traceProtoBean, Integer column, String message) {
        if (null == column) {
            if (!skipErrorLog) {
                log.error(message + ",traceBean:{}", traceProtoBean);
            }
            throw new IllegalArgumentException(message);
        }
    }

    public void check(TraceProtoBean traceProtoBean) {
        checkStringColumn(traceProtoBean, traceProtoBean.getTenantCode(), "tenantCode is null");
        checkStringColumn(traceProtoBean, traceProtoBean.getTraceId(), "traceId is null");
        checkStringColumn(traceProtoBean, traceProtoBean.getAppName(), "appName is null");
        checkStringColumn(traceProtoBean, traceProtoBean.getAgentId(), "agentId is null");
        checkStringColumn(traceProtoBean, traceProtoBean.getInvokeId(), "invokeId is null");
        checkStringColumn(traceProtoBean, traceProtoBean.getService(), "service is null");
        checkStringColumn(traceProtoBean, traceProtoBean.getMethod(), "method is null");
        checkStringColumn(traceProtoBean, traceProtoBean.getChainCode(), "chainCode is null");
        checkStringColumn(traceProtoBean, traceProtoBean.getHostIp(), "hostIp is null");
        checkStringColumn(traceProtoBean, traceProtoBean.getMiddlewareName(), "middlewareName is null");
        checkStringColumn(traceProtoBean, traceProtoBean.getTenantCode(), "tenantCode is null");
        checkIntegerColumn(traceProtoBean, traceProtoBean.getPort(), "port is null");

        if (ConvertUtils.isNullString(traceProtoBean.getRequest())) {
            if (!skipSilentLog) {
                log.error("request is null" + ",traceBean:{}", traceProtoBean);
            }
            throw new IllegalArgumentException("request is null");
        } else {
            if (!skipErrorLog) {
                JSON.toJSONString(traceProtoBean.getRequest());
            }
        }

        if (ConvertUtils.isNullString(traceProtoBean.getResponse())) {
            if (!skipSilentLog) {
                log.error("response is null" + ",traceBean:{}", traceProtoBean);
            }
            throw new IllegalArgumentException("response is null");
        } else {
            if (!skipErrorLog) {
                JSON.toJSONString(traceProtoBean.getResponse());
            }
        }

        if (null != traceProtoBean.getStartTime() && traceProtoBean.getStartTime().getYear() == 1970) {
            if (!skipSilentLog) {
                log.error("startTime is null" + ",traceBean:{}", traceProtoBean);
            }
            throw new IllegalArgumentException("startTime is null");
        }
    }

    @Override
    public List<TraceProtoBean> convertMetric(ExportMetricsServiceRequest request) {
        return null;
    }

    @Override
    public List<TraceProtoBean> convertLog(ExportLogsServiceRequest request) {
        return null;
    }
}
