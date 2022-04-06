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

package io.shulie.surge.data.sink.redis;

import java.util.concurrent.TimeUnit;

import io.shulie.surge.data.common.lifecycle.Stoppable;

public interface RedisSupport extends Stoppable {

    Boolean hasKey(String key);

    void setString(String key, String value);

    Boolean setNXString(String key, String value);

    Boolean setNXString(String key, String value, int expire, TimeUnit timeUnit);

    void setString(String key, String value, int expire, TimeUnit timeUnit);

    String getString(final String key);

    void delete(final String key);

    void del(String... key);

    void hmset(String key, String field, Object value);

    Object hmget(String key, String field);

    Long hIncrBy(String key, String field, long delta);

    void hDel(String key, String field);

    Long hSize(String key);
}
