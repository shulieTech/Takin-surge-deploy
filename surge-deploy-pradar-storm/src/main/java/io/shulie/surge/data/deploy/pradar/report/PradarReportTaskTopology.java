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

package io.shulie.surge.data.deploy.pradar.report;

import java.util.Map;

import com.google.common.collect.Maps;
import io.shulie.surge.data.deploy.pradar.common.ParamUtil;
import io.shulie.surge.data.deploy.pradar.common.StormConfig;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;

/**
 * 处理压测报告数据统计的topology
 */
public class PradarReportTaskTopology {

    public static final String ACTIVITY_STREAM_ID = "activity";
    public static final String ACTIVITY_SERVICE_STREAM_ID = "activity-service";
    public static final String ACTIVITY_SERVICE_METRICS_STREAM_ID = "activity-service-metrics";

    public static void main(String[] args) throws Exception {
        Map<String, String> inputMap = Maps.newHashMap();
        ParamUtil.parseInputParam(inputMap, args);
        int workers = Integer.parseInt(inputMap.get(ParamUtil.WORKERS));
        Config config = StormConfig.createConfig(workers);
        config.putAll(inputMap);
        TopologyBuilder builder = createReportBuilder(workers);
        if (inputMap.containsKey(ParamUtil.TOPOLOGY_NAME)) {
            StormSubmitter.submitTopology(inputMap.get(ParamUtil.TOPOLOGY_NAME), config, builder.createTopology());
        } else {
            StormSubmitter.submitTopology(PradarReportTaskTopology.class.getSimpleName(), config, builder.createTopology());
        }
    }

    public static TopologyBuilder createReportBuilder(int workers) {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(ReportTaskSpout.class.getSimpleName(), new ReportTaskSpout(), 1);
        builder.setBolt(ReportActivityBolt.class.getSimpleName(), new ReportActivityBolt(), 1).shuffleGrouping(
            ReportTaskSpout.class.getSimpleName(), ACTIVITY_STREAM_ID);
        builder.setBolt(ReportActivityServiceBolt.class.getSimpleName(), new ReportActivityServiceBolt(), workers << 1)
            .directGrouping(ReportActivityBolt.class.getSimpleName(), ACTIVITY_SERVICE_STREAM_ID);
        builder.setBolt(ReportServiceMetricsBolt.class.getSimpleName(), new ReportServiceMetricsBolt(), workers << 2)
            .directGrouping(ReportActivityServiceBolt.class.getSimpleName(), ACTIVITY_SERVICE_METRICS_STREAM_ID);
        return builder;
    }
}
