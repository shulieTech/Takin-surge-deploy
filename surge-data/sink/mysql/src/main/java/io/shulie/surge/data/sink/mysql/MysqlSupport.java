
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

package io.shulie.surge.data.sink.mysql;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.name.Named;
import io.shulie.surge.data.common.batch.CountRotationPolicy;
import io.shulie.surge.data.common.batch.RotationBatch;
import io.shulie.surge.data.common.batch.TimedRotationPolicy;
import io.shulie.surge.data.common.lifecycle.Lifecycle;
import io.shulie.surge.data.common.lifecycle.Stoppable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * 基于 clickhouse 的异步实现
 *
 * @author pamirs
 */

public class MysqlSupport implements Lifecycle, Stoppable {

    private static final Logger logger = LoggerFactory.getLogger(MysqlSupport.class);

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private RotationBatch<String> rotationSqlBatch;
    private Map<String, RotationBatch<Object[]>> rotationPrepareSqlBatch = Maps.newHashMap();
    private DataSourceTransactionManager transactionManager;

    public MysqlSupport(@Named("config.mysql.url") String url,
                        @Named("config.mysql.userName") String username,
                        @Named("config.mysql.password") String password,
                        @Named("config.mysql.initialSize") Integer initialSize,
                        @Named("config.mysql.minIdle") Integer minIdle,
                        @Named("config.mysql.maxActive") Integer maxActive) {
        try {
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.setInitialSize(minIdle);
            dataSource.setMinIdle(initialSize);
            dataSource.setMaxActive(maxActive);
            dataSource.setTestOnBorrow(false);
            dataSource.setTestOnReturn(false);
            dataSource.setTestWhileIdle(true);
            dataSource.setTimeBetweenEvictionRunsMillis(60000);
            dataSource.setMinEvictableIdleTimeMillis(300000);
            dataSource.setMaxWait(60000);
            dataSource.setValidationQuery("select '*'");
            this.dataSource = dataSource;
            jdbcTemplate = new JdbcTemplate(dataSource);
            transactionManager = new DataSourceTransactionManager(dataSource);
        } catch (Exception e) {
            logger.error("Init datasource failed.", e);
            throw e;
        }
    }

    @Override
    public void start() throws Exception {
        if (jdbcTemplate == null) {
            jdbcTemplate = new JdbcTemplate(dataSource);
        }
        rotationSqlBatch = new RotationBatch(new CountRotationPolicy(200), new TimedRotationPolicy(2, TimeUnit.SECONDS));
        rotationSqlBatch.batchSaver(new RotationBatch.BatchSaver<String>() {
            @Override
            public boolean saveBatch(LinkedBlockingQueue<String> batchSql) {
                jdbcTemplate.batchUpdate(batchSql.toArray(new String[batchSql.size()]));
                return true;
            }

            @Override
            public boolean shardSaveBatch(String key, LinkedBlockingQueue<String> ObjectBatch) {
                return false;
            }
        });
        rotationPrepareSqlBatch = Maps.newHashMap();
    }

