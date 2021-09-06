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

package io.shulie.surge.data.sink.clickhouse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.shulie.surge.data.common.batch.CountRotationPolicy;
import io.shulie.surge.data.common.batch.RotationBatch;
import io.shulie.surge.data.common.batch.TimedRotationPolicy;
import io.shulie.surge.data.common.lifecycle.Lifecycle;
import io.shulie.surge.data.common.lifecycle.Stoppable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.clickhouse.BalancedClickhouseDataSource;
import ru.yandex.clickhouse.settings.ClickHouseProperties;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 基于 clickhouse 的异步实现-支持分片写入
 *
 * @author zhouyuan
 */
public class ClickHouseShardSupport implements Lifecycle, Stoppable {
    private static final Logger logger = LoggerFactory.getLogger(ClickHouseShardSupport.class);
    private static final Pattern URL_TEMPLATE = Pattern.compile("jdbc:clickhouse://([a-zA-Z0-9_:,.-]+)(/[a-zA-Z0-9_]+([?][a-zA-Z0-9_]+[=][a-zA-Z0-9_]+([&][a-zA-Z0-9_]+[=][a-zA-Z0-9_]+)*)?)?");
    private List<String> urls;
    private int batchCount;
    private static int deleyTime = 5;
    private Map<String, String> urlMap = Maps.newHashMap();
    private Map<String, JdbcTemplate> shardJdbcTemplateMap = Maps.newHashMap();
    private Map<String, RotationBatch<Object[]>> rotationPrepareSqlBatch = Maps.newHashMap();

    public ClickHouseShardSupport() {
    }

    @Inject
    public ClickHouseShardSupport(@Named("config.clickhouse.url") String url,
                                  @Named("config.clickhouse.userName") String username,
                                  @Named("config.clickhouse.password") String password,
                                  @Named("config.clickhouse.batchCount") int batchCount,
                                  @Named("config.clickhouse.enableRound") boolean enableRound) {
        try {
            this.urls = splitUrl(url);
            ClickHouseProperties clickHouseProperties = new ClickHouseProperties();
            if (StringUtils.isNotBlank(username)) {
                clickHouseProperties.setUser(username);
            }
            if (StringUtils.isNotBlank(password)) {
                clickHouseProperties.setPassword(password);
            }
            DataSource clickHouseDataSource = null;
            for (int i = 0; i < urls.size(); i++) {
                String urlParam = urls.get(i);
                if (enableRound) {
                    clickHouseDataSource = new RoundClickhouseDataSource(urlParam, clickHouseProperties, true);
                } else {
                    clickHouseDataSource = new BalancedClickhouseDataSource(urlParam, clickHouseProperties);
                }
                JdbcTemplate jdbcTemplate = new JdbcTemplate(clickHouseDataSource);

                shardJdbcTemplateMap.put(urlParam, jdbcTemplate);
            }
            this.batchCount = batchCount;
        } catch (Exception e) {
            logger.error("Init datasource failed.", e);
            throw e;
        }
    }

    @Override
    public void start() {
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
     * @param shardBatchArgs
     */
    public synchronized void batchUpdate(final String sql, Map<String, List<Object[]>> shardBatchArgs) {
        Map<String, List<Object[]>> shardBatchArgsMap = shardBatchArgs(shardBatchArgs);
        for (Map.Entry<String, List<Object[]>> entry : shardBatchArgsMap.entrySet()) {
            RotationBatch<Object[]> rotationBatch = null;
            if (!rotationPrepareSqlBatch.containsKey(entry.getKey() + ":" + sql)) {
                rotationBatch = new RotationBatch(entry.getKey(), new CountRotationPolicy(batchCount), new TimedRotationPolicy(deleyTime, TimeUnit.SECONDS));
                rotationBatch.batchSaver(new RotationBatch.BatchSaver<Object[]>() {
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
                            shardJdbcTemplate(shardKey).batchUpdate(sql, Lists.newArrayList(batchSql));
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
                });
            } else {
                rotationBatch = rotationPrepareSqlBatch.get(entry.getKey() + ":" + sql);
            }
            for (Object[] args : entry.getValue()) {
                rotationBatch.addBatch(args);
            }
            rotationPrepareSqlBatch.put(entry.getKey() + ":" + sql, rotationBatch);
        }
    }

    private JdbcTemplate shardJdbcTemplate(String key) {
        return shardJdbcTemplateMap.get(key);
    }

    /**
     * 按url-> args拆分
     *
     * @param batchArgsMap
     * @return
     */
    private Map<String, List<Object[]>> shardBatchArgs(final Map<String, List<Object[]>> batchArgsMap) {
        Map<String, List<Object[]>> shardBatchArgsMap = Maps.newHashMap();
        for (Map.Entry<String, List<Object[]>> entry : batchArgsMap.entrySet()) {
            int idx = (entry.getKey().hashCode() & Integer.MAX_VALUE) % this.urls.size();
            if (shardBatchArgsMap.containsKey(urls.get(idx))) {
                shardBatchArgsMap.get(urls.get(idx)).addAll(entry.getValue());
            } else {
                shardBatchArgsMap.put(urls.get(idx), entry.getValue());
            }
        }
        return shardBatchArgsMap;
    }

    public List<String> splitUrl(String url) {
        Matcher m = URL_TEMPLATE.matcher(url);
        if (!m.matches()) {
            throw new IllegalArgumentException("Incorrect url");
        } else {
            String database = m.group(2);
            if (database == null) {
                database = "";
            }
            String[] hosts = m.group(1).split(",");
            List<String> result = new ArrayList(hosts.length);
            String[] var5 = hosts;
            int var6 = hosts.length;

            for (int var7 = 0; var7 < var6; ++var7) {
                String host = var5[var7];
                try {
                    String hostAddress = InetAddress.getByName(host.substring(0, host.lastIndexOf(":"))).getHostAddress();
                    if (!urlMap.containsKey(hostAddress)) {
                        result.add("jdbc:clickhouse://" + host + database);
                        urlMap.put(hostAddress, hostAddress);
                    }
                } catch (Throwable e) {
                }
            }
            return result;
        }
    }

    public boolean isCluster() {
        Set<String> urlSet = formatUrl(urls);
        Set<String> var2 = urlSet.stream().map(var -> {
            try {
                return InetAddress.getByName(var).getHostAddress();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }).collect(Collectors.toSet());
        return var2.size() > 1;
    }

    public Set<String> formatUrl(List<String> urls) {
        if (CollectionUtils.isEmpty(urls)) {
            throw new RuntimeException("clickhouse url is null");
        }
        return urls.stream().map(var -> var.substring(var.indexOf("//") + 2, var.lastIndexOf(":"))).collect(Collectors.toSet());
    }
}
