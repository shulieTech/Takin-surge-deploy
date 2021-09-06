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
import com.google.common.collect.Sets;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.shulie.surge.data.common.aggregation.AggregateSlot;
import io.shulie.surge.data.common.aggregation.Aggregation;
import io.shulie.surge.data.common.aggregation.Scheduler;
import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.common.utils.FormatUtils;
import io.shulie.surge.data.common.utils.Pair;
import io.shulie.surge.data.deploy.pradar.common.PradarRtConstant;
import io.shulie.surge.data.deploy.pradar.common.PradarStormConfigHolder;
import io.shulie.surge.data.deploy.pradar.config.PradarSupplierConfiguration;
import io.shulie.surge.data.deploy.pradar.model.AmdbAppModel;
import io.shulie.surge.data.deploy.pradar.model.AmdbAppRelationModel;
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
import java.util.Set;

/**
 * @author pamirs
 */
public class E2ETraceReduceBolt extends BaseBasicBolt {
    private static Logger logger = LoggerFactory.getLogger(E2ETraceReduceBolt.class);

    private transient Aggregation<Metric, CallStat> aggregation;
    private transient Scheduler scheduler;
    private InfluxDBSupport influxDbSupport;
    private String metricsDataBase = "pradar";

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        try {
            PradarStormConfigHolder.init(stormConf);
            Injector injector = new PradarSupplierConfiguration(context.getThisWorkerPort())
                    .initDataRuntime().getInstance(Injector.class);
            influxDbSupport = injector.getInstance(InfluxDBSupport.class);
            scheduler = new Scheduler(1);
            aggregation = new Aggregation(PradarRtConstant.REDUCE_TRACE_SECONDS_INTERVAL,
                    PradarRtConstant.REDUCE_TRACE_SECONDS_LOWER_LIMIT);
            aggregation.start(scheduler, new TraceMetricsCommitAction(influxDbSupport, metricsDataBase));
        } catch (Throwable e) {
            logger.error("E2ETraceReduceBolt fail " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
        logger.info("E2ETraceReduceBolt started...");
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector basicOutputCollector) {
        if (!input.getSourceStreamId().equals(PradarRtConstant.REDUCE_E2E_TRACE_METRICS_STREAM_ID)) {
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

    /**
     * APP指标计算提交
     */
    static class TraceMetricsCommitAction implements Aggregation.CommitAction<Metric, CallStat> {
        private InfluxDBSupport influxDbSupport;
        private String metricsDataBase;

        public TraceMetricsCommitAction(InfluxDBSupport influxDbSupport, String metricsDataBase) {
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
                });

            } catch (Throwable e) {
                logger.error("write fail influxdb " + ExceptionUtils.getStackTrace(e));
            }
        }
    }
}
