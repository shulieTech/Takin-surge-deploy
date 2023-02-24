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
import io.shulie.surge.data.common.utils.FormatUtils;
import io.shulie.surge.data.deploy.pradar.common.HttpUtil;
import io.shulie.surge.data.deploy.pradar.common.PradarRtConstant;
import io.shulie.surge.data.sink.clickhouse.ClickHouseShardSupport;
import io.shulie.surge.data.sink.influxdb.InfluxDBSupport;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class AppRelationMetricsResultListener implements DefaultAggregator.ResultListener {

    private static Logger logger = LoggerFactory.getLogger(AppRelationMetricsResultListener.class);

    @Inject
    private ClickHouseShardSupport clickHouseShardSupport;

    @Inject
    @Named("instance.appRelationInfoSave.url")
    private String appRelationInfoSaveUrl;

    @Override
    public String metricId() {
        return "tro_pradar_app";
    }

    @Override
    public boolean fire(Long slotKey, Metric metric, CallStat callStat) {
        try {
            String metricsId = metric.getMetricId();
            String[] tags = metric.getPrefixes();

            Map<String, Object> fields = Maps.newHashMap();
            fields.put("fromAppName", tags[0]);
            fields.put("toAppName", tags[1]);
            // 总次数/成功次数/totalRt/错误次数/totalQps
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
            fields.put("log_time", FormatUtils.toDateTimeSecondString(slotKey * 1000));
            fields.put("time", System.currentTimeMillis());
            // 写入clickHouse
            String tableName = clickHouseShardSupport.isCluster() ? metricsId : metricsId + "_all";
            clickHouseShardSupport.insert(fields, tags[0], tableName);

            // 写入应用
            String fromAppName = tags[0];
            String toAppName = tags[1];
            String fromAppType = tags[2];
            String toAppType = tags[3];
            saveApp(fromAppName, fromAppType);
            saveApp(toAppName, toAppType);
            // 写入应用关系
            saveAppRelation(fromAppName, toAppName);
        } catch (Throwable e) {
            logger.error("write fail clickHouse " + ExceptionUtils.getStackTrace(e));
        }
        return true;
    }

    /**
     * 上报应用信息
     *
     * @param appName
     * @param appType
     */
    private void saveApp(String appName, String appType) {
    }

    /**
     * 上报应用关系信息
     *
     * @param fromAppName
     * @param toAppName
     */
    private void saveAppRelation(String fromAppName, String toAppName) {
        Map<String, Object> params = new HashMap<>();
        params.put("fromAppName", fromAppName);
        params.put("toAppName", toAppName);
        HttpUtil.sendPost(appRelationInfoSaveUrl, params);
    }
}
