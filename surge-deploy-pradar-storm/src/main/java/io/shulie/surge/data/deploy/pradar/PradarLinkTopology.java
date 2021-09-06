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
import io.shulie.surge.data.deploy.pradar.common.ParamUtil;
import io.shulie.surge.data.deploy.pradar.common.StormConfig;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.topology.TopologyBuilder;

import java.util.Map;

/**
 * @author xingchen
 * @desc 链路梳理相关功能模块
 */
public class PradarLinkTopology {
    public static TopologyBuilder createLogBuilder(int workers) {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(PradarLinkSpout.class.getSimpleName(), new PradarLinkSpout(), workers);
        return builder;
    }

    /**
     * 启动
     *
     * @param args
     * @throws InvalidTopologyException
     * @throws AuthorizationException
     * @throws AlreadyAliveException
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> inputMap = Maps.newHashMap();
        ParamUtil.parseInputParam(inputMap, args);
        Integer workers = Integer.valueOf(inputMap.get(ParamUtil.WORKERS));
        //移除无关参数
        inputMap.remove(ParamUtil.REGISTERZK);

        Config config = StormConfig.createConfig(workers);
        config.putAll(inputMap);

        TopologyBuilder topologyBuilder = createLogBuilder(workers);
        StormSubmitter.submitTopology(PradarLinkTopology.class.getSimpleName(), config, topologyBuilder.createTopology());
    }
}

