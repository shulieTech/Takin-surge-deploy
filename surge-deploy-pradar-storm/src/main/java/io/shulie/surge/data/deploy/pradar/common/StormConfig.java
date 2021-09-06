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

package io.shulie.surge.data.deploy.pradar.common;


import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.common.utils.Pair;
import org.apache.storm.Config;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Pradar Storm 配置
 *
 * @author xingchen
 */
public class StormConfig {
    /**
     * @param workCount
     * @return
     */
    public static Config createConfig(int workCount) {
        Config conf = new Config();
        // 获取外部的worker数,默认起1个
        conf.setNumWorkers(workCount);
        conf.setNumAckers(0);
        conf.setFallBackOnJavaSerialization(false);
        conf.registerSerialization(String.class);
        conf.registerSerialization(String[].class);
        conf.registerSerialization(long[].class);
        conf.registerSerialization(char[].class);
        conf.registerSerialization(ArrayList.class);
        conf.registerSerialization(HashMap.class);
        conf.registerSerialization(Metric.class);
        conf.registerSerialization(CallStat.class);
        conf.registerSerialization(Pair.class);

        conf.put("topology.worker.gc.childopts",
                "-Xms2g -Xmx2g -XX:MaxDirectMemorySize=512m -XX:+HeapDumpOnOutOfMemoryError "
                        + "-XX:HeapDumpPath=java.hprof -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=256m -XX:-OmitStackTraceInFastThrow ");
        conf.put(Config.NIMBUS_THRIFT_PORT, 6627);
        conf.put(Config.STORM_THRIFT_TRANSPORT_PLUGIN, "org.apache.storm.security.auth.SimpleTransportPlugin");
        conf.put(Config.STORM_ZOOKEEPER_PORT, 2181);
        conf.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS, 300);
        return conf;
    }
}
