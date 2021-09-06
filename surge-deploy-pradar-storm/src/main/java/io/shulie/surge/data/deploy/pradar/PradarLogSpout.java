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

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.shulie.surge.data.common.aggregation.Scheduler;
import io.shulie.surge.data.deploy.pradar.agg.E2ETraceMetricsAggarator;
import io.shulie.surge.data.deploy.pradar.agg.TraceMetricsAggarator;
import io.shulie.surge.data.deploy.pradar.common.*;
import io.shulie.surge.data.deploy.pradar.config.PradarSupplierConfiguration;
import io.shulie.surge.data.runtime.common.DataRuntime;
import io.shulie.surge.data.suppliers.nettyremoting.NettyRemotingSupplier;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author vincent
 */

public class PradarLogSpout extends BaseRichSpout {
    private static Logger logger = LoggerFactory.getLogger(PradarLogSpout.class);
    @Inject
    private TraceMetricsAggarator traceMetricsAggarator;
    @Inject
    private E2ETraceMetricsAggarator e2eTraceMetricsAggarator;
    @Inject
    private EagleLoader eagleLoader;
    @Inject
    private RuleLoader ruleLoader;

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        PradarStormConfigHolder.init(map);
        PradarSupplierConfiguration pradarSupplierConfiguration =
                new PradarSupplierConfiguration(
                        topologyContext.getThisWorkerPort(),
                        map.get(ParamUtil.NET),
                        map.get(ParamUtil.HOSTNAME),
                        map.get(ParamUtil.REGISTERZK),
                        map.get(ParamUtil.CORE_SIZE),
                        map.get(ParamUtil.DATA_SOURCE_TYPE),
                        map.get(ParamUtil.PORTS),
                        map.get(ParamUtil.GENERAL_VERSION));
        try {
            DataRuntime dataRuntime = pradarSupplierConfiguration.initDataRuntime();
            PradarStormSupplierConfiguration pradarStormSupplierConfiguration = new PradarStormSupplierConfiguration(
                    pradarSupplierConfiguration.getNetMap(), pradarSupplierConfiguration.getHostNameMap(),
                    pradarSupplierConfiguration.isRegisterZk(), pradarSupplierConfiguration.getCoreSize(),
                    pradarSupplierConfiguration.getDataSourceType(),
                    pradarSupplierConfiguration.getServerPortsMap(),
                    pradarSupplierConfiguration.isGeneralVersion());

            NettyRemotingSupplier nettyRemotingSupplier = pradarStormSupplierConfiguration.buildSupplier(dataRuntime, true);
            Injector injector = dataRuntime.getInstance(Injector.class);
            injector.injectMembers(this);
            /**
             * 初始化metrics聚合任务。此处注入和diggest同一个对象
             */
            if (!pradarSupplierConfiguration.isGeneralVersion()) {
                traceMetricsAggarator.init(new Scheduler(1), spoutOutputCollector, topologyContext);
            }
            e2eTraceMetricsAggarator.init(new Scheduler(1), spoutOutputCollector, topologyContext);
            // 初始化边缓存
            eagleLoader.init();
            ruleLoader.init();
            nettyRemotingSupplier.start();
        } catch (Throwable e) {
            throw new RuntimeException("fail to start PradarLogSpout", e);
        }
        logger.info("PradarLogSpout starte successfull...");
    }

    @Override
    public void nextTuple() {
        try {
            synchronized (this) {
                this.wait(1000);
            }
        } catch (InterruptedException e1) {
            logger.warn("nextTuple() is interrupted");
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declareStream(PradarRtConstant.REDUCE_TRACE_METRICS_STREAM_ID, new Fields("slotKey", "job"));
        outputFieldsDeclarer.declareStream(PradarRtConstant.REDUCE_E2E_TRACE_METRICS_STREAM_ID, new Fields("slotKey", "job"));
    }
}
