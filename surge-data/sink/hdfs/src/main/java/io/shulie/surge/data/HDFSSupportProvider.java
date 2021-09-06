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

package io.shulie.surge.data;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import io.shulie.surge.data.runtime.common.DataRuntime;

/**
 * @author pamirs
 */
public class HDFSSupportProvider implements Provider<HDFSSupport> {
    private RollingHDFSSupportFactory factory;
    private HDFSSupportSpec spec;

    @Inject
    public HDFSSupportProvider(DataRuntime runtime,@Named("config.pointName") String pointName) {
        super();
        factory = runtime.getInstance(RollingHDFSSupportFactory.class);
        spec = new HDFSSupportSpec();
        spec.setDir("hdfs:///group/log/" + pointName + "/");
        spec.setFileName("");
        spec.setDateFormat("yyyy/MM/dd/'log'_HH");
    }

    @Override
    public HDFSSupport get() {
        try {
            return factory.create(spec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
