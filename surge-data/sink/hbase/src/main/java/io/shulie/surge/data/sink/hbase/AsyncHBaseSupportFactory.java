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

package io.shulie.surge.data.sink.hbase;

import com.google.inject.Inject;
import io.shulie.surge.data.common.factory.GenericFactory;
import io.shulie.surge.data.common.lifecycle.StopLevel;
import io.shulie.surge.data.runtime.common.DataRuntime;

import java.util.HashMap;
import java.util.Map;

public class AsyncHBaseSupportFactory implements GenericFactory<HBaseSupport, HBaseSupportSpec> {

    private static final int DEFAULT_FLUSH_INTERVAL = 250;

    @Inject
    private DataRuntime runtime;

    private static Map<String, AsyncHBaseSupport> hbaseSupportMap = new HashMap<String, AsyncHBaseSupport>();

    @Override
    public HBaseSupport create(HBaseSupportSpec hbaseSpec) throws Exception {
        synchronized (AsyncHBaseSupportFactory.class) {
            String zkServers = hbaseSpec.getZkHosts();
            boolean traceExceptions = hbaseSpec.isExceptionLoggingEnabled();
            int flushInterval = hbaseSpec.getFlushInterval() > 0 ?
                    hbaseSpec.getFlushInterval() : DEFAULT_FLUSH_INTERVAL;

            if (hbaseSupportMap.containsKey(zkServers)) {
                return hbaseSupportMap.get(zkServers);
            }
            AsyncHBaseSupport hbaseSupport = new AsyncHBaseSupport(zkServers, hbaseSpec.getZkRootNode());
            hbaseSupport.setTraceExceptions(traceExceptions);
            hbaseSupport.setFlushInterval((short) flushInterval);
            runtime.inject(hbaseSupport);
            runtime.registShutdownCall(hbaseSupport, StopLevel.SUPPORT);

            hbaseSupportMap.put(zkServers, hbaseSupport);

            return hbaseSupport;
        }
    }
}
