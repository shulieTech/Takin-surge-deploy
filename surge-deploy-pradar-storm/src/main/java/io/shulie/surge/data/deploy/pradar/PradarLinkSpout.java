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
import io.shulie.surge.data.deploy.pradar.starter.PradarLinkStarter;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author vincent
 */
public class PradarLinkSpout extends BaseRichSpout {
    private static final Logger logger = LoggerFactory.getLogger(PradarLinkSpout.class);

    @Override
    public void open(Map conf, TopologyContext topologyContext, SpoutOutputCollector collector) {
        try {
            Map<String, Object> args = Maps.newHashMap(conf);
            PradarLinkStarter pradarLinkStarter = new PradarLinkStarter();
            pradarLinkStarter.init(args);
            pradarLinkStarter.start();
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
