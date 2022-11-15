package io.shulie.surge.data.deploy.pradar.agg;

import com.google.common.collect.Maps;
import io.shulie.surge.data.common.aggregation.AggregateSlot;
import io.shulie.surge.data.common.aggregation.Aggregation;
import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.common.utils.FormatUtils;
import io.shulie.surge.data.deploy.pradar.common.PradarRtConstant;
import io.shulie.surge.data.deploy.pradar.link.util.StringUtil;
import io.shulie.surge.data.sink.influxdb.InfluxDBSupport;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MetricsCommitAction implements Aggregation.CommitAction<Metric, CallStat> {
    private static Logger logger = LoggerFactory.getLogger(MetricsCommitAction.class);
    private InfluxDBSupport influxDbSupport;

    public MetricsCommitAction(InfluxDBSupport influxDbSupport) {
        this.influxDbSupport = influxDbSupport;
    }

    @Override
    public void commit(long slotKey, AggregateSlot<Metric, CallStat> slot) {
        try {
            slot.toMap().entrySet().forEach(metricCallStatEntry -> {
                Metric metric = metricCallStatEntry.getKey();
                CallStat callStat = metricCallStatEntry.getValue();
                String metricsId = metric.getMetricId();
                String[] tags = metric.getPrefixes();

                Map<String, String> influxdbTags = Maps.newHashMap();
                influxdbTags.put("appName", tags[0]);
                influxdbTags.put("event", tags[1]);
                influxdbTags.put("type", tags[2]);
                influxdbTags.put("ptFlag", tags[3]);
                influxdbTags.put("callEvent", StringUtil.formatString(tags[4]));
                influxdbTags.put("callType", StringUtil.formatString(tags[5]));
                influxdbTags.put("entryFlag", tags[6]);

                // 总次数/成功次数/totalRt/错误次数/hitCount/totalQps/totalTps/total
                Map<String, Object> fields = Maps.newHashMap();
                fields.put("totalCount", callStat.get(0));
                fields.put("successCount", callStat.get(1));
                fields.put("totalRt", callStat.get(2));
                fields.put("errorCount", callStat.get(3));
                fields.put("hitCount", callStat.get(4));
                fields.put("totalQps", formatDouble(callStat.get(5)));
                fields.put("totalTps", formatDouble(callStat.get(6)));
                fields.put("total", callStat.get(7));

                fields.put("qps", callStat.get(5) / (double) PradarRtConstant.REDUCE_TRACE_SECONDS_INTERVAL);
                fields.put("tps", callStat.get(6) / (double) PradarRtConstant.REDUCE_TRACE_SECONDS_INTERVAL);
                if (callStat.get(0) == 0) {
                    // 平均
                    fields.put("rt", (double) callStat.get(2));
                } else {
                    fields.put("rt", callStat.get(2) / (double) callStat.get(0));
                }
                fields.put("traceId", callStat.getTraceId());
                fields.put("log_time", FormatUtils.toDateTimeSecondString(slotKey * 1000));
                influxDbSupport.write("pradar", metricsId, influxdbTags, fields, slotKey * 1000);
            });
        } catch (Throwable e) {
            logger.error("write fail influxdb " + ExceptionUtils.getStackTrace(e));
        }
    }

    Double formatDouble(long value) {
        return Double.valueOf(value);
    }
}
