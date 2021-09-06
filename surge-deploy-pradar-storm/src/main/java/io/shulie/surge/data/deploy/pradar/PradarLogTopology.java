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
import io.shulie.surge.data.deploy.pradar.common.PradarRtConstant;
import io.shulie.surge.data.deploy.pradar.common.StormConfig;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;

import java.util.Map;

public class PradarLogTopology {
    public static TopologyBuilder createLogBuilder(int workers, Boolean isGeneralVersion) {
        TopologyBuilder builder = new TopologyBuilder();
        // reduce数为spout的一半
        Double reduceCount = Math.ceil(Double.valueOf(workers) / 2);
        builder.setSpout(PradarLogSpout.class.getSimpleName(), new PradarLogSpout(), workers);
        if (!isGeneralVersion) {
            // TODO 使用trace记录metrics,通用版本里面暂时不计算流量,可通过配置打开
            builder.setBolt(PradarTraceReduceBolt.class.getSimpleName(), new PradarTraceReduceBolt(), reduceCount.intValue())
                    .directGrouping(PradarLogSpout.class.getSimpleName(), PradarRtConstant.REDUCE_TRACE_METRICS_STREAM_ID);
        }
        // E2E巡检指标计算
        builder.setBolt(E2ETraceReduceBolt.class.getSimpleName(), new E2ETraceReduceBolt(), reduceCount.intValue())
                .directGrouping(PradarLogSpout.class.getSimpleName(), PradarRtConstant.REDUCE_E2E_TRACE_METRICS_STREAM_ID);
        return builder;
    }

    /**
     * 启动的时候，需要设置主机名和发布的ip
     * storm部署在docker中的时候，docker的时候解析不了ip，或未准确读取到网卡信息
     * 设置系统参数,按分号分隔
     *
     * @param args
     * @throws Exception 目前可以设置
     *                   -DWorkers=2
     *                   -DCoreSize=1   设置ringbuffer的线程数一般默认
     *                   -DHostName={}   设置主机名和ip的映射
     *                   -DNet={}        设置外网的映射
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> inputMap = Maps.newHashMap();
        ParamUtil.parseInputParam(inputMap, args);
        Integer workers = Integer.valueOf(inputMap.get(ParamUtil.WORKERS));
        Boolean generalVersion = Boolean.valueOf(inputMap.get(ParamUtil.GENERAL_VERSION));
        Config config = StormConfig.createConfig(workers);
        config.putAll(inputMap);
        TopologyBuilder topologyBuilder = createLogBuilder(workers, generalVersion);

        StormSubmitter.submitTopology(PradarLogTopology.class.getSimpleName(), config, topologyBuilder.createTopology());
    }
}

