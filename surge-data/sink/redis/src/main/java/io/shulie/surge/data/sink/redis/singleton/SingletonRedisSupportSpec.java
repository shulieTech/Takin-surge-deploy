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

import io.shulie.surge.data.common.factory.GenericFactorySpec;
import io.shulie.surge.data.sink.redis.RedisSupport;

public class SingletonRedisSupportSpec implements GenericFactorySpec<RedisSupport> {

    private String host;
    private int port;
    private String password;

    @Override
    public String factoryName() {
        return "DefaultSingletonRedis";
    }

    @Override
    public Class<RedisSupport> productClass() {
        return RedisSupport.class;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
