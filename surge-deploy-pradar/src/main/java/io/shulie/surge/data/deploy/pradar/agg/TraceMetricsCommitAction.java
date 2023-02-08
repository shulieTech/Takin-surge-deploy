package io.shulie.surge.data.deploy.pradar.agg;

import com.google.common.collect.Maps;
import io.shulie.surge.data.common.aggregation.AggregateSlot;
import io.shulie.surge.data.common.aggregation.Aggregation;
import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.common.utils.FormatUtils;
import io.shulie.surge.data.deploy.pradar.common.PradarRtConstant;
import io.shulie.surge.data.deploy.pradar.link.util.StringUtil;
import io.shulie.surge.data.sink.clickhouse.ClickHouseShardSupport;
import io.shulie.surge.data.sink.influxdb.InfluxDBSupport;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TraceMetricsCommitAction implements Aggregation.CommitAction<Metric, CallStat> {
        private static Logger logger = LoggerFactory.getLogger(TraceMetricsCommitAction.class);

        private ClickHouseShardSupport clickHouseShardSupport;

        public TraceMetricsCommitAction(ClickHouseShardSupport clickHouseShardSupport) {
            this.clickHouseShardSupport = clickHouseShardSupport;
        }

        @Override
        public void commit(long slotKey, AggregateSlot<Metric, CallStat> slot) {
            try {
                slot.toMap().entrySet().forEach(metricCallStatEntry -> {
                    Metric metric = metricCallStatEntry.getKey();
                    CallStat callStat = metricCallStatEntry.getValue();

                    String metricsId = metric.getMetricId();
                    String[] tags = metric.getPrefixes();
                    Map<String, Object> fields = Maps.newHashMap();
                    fields.put("edgeId", tags[0]);
                    fields.put("clusterTest", tags[1]);
                    //influxdbTags.put("linkId", tags[2]);
                    fields.put("service", tags[3]);
                    fields.put("method", tags[4]);
                    //influxdbTags.put("extend", StringUtil.formatString(tags[5]));
                    fields.put("appName", StringUtil.formatString(tags[6]));
                    //influxdbTags.put("traceAppName", StringUtil.formatString(tags[7]));
                    //influxdbTags.put("serverAppName", StringUtil.formatString(tags[8]));
                    fields.put("rpcType", tags[9]);
                    //influxdbTags.put("logType", tags[10]);
                    fields.put("middlewareName", tags[11]);
                    //influxdbTags.put("entranceId", tags[12]);
                    //使用sql的md5值作为分组字段,防止sql过长导致分组性能过差
                    //influxdbTags.put("sqlStatementMd5", tags[13]);
                    //放入租户标识
                    fields.put("tenantAppKey", tags[14]);
                    //放入环境标识
                    fields.put("envCode", tags[15]);

                    // 总次数/成功次数/totalRt/错误次数/hitCount/totalQps/totalTps/总次数(不计算采样率)/e2e成功次数/e2e失败次数/maxRt
                    fields.put("totalCount", callStat.get(0));
                    fields.put("successCount", callStat.get(1));
                    fields.put("totalRt", callStat.get(2));
                    fields.put("errorCount", callStat.get(3));
                    fields.put("hitCount", callStat.get(4));
                    fields.put("totalTps", callStat.get(5));
                    fields.put("total", callStat.get(6));
                    fields.put("e2eSuccessCount", callStat.get(7));
                    fields.put("e2eErrorCount", callStat.get(8));
                    fields.put("maxRt", callStat.get(9));
                    //计算平均耗时
                    if (callStat.get(0) == 0) {
                        // 如果总调用次数为0,直接取总耗时
                        fields.put("avgRt", (double) callStat.get(2));
                    } else {
                        fields.put("avgRt", callStat.get(2) / (double) callStat.get(0));
                    }
                    fields.put("avgTps", (double) callStat.get(5) / PradarRtConstant.REDUCE_TRACE_SECONDS_INTERVAL);
                    fields.put("traceId", callStat.getTraceId());
                    //放入真实的sql语句
                    fields.put("sqlStatement", callStat.getSqlStatement());
                    fields.put("log_time", FormatUtils.toDateTimeSecondString(slotKey * 1000));
                    fields.put("time", System.currentTimeMillis());
                    //华为云saas环境发现存在measurement名称非法的问题(\u001d\u001ctrace_metrics),推测是反序列化问题导致的
                    if (!metricsId.equals(PradarRtConstant.METRICS_ID_TRACE)) {
                        logger.warn("measurement is illegal:{}", metricsId);
                        metricsId = PradarRtConstant.METRICS_ID_TRACE;
                    }
                    String tableName = clickHouseShardSupport.isCluster() ? metricsId : metricsId + "_all";
                    clickHouseShardSupport.insert(fields, StringUtil.formatString(tags[6]), tableName);
                });
            } catch (Throwable e) {
                logger.error("write fail clickHouse " + ExceptionUtils.getStackTrace(e));
            }
        }
    }