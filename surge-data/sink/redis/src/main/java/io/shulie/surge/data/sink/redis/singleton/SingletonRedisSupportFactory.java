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

package io.shulie.surge.data.sink.redis.singleton;

import com.google.inject.Inject;
import io.shulie.surge.data.common.factory.GenericFactory;
import io.shulie.surge.data.common.lifecycle.StopLevel;
import io.shulie.surge.data.runtime.common.DataRuntime;
import io.shulie.surge.data.sink.redis.RedisSupport;

public class SingletonRedisSupportFactory implements GenericFactory<RedisSupport, SingletonRedisSupportSpec> {

    @Inject
    private DataRuntime runtime;

    @Override
    public RedisSupport create(SingletonRedisSupportSpec spec) {
        synchronized (SingletonRedisSupportFactory.class) {
            SingletonRedisSupport support = new SingletonRedisSupport(spec.getHost(), spec.getPort(), spec.getPassword());
            runtime.inject(support);
            runtime.registShutdownCall(support, StopLevel.SUPPORT);
            return support;
        }
    }
}
