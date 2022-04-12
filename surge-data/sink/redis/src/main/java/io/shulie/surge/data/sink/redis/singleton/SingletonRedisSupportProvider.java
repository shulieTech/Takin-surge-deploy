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
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.shulie.surge.data.runtime.common.DataRuntime;
import io.shulie.surge.data.sink.redis.RedisSupport;

@Singleton
public class SingletonRedisSupportProvider implements Provider<RedisSupport> {

    private final SingletonRedisSupportFactory factory;
    private final SingletonRedisSupportSpec spec;

    @Inject
    public SingletonRedisSupportProvider(DataRuntime runtime,
        @Named("config.redis.host") String host,
        @Named("config.redis.port") int port,
        @Named("config.redis.password") String password) {
        factory = runtime.getInstance(SingletonRedisSupportFactory.class);
        spec = new SingletonRedisSupportSpec();
        spec.setHost(host);
        spec.setPort(port);
        spec.setPassword(password);
    }

    @Override
    public RedisSupport get() {
        return factory.create(spec);
    }
}
