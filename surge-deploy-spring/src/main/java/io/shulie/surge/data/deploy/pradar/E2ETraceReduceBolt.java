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
    public void prepare() {
        try {
            Injector injector = new PradarSupplierConfiguration()
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


}
