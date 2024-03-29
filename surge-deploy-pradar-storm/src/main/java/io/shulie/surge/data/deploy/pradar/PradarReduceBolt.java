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

package io.shulie.surge.data.deploy.pradar;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.shulie.surge.data.common.aggregation.AggregateSlot;
import io.shulie.surge.data.common.aggregation.Aggregation;
import io.shulie.surge.data.common.aggregation.Scheduler;
import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.common.utils.FormatUtils;
import io.shulie.surge.data.common.utils.Pair;
import io.shulie.surge.data.deploy.pradar.common.PradarRtConstant;
import io.shulie.surge.data.deploy.pradar.common.PradarStormConfigHolder;
import io.shulie.surge.data.deploy.pradar.common.StringUtil;
import io.shulie.surge.data.deploy.pradar.config.PradarSupplierConfiguration;
import io.shulie.surge.data.sink.influxdb.InfluxDBSupport;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class PradarReduceBolt extends BaseBasicBolt {
    private static Logger logger = LoggerFactory.getLogger(PradarReduceBolt.class);
    private transient Aggregation aggregation;
    private transient Scheduler scheduler;
    @Inject
    private InfluxDBSupport influxDbSupport;

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        try {
            PradarStormConfigHolder.init(stormConf);
            Injector injector = new PradarSupplierConfiguration(context.getThisWorkerPort()).initDataRuntime().getInstance(Injector.class);
            influxDbSupport = injector.getInstance(InfluxDBSupport.class);

            scheduler = new Scheduler(1);
            aggregation = new Aggregation(PradarRtConstant.REDUCE_TRACE_SECONDS_INTERVAL,
                    PradarRtConstant.REDUCE_TRACE_SECONDS_LOWER_LIMIT);

            aggregation.start(scheduler, new MetricsCommitAction(influxDbSupport));
        } catch (Throwable e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw e;
        }
        logger.info("PradarReduceBolt started...");
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector basicOutputCollector) {
        if (!input.getSourceStreamId().equals(PradarRtConstant.REDUCE_METRICS_STREAM_ID)) {
            return;
        }
        Long slotKey = input.getLong(0);
        List<Pair<Metric, CallStat>> job = (List<Pair<Metric, CallStat>>) input.getValue(1);
        AggregateSlot<Metric, CallStat> slot = aggregation.getSlotByTimestamp(slotKey);
        if (slot != null) {
            for (Pair<Metric, CallStat> pair : job) {
                slot.addToSlot(pair.getFirst(), pair.getSecond());
            }
        } else {
            logger.info("no slot for " + slotKey + ": " + FormatUtils.toSecondTimeString(slotKey * 1000));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }

    static class MetricsCommitAction implements Aggregation.CommitAction<Metric, CallStat> {
        private static Logger logger = LoggerFactory.getLogger(PradarTraceReduceBolt.TraceMetricsCommitAction.class);
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
    }

    public static Double formatDouble(long value) {
        return Double.valueOf(value);
    }
}
