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
public class NodeResultListener implements DefaultAggregator.ResultListener {
    private static Logger logger = LoggerFactory.getLogger(NodeResultListener.class);

    @Inject
    private InfluxDBSupport influxDbSupport;

    @Inject
    @Named("config.influxdb.database.metircs")
    private String metricsDataBase;

    @Override
    public String metricId() {
        return "app_stat_gc";
    }

    @Override
    public boolean fire(Long slotKey, Metric metric, CallStat callStat) {
        try {
            String metricsId = metric.getMetricId();
            String[] tags = metric.getPrefixes();

            Map<String, String> influxdbTags = Maps.newHashMap();
            influxdbTags.put("appName", tags[0]);
            influxdbTags.put("ip", tags[1]);
            influxdbTags.put("host", tags[2]);
            influxdbTags.put("agentId", tags[3]);
            influxdbTags.put("tenantCode", tags[4]);
            influxdbTags.put("envCode", tags[5]);

            // 堆使用/eden 使用/survivor使用/old 区使用/metaspace 使用/codeCache 使用/nonHeap 使用/cpu 使用率/younggc次数/younggc 耗时
            // fullGc 次数/fullGc耗时/内存使用/ 新建线程数/运行中线程数/blocked 线程数/等待线程数/timed 等待线程数/terminated 线程数
            Map<String, Object> fields = Maps.newHashMap();
            fields.put("heapUsed", callStat.get(0));
            fields.put("edenUsed", callStat.get(1));
            fields.put("survivorUsed", callStat.get(2));
            fields.put("oldUsed", callStat.get(3));
            fields.put("metaspaceUsed", callStat.get(4));
            fields.put("codeCacheUsed", callStat.get(5));
            fields.put("nonHeapUsed", callStat.get(6));
            fields.put("coresPercent", callStat.get(7));
            fields.put("youngGcCount", callStat.get(8));
            fields.put("youngGcCost", callStat.get(9));
            fields.put("fullGcCount", callStat.get(10));
            fields.put("fullGcCost", callStat.get(11));
            fields.put("memoryUsed", callStat.get(12));
            fields.put("threadCount", callStat.get(13));
            fields.put("threadNewCount", callStat.get(14));
            fields.put("threadDeadlockCount", callStat.get(15));
            fields.put("threadRunnableCount", callStat.get(16));
            fields.put("threadBlockedCount", callStat.get(17));
            fields.put("threadWaitCount", callStat.get(18));
            fields.put("threadTimedWaitCount", callStat.get(19));
            fields.put("threadTerminatedCount", callStat.get(20));
            influxDbSupport.write(metricsDataBase, metricsId, influxdbTags, fields, slotKey * 1000);
        } catch (Throwable e) {
            logger.error("write fail influxdb " + ExceptionUtils.getStackTrace(e));
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