    @Override
    public void stop() throws Exception {
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    /**
     * 批量更新
     *
     * @param sql
     */

    public void addBatch(final String... sql) throws Exception {
        if (rotationSqlBatch == null) {
            start();
        }
        for (String s : sql) {
            rotationSqlBatch.addBatch(s);
        }
    }

    public void addBatch(final String sql, Object[] args) {
        RotationBatch<Object[]> rotationBatch = rotationPrepareSqlBatch.get(sql);
        if (rotationBatch == null) {
            rotationBatch = new RotationBatch<>(new CountRotationPolicy(200), new TimedRotationPolicy(2, TimeUnit.SECONDS));
            rotationBatch.batchSaver(new RotationBatch.BatchSaver<Object[]>() {
                @Override
                public boolean saveBatch(LinkedBlockingQueue<Object[]> batchSql) {
                    jdbcTemplate.batchUpdate(sql, Lists.newArrayList(batchSql));
                    return true;
                }

                @Override
                public boolean shardSaveBatch(String key, LinkedBlockingQueue<Object[]> ObjectBatch) {
                    return false;
                }
            });
            RotationBatch old = rotationPrepareSqlBatch.putIfAbsent(sql, rotationBatch);
            if (old != null) {
                RotationBatch current = rotationBatch;
                rotationBatch = old;
                current.stop();
            } else {
                rotationBatch.start(false);
            }
        }
        rotationBatch.addBatch(args);
    }

    /**
     * 批量更新
     *
     * @param sql
     * @param batchArgs
     */

    public void addBatch(final String sql, final List<Object[]> batchArgs) {
        RotationBatch<Object[]> rotationBatch = rotationPrepareSqlBatch.get(sql);
        if (rotationBatch == null) {
            rotationBatch = new RotationBatch<>(new CountRotationPolicy(200), new TimedRotationPolicy(2, TimeUnit.SECONDS));
            rotationBatch.batchSaver(new RotationBatch.BatchSaver<Object[]>() {
                @Override
                public boolean saveBatch(LinkedBlockingQueue<Object[]> batchSql) {
                    jdbcTemplate.batchUpdate(sql, Lists.newArrayList(batchSql));
                    return true;
                }

                @Override
                public boolean shardSaveBatch(String key, LinkedBlockingQueue<Object[]> ObjectBatch) {
                    return false;
                }
            });
            RotationBatch old = rotationPrepareSqlBatch.putIfAbsent(sql, rotationBatch);
            if (old != null) {
                RotationBatch current = rotationBatch;
                rotationBatch = old;
                current.stop();
            } else {
                rotationBatch.start(false);
            }
        }
        rotationBatch.addBatch(batchArgs);
    }

    public void updateBatch(final String sql, final List<Object[]> batchArgs) {
        if (jdbcTemplate == null) {
            jdbcTemplate = new JdbcTemplate(dataSource);
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    public void update(final String sql, final Object[] args) {
        if (jdbcTemplate == null) {
            jdbcTemplate = new JdbcTemplate(dataSource);
        }
        jdbcTemplate.update(sql, args);
    }

    /**
     * 执行指定sql
     */
    public void execute(String sql) {
        if (jdbcTemplate == null) {
            jdbcTemplate = new JdbcTemplate(dataSource);
        }
        jdbcTemplate.execute(sql);
    }

    /**
     * 查询map
     *
     * @param sql
     * @return
     */

    public Map<String, Object> queryForMap(String sql) {
        if (jdbcTemplate == null) {
            jdbcTemplate = new JdbcTemplate(dataSource);
        }
        return jdbcTemplate.queryForMap(sql);
    }

    /**
     * 查询map
     *
     * @param sql
     * @return
     */

    public <T> T queryForObject(String sql, Class<T> clazz) {
        if (jdbcTemplate == null) {
            jdbcTemplate = new JdbcTemplate(dataSource);
        }
        return jdbcTemplate.queryForObject(sql, clazz);
    }

    /**
     * 查询map
     * 使用 {@link MysqlSupport#query} 替代
     *
     * @param sql
     * @return
     */
    public <T> List<T> queryForList(String sql, Class<T> clazz) {
        if (jdbcTemplate == null) {
            jdbcTemplate = new JdbcTemplate(dataSource);
        }
        return jdbcTemplate.queryForList(sql, clazz);
    }


    /**
     * 查询list
     * 使用 {@link MysqlSupport#query} 替代
     *
     * @param sql
     * @return
     */
    public List<Map<String, Object>> queryForList(String sql) {
        if (jdbcTemplate == null) {
            jdbcTemplate = new JdbcTemplate(dataSource);
        }
        return jdbcTemplate.queryForList(sql);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        if (jdbcTemplate == null) {
            jdbcTemplate = new JdbcTemplate(dataSource);
        }
        return jdbcTemplate.query(sql, rowMapper);
    }
}
