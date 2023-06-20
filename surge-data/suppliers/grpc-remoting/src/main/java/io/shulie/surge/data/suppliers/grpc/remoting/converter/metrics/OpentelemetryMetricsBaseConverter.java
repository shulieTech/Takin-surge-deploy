package io.shulie.surge.data.suppliers.grpc.remoting.converter.metrics;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import io.opentelemetry.proto.metrics.v1.Sum;
import io.opentelemetry.proto.resource.v1.Resource;
import io.shulie.surge.data.suppliers.grpc.remoting.TenantCodeCache;
import io.shulie.surge.data.suppliers.grpc.remoting.converter.ConvertUtils;
import io.shulie.surge.data.suppliers.grpc.remoting.converter.trace.OpentelemetryTraceBaseConverter;
import io.shulie.surge.data.suppliers.grpc.remoting.metrics.MetricsProtoBean;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * metrics基本信息转换类
 *
 * @author vincent
 * @date 2022/07/02 14:13
 **/
public class OpentelemetryMetricsBaseConverter {
    public static final String AGENT_ID = OpentelemetryTraceBaseConverter.AGENT_ID;

    public static final String APP_NAME = OpentelemetryTraceBaseConverter.APP_NAME;

    public static final String USER_APP_KEY = OpentelemetryTraceBaseConverter.USER_APP_KEY;

    /**
     * @param metricsProtoBean
     */
    public void resourceConvert(Resource resource, MetricsProtoBean metricsProtoBean) {
        Map<String, String> attributes = ConvertUtils.attributeToMap(resource.getAttributesList());
        if (attributes.containsKey(APP_NAME)) {
            metricsProtoBean.setAppName(attributes.get(APP_NAME));
        } else {
            throw new IllegalArgumentException(APP_NAME + " is null" + resource);
        }
        if (attributes.containsKey(AGENT_ID)) {
            metricsProtoBean.setAgentId(attributes.get(AGENT_ID));
        } else {
            throw new IllegalArgumentException(AGENT_ID + " is null" + resource);
        }
        if (attributes.containsKey(USER_APP_KEY)) {
            String tenantCode = TenantCodeCache.getInstance().get(attributes.get(USER_APP_KEY));
            if (StringUtils.isBlank(tenantCode)) {
                throw new IllegalArgumentException("Tenant code is not register.UserAppKey:" + attributes.get(USER_APP_KEY));
            }
            metricsProtoBean.setTenantCode(tenantCode);
        } else {
            throw new IllegalArgumentException(USER_APP_KEY + " is null" + resource);
        }
    }

    /**
     * open telemetry sum类型转换
     *
     * @param resource
     * @param metric
     * @return
     */
    public List<MetricsProtoBean> sumConvert(Resource resource, Metric metric) {
        List<MetricsProtoBean> metricsProtoBeans = Lists.newArrayList();
        if (null == metric.getSum()) {
            return metricsProtoBeans;
        }
        MetricsProtoBean metricsProtoBean = new MetricsProtoBean();
        resourceConvert(resource, metricsProtoBean);
        String appName = metricsProtoBean.getAppName();
        String agentId = metricsProtoBean.getAgentId();
        String tenantCode = metricsProtoBean.getTenantCode();
        String metricsName = metric.getName();
        String description = metric.getDescription();
        Sum sum = metric.getSum();
        List<NumberDataPoint> numberDataPoints = sum.getDataPointsList();
        for (NumberDataPoint numberDataPoint : numberDataPoints) {
            metricsProtoBean = new MetricsProtoBean();
            long nanoTimeStamp = numberDataPoint.getTimeUnixNano();
            metricsProtoBean.setMetricType("Counter");
            metricsProtoBean.setAgentId(agentId);
            metricsProtoBean.setAppName(appName);
            metricsProtoBean.setTenantCode(tenantCode);
            metricsProtoBean.setDescription(description);
            metricsProtoBean.setMetricsName(metricsName);
            metricsProtoBean.setStartTime(timeConvert(nanoTimeStamp));
            metricsProtoBean.getLabels().putAll(keyValueListToMap(numberDataPoint.getAttributesList()));
            metricsProtoBean.setValue(Double.valueOf(numberDataPoint.getAsInt()));

            metricsProtoBeans.add(metricsProtoBean);
        }

        return metricsProtoBeans;
    }

    public Map<String, String> keyValueListToMap(List<KeyValue> keyValues) {
        Map<String, String> map = Maps.newHashMap();
        for (KeyValue keyValue : keyValues) {
            if (StringUtils.equals(keyValue.getKey(), "http.referer")) {
                String pageUrl = keyValue.getValue().getStringValue();
                int index = StringUtils.indexOf(pageUrl, "?");
                if (-1 != index) {
                    pageUrl = StringUtils.substring(pageUrl, 0, index);
                }
                if (StringUtils.isNotBlank(pageUrl)) {
                    map.put("http_referer", pageUrl);
                }
                continue;
            }
            map.put(keyValue.getKey().replaceAll("\\.", "_"), keyValue.getValue().getStringValue());
        }
        return map;
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
}
