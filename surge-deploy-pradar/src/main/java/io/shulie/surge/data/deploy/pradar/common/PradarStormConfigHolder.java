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

package io.shulie.surge.data.deploy.pradar.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * storm运行时 配置
 *
 * @author anjone
 */
public class PradarStormConfigHolder {

    private static final Map<String, String> propertiesMap = new ConcurrentHashMap<>();

    private PradarStormConfigHolder() { }

    public static <K, V> void init(Map<K, V> map) {
        synchronized (PradarStormConfigHolder.class) {
            if (propertiesMap.isEmpty()) {
                addProperties(map);
            }
        }
    }

    public static <K, V> void addProperties(Map<K, V> map) {
        if (map == null) return;
        map.forEach((key, value) -> {
            if(key != null && value != null )
            propertiesMap.put(key.toString(), value.toString());
        });
    }

    public static String getProperties(String key) {
        return propertiesMap.get(key);
    }
}
