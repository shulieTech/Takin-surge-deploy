package io.shulie.takin.kafka.receiver.service;

import io.shulie.takin.kafka.receiver.dto.clickhouse.ClickhouseQueryRequest;

import java.util.List;
import java.util.Map;

/**
 * @author Sunsy
 * @date 2022/2/28
 * @apiNode
 * @email sunshiyu@shulie.io
 */
public interface ClickhouseQueryService {

    List<Map<String, Object>> queryObjectByConditions(ClickhouseQueryRequest request);

    <T> List<T> queryObjectByConditions(ClickhouseQueryRequest request, Class<T> clazz);
}
