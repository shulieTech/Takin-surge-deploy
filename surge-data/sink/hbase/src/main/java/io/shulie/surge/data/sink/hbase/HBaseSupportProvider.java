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
import com.google.inject.Provider;
import com.google.inject.name.Named;
import io.shulie.surge.data.runtime.common.DataRuntime;

/**
 * @author pamirs
 */
public class HBaseSupportProvider implements Provider<HBaseSupport> {

    private AsyncHBaseSupportFactory factory;
    private HBaseSupportSpec spec;

    @Inject
    public HBaseSupportProvider(DataRuntime runtime, @Named("config.hbase.zk") String zkHosts,
                                @Named("config.hbase.zk.rootNode") String rootNode) {
        super();

        factory = runtime.getInstance(AsyncHBaseSupportFactory.class);
        spec = new HBaseSupportSpec();
        spec.setZkHosts(zkHosts);
        spec.setZkRootNode(rootNode);
        spec.setExceptionLoggingEnabled(true);
    }

    @Override
    public HBaseSupport get() {
        try {
            return factory.create(spec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
