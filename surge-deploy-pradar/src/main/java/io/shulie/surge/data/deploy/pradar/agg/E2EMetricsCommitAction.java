package io.shulie.surge.data.deploy.pradar.agg;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.shulie.surge.data.common.aggregation.AggregateSlot;
import io.shulie.surge.data.common.aggregation.Aggregation;
import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.deploy.pradar.common.PradarRtConstant;
import io.shulie.surge.data.deploy.pradar.model.AmdbAppModel;
import io.shulie.surge.data.deploy.pradar.model.AmdbAppRelationModel;
import io.shulie.surge.data.sink.influxdb.InfluxDBSupport;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * APP指标计算提交
 */
public class E2EMetricsCommitAction implements Aggregation.CommitAction<Metric, CallStat> {
    private static final Logger logger = LoggerFactory.getLogger(E2EMetricsCommitAction.class);
    private InfluxDBSupport influxDbSupport;
    private String metricsDataBase;

    public E2EMetricsCommitAction(InfluxDBSupport influxDbSupport, String metricsDataBase) {
        this.influxDbSupport = influxDbSupport;
        this.metricsDataBase = metricsDataBase;
    }

    @Override
    public void commit(long slotKey, AggregateSlot<Metric, CallStat> slot) {
        try {
            Set<AmdbAppModel> appSet = Sets.newHashSet();
            Set<AmdbAppRelationModel> appRelationSet = Sets.newHashSet();

            slot.toMap().entrySet().forEach(metricCallStatEntry -> {
                Metric metric = metricCallStatEntry.getKey();
                CallStat callStat = metricCallStatEntry.getValue();
                String metricsId = metric.getMetricId();
                String[] tags = metric.getPrefixes();
                Map<String, String> influxdbTags = Maps.newHashMap();
                influxdbTags.put("nodeId", tags[0]);
                influxdbTags.put("parsedAppName", tags[1]);
                influxdbTags.put("parsedServiceName", tags[2]);
                influxdbTags.put("parsedMethod", tags[3]);
                influxdbTags.put("rpcType", tags[4]);
                influxdbTags.put("clusterTest", tags[5]);
                influxdbTags.put("exceptionType", tags[6]);
                //放入租户标识
                influxdbTags.put("tenantAppKey", tags[7]);
                //放入环境标识
                influxdbTags.put("envCode", tags[8]);

                // 总次数/成功次数/totalRt/错误次数/totalQps
                Map<String, Object> fields = Maps.newHashMap();
                fields.put("totalCount", callStat.get(0));
                fields.put("successCount", callStat.get(1));
                fields.put("totalRt", callStat.get(2));
                fields.put("errorCount", callStat.get(3));
                fields.put("totalQps", callStat.get(4));
                // 总QPS/时间窗口
                fields.put("qps", (double) callStat.get(4) / PradarRtConstant.AGG_TRACE_SECONDS_INTERVAL);
                if (callStat.get(0) == 0) {
                    // 平均
                    fields.put("rt", (double) callStat.get(2));
                } else {
                    fields.put("rt", callStat.get(2) / (double) callStat.get(0));
                }
                fields.put("traceId", callStat.getTraceId());

                // 写入influxDB
                influxDbSupport.write(metricsDataBase, metricsId, influxdbTags, fields, slotKey * 1000);
            });

        } catch (Throwable e) {
            logger.error("write fail influxdb " + ExceptionUtils.getStackTrace(e));
        }
    }
}