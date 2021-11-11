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

package io.shulie.surge.config.clickhouse;

import io.shulie.surge.data.runtime.common.DataOperations;

/**
 * clickhouse客户端holder
 */
public class ClickhouseTemplateHolder {

    /**
     * clickhouse操作客户端
     */
    private final DataOperations template;
    /**
     * clickhouse表名
     */
    private final String tableName;

    public ClickhouseTemplateHolder(DataOperations template, String tableName) {
        this.template = template;
        this.tableName = tableName;
    }

    public DataOperations getTemplate() {
        return template;
    }

    public String getTableName() {
        return tableName;
    }
}
