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

package io.shulie.surge.data.runtime.common;

import java.util.List;
import java.util.Map;

import io.shulie.surge.data.common.lifecycle.Lifecycle;
import io.shulie.surge.data.common.lifecycle.Stoppable;
import org.springframework.jdbc.core.RowMapper;

public interface DataOperations extends Lifecycle, Stoppable {

    default void batchUpdate(final String sql, final List<Object[]> batchArgs) {}

    default void batchUpdate(String sql, Map<String, List<Object[]>> batchArgs) {}

    default void update(final String sql, final Object[] args) {}

    default Map<String, Object> queryForMap(String sql) {return null;}

    default <T> T queryForObject(String sql, Class<T> clazz) {return null;}

    default <T> List<T> queryForList(String sql, Class<T> clazz) {return null;}

    default  <T> List<T> query(String sql, RowMapper<T> rowMapper) {return null;}

    default List<Map<String, Object>> queryForList(String sql) {return null;}

    default void execute(String sql) {}
}
