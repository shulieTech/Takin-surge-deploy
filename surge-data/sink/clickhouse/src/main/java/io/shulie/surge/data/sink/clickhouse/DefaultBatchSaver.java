package io.shulie.surge.data.sink.clickhouse;

import com.google.common.collect.Lists;
import io.shulie.surge.data.common.batch.RotationBatch;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2023/4/12 16:29
 */
public class DefaultBatchSaver implements RotationBatch.BatchSaver<Object[]> {
    private final Map<String, JdbcTemplate> shardJdbcTemplateMap;
    private final String sql;

    public DefaultBatchSaver(String sql, Map<String, JdbcTemplate> shardJdbcTemplateMap) {
        this.shardJdbcTemplateMap = shardJdbcTemplateMap;
        this.sql = sql;
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
            e.printStackTrace();
            try {
                TimeUnit.MILLISECONDS.sleep(10L);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            return false;
        }
        return true;
    }
}
