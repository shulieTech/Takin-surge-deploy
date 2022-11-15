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
import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.common.utils.Pair;
import io.shulie.surge.data.deploy.pradar.common.DataBootstrapEnhancer;
import io.shulie.surge.data.deploy.pradar.common.PradarRtConstant;
import io.shulie.surge.data.deploy.pradar.config.PradarReduceConfiguration;
import io.shulie.surge.data.runtime.common.DataBootstrap;
import io.shulie.surge.data.runtime.common.DataRuntime;
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
 * @author pamirs
 */
public class E2ETraceReduceBolt extends BaseBasicBolt {
    private static Logger logger = LoggerFactory.getLogger(E2ETraceReduceBolt.class);
    private PradarReduceConfiguration pradarReduceConfiguration;

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        try {
            Map<String, Object> args = Maps.newHashMap(stormConf);
            args.put("receivers", "e2e");
            DataBootstrap bootstrap = DataBootstrap.create("deploy.properties", "pradar");
            DataBootstrapEnhancer.enhancer(bootstrap);
            pradarReduceConfiguration = new PradarReduceConfiguration();
            pradarReduceConfiguration.initArgs(args);
            pradarReduceConfiguration.install(bootstrap);
            DataRuntime dataRuntime = bootstrap.startRuntime();
            pradarReduceConfiguration.doAfterInit(dataRuntime);

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
        pradarReduceConfiguration.getE2eReceiver().execute(slotKey, job);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }

}
