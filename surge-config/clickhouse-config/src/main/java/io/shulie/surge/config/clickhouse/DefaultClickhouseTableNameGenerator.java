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

import org.apache.commons.lang3.StringUtils;

/**
 * clickhouse默认表名生成：使用时 generateLocalTableName 与 generateDistributeTableName 传同一个对象，不要改动
 * <pre>
 * 非集群环境下：local table = distribute table (此时其实没有分布式表的概念，此处为了跟集群环境描述保持一致)
 * 集群环境下：local table + "_all" = distribute table
 *
 *
 * 生产环境+集群：
 *      local table =   t_trace_租户名称
 *      distribute table =  t_trace_租户名称_all
 *
 * 生产环境+非集群：一般不会出现
 *      local table =       t_trace_租户名称_all
 *      distribute table =  t_trace_租户名称_all
 *
 * 非生产环境+集群：
 *      local table =   t_trace
 *      distribute table =  t_trace_all
 *
 * 非生产环境+非集群：
 *      local table = t_trace_all
 *      distribute table =  t_trace_all
 *
 * </pre>
 */
public class DefaultClickhouseTableNameGenerator implements ClickhouseTableNameGenerator {

    private final boolean isCluster;

    public DefaultClickhouseTableNameGenerator(boolean isCluster) {
        this.isCluster = isCluster;
    }

    @Override
    public String generateDistributeTableName(ClickhouseClusterConfigEntity configEntity) {
        String localTable = generateLocalTableName(configEntity);
        return !isCluster ? localTable : StringUtils.substringBeforeLast(localTable, "\"") + "_all\"";
    }

    @Override
    public String generateLocalTableName(ClickhouseClusterConfigEntity configEntity) {
        String tablePrefix = "\"t_trace";
        if (configEntity.isProd()) {
            tablePrefix = tablePrefix + "_" + configEntity.getTenantName();
        }
        if (!isCluster) {
            tablePrefix = tablePrefix + "_all";
        }
        return tablePrefix + "\"";
    }
}
