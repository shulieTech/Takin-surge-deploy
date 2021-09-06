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
import io.shulie.surge.data.common.factory.GenericFactory;
import io.shulie.surge.data.common.lifecycle.StopLevel;
import io.shulie.surge.data.runtime.common.DataRuntime;
import io.shulie.surge.data.runtime.common.TaskIdentifier;

import java.util.HashMap;
import java.util.Map;

public class RollingHDFSSupportFactory implements GenericFactory<HDFSSupport, HDFSSupportSpec> {
    private static Map<String, RollingHDFSSupport> pathToSupportMap = new HashMap<>();

    @Inject
    private DataRuntime runtime;

    @Inject
    private TaskIdentifier taskIdentifier;

    @Override
    public HDFSSupport create(HDFSSupportSpec spec) {
        synchronized (RollingHDFSSupportFactory.class) {
            String staticPath = spec.getDir() + spec.getFileName();
            RollingHDFSSupport support = pathToSupportMap.get(staticPath);
            if (support == null) {
                // Support 是进程级别，因此使用了 worker Id
                support = new RollingHDFSSupport(spec.getDir(),
                        spec.getFileName(),
                        spec.getDateFormat(),
                        taskIdentifier.getWorkerId());
                pathToSupportMap.put(staticPath, support);
                runtime.registShutdownCall(support, StopLevel.SUPPORT);
            }
            return support;
        }
    }
}
