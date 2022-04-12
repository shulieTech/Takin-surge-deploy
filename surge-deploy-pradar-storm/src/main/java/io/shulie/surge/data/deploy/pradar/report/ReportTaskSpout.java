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

import io.shulie.surge.data.deploy.pradar.common.ParamUtil;
import io.shulie.surge.data.deploy.pradar.common.PradarStormConfigHolder;
import io.shulie.surge.data.runtime.common.DataRuntime;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.shulie.surge.data.deploy.pradar.report.PradarReportTaskTopology.ACTIVITY_STREAM_ID;

public class ReportTaskSpout extends BaseRichSpout {

    private static final Logger logger = LoggerFactory.getLogger(ReportTaskSpout.class);

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        PradarStormConfigHolder.init(conf);
        PradarReportConfiguration configuration = new PradarReportConfiguration(String.valueOf(conf.get(ParamUtil.NET)),
            String.valueOf(conf.get(ParamUtil.HOSTNAME)));
        DataRuntime dataRuntime = configuration.initDataRuntime();
        try {
            startServer(configuration, collector, dataRuntime);
        } catch (Exception e) {
            throw new RuntimeException("压测报告任务server启动失败");
        }
    }

    @Override
    public void nextTuple() {
        try {
            synchronized (this) {
                this.wait(1000);
            }
        } catch (InterruptedException e) {
            logger.warn("ReportTaskSpout nextTuple() is interrupted");
        }
    }

    public void startServer(PradarReportConfiguration configuration,
        SpoutOutputCollector collector, DataRuntime dataRuntime) throws Exception {
        ReportTaskServerSpec spec = new ReportTaskServerSpec(configuration.getServerPort(), configuration.getNetMap(),
            configuration.getHostNameMap(), collector, dataRuntime);
        new ReportTaskServer(spec).startServer();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream(ACTIVITY_STREAM_ID, new Fields("reportId"));
    }
}
