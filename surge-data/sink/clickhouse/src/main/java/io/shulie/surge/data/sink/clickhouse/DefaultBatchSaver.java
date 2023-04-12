package io.shulie.surge.data.sink.clickhouse;

import com.google.common.collect.Lists;
import io.shulie.surge.data.common.batch.RotationBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2023/4/12 16:29
 */
public class DefaultBatchSaver implements RotationBatch.BatchSaver<Object[]> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, JdbcTemplate> shardJdbcTemplateMap;
    private final String sql;
    private AtomicInteger count = new AtomicInteger(0);
    private AtomicLong cost = new AtomicLong(0L);

    private ScheduledExecutorService service;

    public DefaultBatchSaver(String sql, Map<String, JdbcTemplate> shardJdbcTemplateMap) {
        this.shardJdbcTemplateMap = shardJdbcTemplateMap;
        this.sql = sql;
        this.service = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public boolean saveBatch(LinkedBlockingQueue<Object[]> batchSql) {
        return true;
    }

    @Override
    public boolean shardSaveBatch(String shardKey, LinkedBlockingQueue<Object[]> batchSql) {
        if (batchSql == null || batchSql.isEmpty()) {
            return true;
        }
        try {
            shardJdbcTemplateMap.get(shardKey).batchUpdate(sql, Lists.newArrayList(batchSql));
        } catch (Exception e) {
            logger.error("write clickhouse failed. shardKey={}, batchSql={}", shardKey, batchSql, e);
            return false;
        }
        return true;
    }
}
