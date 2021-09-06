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
import io.shulie.surge.data.deploy.pradar.config.PradarSupplierConfiguration;
import io.shulie.surge.data.deploy.pradar.model.AmdbAppModel;
import io.shulie.surge.data.deploy.pradar.model.AmdbAppRelationModel;
import io.shulie.surge.data.sink.influxdb.InfluxDBSupport;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
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
import java.util.stream.Collectors;

/**
 * @author zhouyuan
 */
public class PradarAppRelationTraceReduceBolt extends BaseBasicBolt {
    private static Logger logger = LoggerFactory.getLogger(PradarAppRelationTraceReduceBolt.class);
    private transient Aggregation<Metric, CallStat> aggregation;
    private transient Scheduler scheduler;
    @Inject
    private MysqlSupport mysqlSupport;
    @Inject
    private InfluxDBSupport influxDbSupport;

    private static final String AMDB_APP = "t_amdb_app";
    private static final String AMDB_APP_RELATION = "t_amdb_app_relation";
    private String appInsertSql = "";
    private String appRelationInsertSql = "";


    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        try {
            PradarStormConfigHolder.init(stormConf);
            init();
            Injector injector = new PradarSupplierConfiguration(context.getThisWorkerPort())
                    .initDataRuntime().getInstance(Injector.class);
            influxDbSupport = injector.getInstance(InfluxDBSupport.class);
            mysqlSupport = injector.getInstance(MysqlSupport.class);
            scheduler = new Scheduler(1);
            aggregation = new Aggregation(PradarRtConstant.REDUCE_TRACE_SECONDS_INTERVAL, PradarRtConstant.REDUCE_TRACE_SECONDS_LOWER_LIMIT);
            aggregation.start(scheduler, new TraceMetricsCommitAction(this.appInsertSql,
                    this.appRelationInsertSql, influxDbSupport, mysqlSupport));
        } catch (Throwable e) {
            logger.error("PradarAppRelationTraceReduceBolt fail " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
        logger.info("PradarAppRelationTraceReduceBolt started...");
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector basicOutputCollector) {
        if (!input.getSourceStreamId().equals(PradarRtConstant.REDUCE_APP_RELATION_TRACE_METRICS_STREAM_ID)) {
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
        private MysqlSupport mysqlSupport;
        private String appInsertSql;
        private String appRelationInsertSql;

        public TraceMetricsCommitAction(String appInsertSql,
                                        String appRelationInsertSql,
                                        InfluxDBSupport influxDbSupport,
                                        MysqlSupport mysqlSupport) {
            this.appInsertSql = appInsertSql;
            this.appRelationInsertSql = appRelationInsertSql;
            this.influxDbSupport = influxDbSupport;
            this.mysqlSupport = mysqlSupport;
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
                    influxdbTags.put("fromAppName", tags[0]);
                    influxdbTags.put("toAppName", tags[1]);
                    // 总次数/成功次数/totalRt/错误次数/totalQps
                    Map<String, Object> fields = Maps.newHashMap();
                    fields.put("totalCount", callStat.get(0));
                    fields.put("successCount", callStat.get(1));
                    fields.put("totalRt", callStat.get(2));
                    fields.put("errorCount", callStat.get(3));
                    fields.put("totalQps", callStat.get(4));
                    // 写入influxDB
                    influxDbSupport.write("pradar", metricsId, influxdbTags, fields, slotKey * 1000);

                    AmdbAppModel fromApp = new AmdbAppModel(tags[0], tags[2]);
                    AmdbAppModel toApp = new AmdbAppModel(tags[1], tags[3]);
                    AmdbAppRelationModel amdbAppRelationModel = new AmdbAppRelationModel(tags[0], tags[1]);

                    appSet.add(fromApp);
                    appSet.add(toApp);
                    appRelationSet.add(amdbAppRelationModel);
                });

                mysqlSupport.batchUpdate(appInsertSql, appSet.stream().map(AmdbAppModel::getValues).collect(Collectors.toList()));
                mysqlSupport.batchUpdate(appRelationInsertSql, appRelationSet.stream().map(AmdbAppRelationModel::getValues).collect(Collectors.toList()));
            } catch (Throwable e) {
                logger.error("write fail influxdb " + ExceptionUtils.getStackTrace(e));
            }
        }
    }

    public void init() {
        appInsertSql = "INSERT IGNORE INTO " + AMDB_APP + AmdbAppModel.getCols() + " VALUES " + AmdbAppModel.getParamCols();
        appRelationInsertSql = "INSERT IGNORE INTO " + AMDB_APP_RELATION + AmdbAppRelationModel.getCols() + " VALUES " + AmdbAppRelationModel.getParamCols();
    }
}


