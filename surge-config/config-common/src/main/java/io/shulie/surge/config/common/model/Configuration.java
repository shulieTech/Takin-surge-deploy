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

package io.shulie.surge.config.common.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Data;

@Data
public class Configuration implements Serializable {
    private String name;    // 配置名称
    private String number;  // 配置编码
    private String desc;    // 配置描述
    private String type;    // 配置类型
    private String availableEnv;    // 配置可用环境
    private List<ConfigurationItem> items;           // 配置项

    public Map<String, String> convertItemsToMap() {
        if (items != null && !items.isEmpty()) {
            return items.stream().collect(
                Collectors.toMap(ConfigurationItem::getKey, ConfigurationItem::getValue, (oldValue, newValue) -> oldValue));
        }
        return null;
    }
}