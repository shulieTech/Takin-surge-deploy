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

package io.shulie.surge.data.common.dataSource;

import io.shulie.surge.data.common.lifecycle.Lifecycle;
import io.shulie.surge.data.common.lifecycle.Stoppable;

import java.util.List;
import java.util.Map;

/**
 * 抽象dataSource写入类,后续替换if/else操作。但会带来使用上的麻烦，暂时不剥离
 *
 * @Author: xingchen
 * @ClassName: DatasourceSupport
 * @Package: io.shulie.surge.data.common.dataSource
 * @Date: 2021/7/3010:14
 * @Description:
 */
public interface DataSourceSupport extends Stoppable, Lifecycle {
    void batchUpdate(final String sql, Map<String, List<Object[]>> shardBatchArgs);

    void batchUpdate(final String sql, final List<Object[]> batchArgs);

    /**
     * 查询->类型转换
     *
     * @param sql
     * @return
     */
    <T> List<T> queryForList(String sql, Class<T> clazz);

    /**
     * 查询list
     *
     * @param sql
     * @return
     */
    List<Map<String, Object>> queryForList(String sql);
}
