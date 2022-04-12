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

import io.shulie.surge.data.runtime.common.DataRuntime;
import org.apache.storm.spout.SpoutOutputCollector;

public class ReportTaskServerSpec {
    private final int port;
    private final Map<String, String> netMap;
    private final Map<String, String> hostNameMap;
    private final SpoutOutputCollector collector;
    private final DataRuntime dataRuntime;

    public ReportTaskServerSpec(int port, Map<String, String> netMap, Map<String, String> hostNameMap,
        SpoutOutputCollector collector, DataRuntime dataRuntime) {
        this.port = port;
        this.netMap = netMap;
        this.hostNameMap = hostNameMap;
        this.collector = collector;
        this.dataRuntime = dataRuntime;
    }

    public int getPort() {
        return port;
    }

    public Map<String, String> getNetMap() {
        return netMap;
    }

    public Map<String, String> getHostNameMap() {
        return hostNameMap;
    }

    public SpoutOutputCollector getCollector() {
        return collector;
    }

    public DataRuntime getDataRuntime() {
        return dataRuntime;
    }
}
