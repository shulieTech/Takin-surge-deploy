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
import io.shulie.surge.data.JettySupplierObserver;
import io.shulie.surge.data.deploy.pradar.collector.OutputCollector;
import io.shulie.surge.data.deploy.pradar.common.PradarRtConstant;
import io.shulie.surge.data.deploy.pradar.config.PradarConfiguration;
import io.shulie.surge.data.deploy.pradar.starter.PradarSupplierStarter;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static io.shulie.surge.data.JettySupplier.registedPort;

/**
 * @author vincent
 */

public class PradarLogSpout extends BaseRichSpout {
    private static Logger logger = LoggerFactory.getLogger(PradarLogSpout.class);

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        try {

            Map<String, Object> args = Maps.newHashMap(map);
            args.put("workerPort", topologyContext.getThisWorkerPort());
            PradarSupplierStarter pradarSupplierStarter = new PradarSupplierStarter();
            pradarSupplierStarter.init(args);
            PradarConfiguration pradarSupplierConfiguration = pradarSupplierStarter.getPradarConfiguration();
            pradarSupplierConfiguration.collector(new OutputCollector() {
                @Override
                public List<Integer> getReduceIds() {
                    return topologyContext.getThisWorkerTasks();
                }

                @Override
                public void emit(int partition, String streamId, Object... values) {
                    spoutOutputCollector.emitDirect(partition, streamId, new Values(values));
                }
            });
            pradarSupplierStarter.start();
        } catch (Exception e) {
            logger.error("Start runtime error.", e);
        }
        logger.info("PradarLogSpout start successfull...");
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

    @Override
    public void close() {
        logger.info("registered port:{}", registedPort);
        if (!registedPort.isEmpty()) {
            for (int i = 0; i < registedPort.size(); i++) {
                try {
                    JettySupplierObserver.offlineNotify(registedPort.get(i));
                    logger.info("jetty service offline success:{}", registedPort.get(i));
                } catch (Exception e) {
                    logger.error("jetty service offline notify gateway failed :{},stack:{}", e, e.getStackTrace());
                    continue;
                }
            }
        }
        super.close();
    }
}
