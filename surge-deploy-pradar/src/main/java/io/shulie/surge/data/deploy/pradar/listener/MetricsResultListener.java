/*
 * Copyright 2021 Shulie Technology, Co.Ltd
 * Email: shulie@shulie.io
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.shulie.surge.data.deploy.pradar.listener;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.shulie.surge.data.common.aggregation.DefaultAggregator;
import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.common.utils.FormatUtils;
import io.shulie.surge.data.sink.clickhouse.ClickHouseShardSupport;
import io.shulie.surge.data.sink.influxdb.InfluxDBSupport;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @Author: xingchen
 * @ClassName: MetricsAgg
 * @Package: io.shulie.surge.data.runtime.agg
 * @Date: 2020/11/1817:11
 * @Description:
 */
@Singleton
public class MetricsResultListener implements DefaultAggregator.ResultListener {
    private static Logger logger = LoggerFactory.getLogger(MetricsResultListener.class);

    @Inject
    private ClickHouseShardSupport clickHouseShardSupport;

    @Override
    public String metricId() {
        return "tro_pradar";
    }

    @Override
    public boolean fire(Long slotKey, Metric metric, CallStat callStat) {
        try {
            String metricsId = metric.getMetricId();
            String[] tags = metric.getPrefixes();

            Map<String, Object> fields = Maps.newHashMap();
            fields.put("appName", tags[0]);
            fields.put("event", tags[1]);
            fields.put("type", tags[2]);
            fields.put("ptFlag", tags[3]);
            fields.put("callEvent", formatString(tags[4]));
            fields.put("callType", formatString(tags[5]));
            fields.put("entryFlag", tags[6]);
            fields.put("agentId", tags[7]);

            // 总次数/成功次数/totalRt/错误次数/hitCount/totalQps/totalTps/total

            fields.put("totalCount", callStat.get(0));
            fields.put("successCount", callStat.get(1));
            fields.put("totalRt", callStat.get(2));
            fields.put("errorCount", callStat.get(3));
            fields.put("hitCount", callStat.get(4));
            fields.put("totalQps", formatDouble(callStat.get(5)));
            fields.put("totalTps", formatDouble(callStat.get(6)));
            fields.put("total", callStat.get(7));
            fields.put("log_time", FormatUtils.toDateTimeSecondString(slotKey * 1000));
            fields.put("qps", callStat.get(5) / (double) callStat.get(7));
            fields.put("tps", callStat.get(6) / (double) callStat.get(7));
            if (callStat.get(0) == 0) {
                // 平均
                fields.put("rt", (double) callStat.get(2));
            } else {
                fields.put("rt", callStat.get(2) / (double) callStat.get(0));
            }
            fields.put("traceId", callStat.getTraceId());
            fields.put("time", System.currentTimeMillis());
            String tableName = clickHouseShardSupport.isCluster() ? metricsId : metricsId + "_all";
            clickHouseShardSupport.insert(fields, tags[0], tableName);
        } catch (Throwable e) {
            logger.error("write fail clickHouse " + ExceptionUtils.getStackTrace(e));
        }
        return true;
    }

    public static String formatString(String value) {
        return "null".equals(value) ? "" : value;
    }

    public static Double formatDouble(long value) {
        return Double.valueOf(value);
    }
}

