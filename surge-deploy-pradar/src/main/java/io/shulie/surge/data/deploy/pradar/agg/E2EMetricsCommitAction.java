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
import io.shulie.surge.data.sink.clickhouse.ClickHouseShardSupport;
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
    private ClickHouseShardSupport clickHouseShardSupport;

    public E2EMetricsCommitAction(ClickHouseShardSupport clickHouseShardSupport) {
        this.clickHouseShardSupport = clickHouseShardSupport;
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
                // 总次数/成功次数/totalRt/错误次数/totalQps
                Map<String, Object> fields = Maps.newHashMap();

                fields.put("nodeId", tags[0]);
                fields.put("parsedAppName", tags[1]);
                fields.put("parsedServiceName", tags[2]);
                fields.put("parsedMethod", tags[3]);
                fields.put("rpcType", tags[4]);
                fields.put("clusterTest", tags[5]);
                fields.put("exceptionType", tags[6]);
                //放入租户标识
                fields.put("tenantAppKey", tags[7]);
                //放入环境标识
                fields.put("envCode", tags[8]);

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
                fields.put("time", System.currentTimeMillis());
                String tableName = clickHouseShardSupport.isCluster() ? metricsId : metricsId + "_all";
                // 写入clickHouse
                clickHouseShardSupport.insert(fields, tags[1], tableName);
            });

        } catch (Throwable e) {
            logger.error("write fail clickHouse " + ExceptionUtils.getStackTrace(e));
        }
    }
}