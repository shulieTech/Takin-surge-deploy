package io.shulie.surge.data.sink.clickhouse;

import com.google.common.collect.Lists;
import io.shulie.surge.data.common.batch.RotationBatch;
import io.shulie.surge.data.common.utils.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static io.shulie.surge.data.common.utils.CommonUtils.divide;

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

        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                StringBuilder appender = new StringBuilder(256);
                appender.append("Clickhouse Process Time: ");
                appender.append("\n  ")
                        .append(FormatUtils.humanReadableTimeSpan(cost.get()))
                        .append("ms, about ")
                        .append(FormatUtils.roundx4(divide(cost.get(), count.get()))).append(" ms/line")
                        .append(" line:").append(count.get())
                        .append(" tps:").append(FormatUtils.roundx0(divide(count.get(), count.get() < 1000 ? 1 : count.get() / 1000)));
                count.set(0);
                cost.set(0);
                logger.warn(appender.toString());
            }
        }, 1, 5, TimeUnit.SECONDS);
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
            long start = System.currentTimeMillis();
            shardJdbcTemplateMap.get(shardKey).batchUpdate(sql, Lists.newArrayList(batchSql));
            long end = System.currentTimeMillis();
            cost.addAndGet(end - start);
            count.addAndGet(batchSql.size());
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
