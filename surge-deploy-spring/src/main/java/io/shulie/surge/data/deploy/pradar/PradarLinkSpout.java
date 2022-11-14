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

import io.shulie.surge.data.deploy.pradar.common.ParamUtil;
import io.shulie.surge.data.deploy.pradar.common.PradarStormConfigHolder;
import io.shulie.surge.data.deploy.pradar.config.PradarLinkConfiguration;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author vincent
 */
public class PradarLinkSpout extends BaseRichSpout {
    private static final Logger logger = LoggerFactory.getLogger(PradarLinkSpout.class);

    @Override
    public void open(Map conf, TopologyContext topologyContext, SpoutOutputCollector collector) {
        PradarStormConfigHolder.init(conf);
        PradarLinkConfiguration pradarLinkConfiguration = new PradarLinkConfiguration(
                conf.get(ParamUtil.DATA_SOURCE_TYPE));
        try {
            List<Integer> taskIds = topologyContext.getComponentTasks(PradarLinkSpout.class.getSimpleName());
            List<String> ids = taskIds.stream().map(id -> String.valueOf(id)).collect(Collectors.toList());
            Integer currentTaskId = topologyContext.getThisTaskId();
            pradarLinkConfiguration.initWithTaskSize(ids, String.valueOf(currentTaskId));
        } catch (Throwable e) {
            throw new RuntimeException("fail to start PradarLinkSpout", e);
        }
        logger.info("PradarLinkSpout start successfull...");
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
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }
}
