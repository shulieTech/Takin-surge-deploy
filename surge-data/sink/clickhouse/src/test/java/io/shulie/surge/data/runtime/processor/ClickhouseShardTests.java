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

package io.shulie.surge.data.runtime.processor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.shulie.surge.data.sink.clickhouse.ClickHouseShardSupport;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @Author: xingchen
 * @ClassName: ClickhouseShardTests
 * @Package: io.shulie.surge.data.runtime.processor
 * @Date: 2021/6/1512:12
 * @Description:
 */
public class ClickhouseShardTests {
    public static void main(String[] args) throws InterruptedException {
        ClickHouseShardSupport support = new ClickHouseShardSupport("jdbc:clickhouse://pradar.host.clickhouse01:8123,pradar.host.clickhouse02:8123/default", "", "", 1, true);
        support.isCluster();
        while (true) {
            Thread.sleep(1000L);
            Map<String, List<Object[]>> map = Maps.newHashMap();
            List<Object[]> objects = Lists.newArrayList();
            objects.add(new Object[]{1});
            map.put(UUID.randomUUID().toString(), objects);
            support.batchUpdate("xx", map);
        }
    }
}
