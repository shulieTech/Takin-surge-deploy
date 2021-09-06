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
import com.google.inject.name.Named;
import io.shulie.surge.data.common.aggregation.DefaultAggregator;
import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.deploy.pradar.common.PradarRtConstant;
import io.shulie.surge.data.sink.influxdb.InfluxDBSupport;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class E2EMetricsResultListener implements DefaultAggregator.ResultListener {

    private static Logger logger = LoggerFactory.getLogger(E2EMetricsResultListener.class);

    @Inject
    private InfluxDBSupport influxDbSupport;

    @Inject
    @Named("config.influxdb.database.metircs")
    private String metricsDataBase;

    @Override
    public String metricId() {
        return "tro_pradar_app";
    }

    @Override
    public boolean fire(Long slotKey, Metric metric, CallStat callStat) {
        try {
            String metricsId = metric.getMetricId();
            String[] tags = metric.getPrefixes();
            //nodeId, parsedAppName, parsedServiceName, parsedMethod, rpcType, clusterTest

            Map<String, String> influxdbTags = Maps.newHashMap();
            influxdbTags.put("nodeId", tags[0]);
            influxdbTags.put("parsedAppName", tags[1]);
            influxdbTags.put("parsedServiceName", tags[2]);
            influxdbTags.put("parsedMethod", tags[3]);
            influxdbTags.put("rpcType", tags[4]);
            influxdbTags.put("clusterTest", tags[5]);
            influxdbTags.put("exceptionType", tags[6]);
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
            // 写入influxDB
            influxDbSupport.write(metricsDataBase, metricsId, influxdbTags, fields, slotKey * 1000);
        } catch (Throwable e) {
            logger.error("write fail influxdb " + ExceptionUtils.getStackTrace(e));
        }
        return true;
    }

}
