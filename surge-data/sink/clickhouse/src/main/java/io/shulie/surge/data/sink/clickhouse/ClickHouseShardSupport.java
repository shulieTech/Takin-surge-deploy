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

import com.google.common.base.Joiner;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
    private static int delayTime = 5;
    private Map<String, String> urlMap = Maps.newHashMap();
    private Map<String, JdbcTemplate> shardJdbcTemplateMap = Maps.newHashMap();
    private ConcurrentMap<String, RotationBatch<Object[]>> rotationPrepareSqlBatch = new ConcurrentHashMap<>();

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
            logger.info("clickhouse url={}.", this.urls);
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

    public void addBatch(final String sql, Map<String, List<Object[]>> args) {
        Map<String, List<Object[]>> map = new HashMap<>();
        for (Map.Entry<String, List<Object[]>> entry : args.entrySet()) {
            String shardKey = getShardKey(entry.getKey());
            List<Object[]> list = map.get(shardKey);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.addAll(entry.getValue());
        }

        for (Map.Entry<String, List<Object[]>> entry : map.entrySet()) {
            String key = entry.getKey() + ':' + sql;

            RotationBatch<Object[]> rotationBatch = rotationPrepareSqlBatch.get(key);
            if (rotationBatch == null) {
                rotationBatch = new RotationBatch<>(entry.getKey(), new CountRotationPolicy(batchCount), new TimedRotationPolicy(delayTime, TimeUnit.SECONDS));
                rotationBatch.batchSaver(new DefaultBatchSaver(sql, shardJdbcTemplateMap));
                RotationBatch old = rotationPrepareSqlBatch.putIfAbsent(key, rotationBatch);
                if (old != null) {
                    RotationBatch current = rotationBatch;
                    rotationBatch = old;
                    current.stop();
                } else {
                    rotationBatch.start(false);
                }
            }

            rotationBatch.addBatch(entry.getValue());
        }
    }

    public void addBatch(final String sql, String key, Object[] args) {
        String shardKey = getShardKey(key);
        String identityId = shardKey + ":" + sql;
        RotationBatch<Object[]> rotationBatch = rotationPrepareSqlBatch.get(identityId);
        if (rotationBatch == null) {
            rotationBatch = new RotationBatch<>(shardKey, new CountRotationPolicy(batchCount), new TimedRotationPolicy(delayTime, TimeUnit.SECONDS));
            rotationBatch.batchSaver(new DefaultBatchSaver(sql, shardJdbcTemplateMap));
            RotationBatch old = rotationPrepareSqlBatch.putIfAbsent(identityId, rotationBatch);
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
     * @param args
     */
    public void addBatch(final String sql, String key, List<Object[]> args) {
        String shardKey = getShardKey(key);
        String identityId = shardKey + ":" + sql;
        RotationBatch<Object[]> rotationBatch = rotationPrepareSqlBatch.get(identityId);
        if (rotationBatch == null) {
            rotationBatch = new RotationBatch<>(shardKey, new CountRotationPolicy(batchCount), new TimedRotationPolicy(delayTime, TimeUnit.SECONDS));
            rotationBatch.batchSaver(new DefaultBatchSaver(sql, shardJdbcTemplateMap));
            RotationBatch old = rotationPrepareSqlBatch.putIfAbsent(identityId, rotationBatch);
            if (old != null) {
                RotationBatch current = rotationBatch;
                rotationBatch = old;
                current.stop();
            } else {
                rotationBatch.start();
            }
        }

        rotationBatch.addBatch(args);
    }

    private String getShardKey(final String key) {
        int idx = (key.hashCode() & Integer.MAX_VALUE) % this.urls.size();
        return urls.get(idx);
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

    public void insert(Map<String, Object> map, String key, String tableName) {
        if (org.springframework.util.CollectionUtils.isEmpty(map)) {
            logger.warn("入参为空，不能插入数据");
            return;
        }
        if (StringUtils.isBlank(tableName)) {
            logger.warn("表名为空，不能插入数据");
            return;
        }
        String cols = Joiner.on(',').join(map.keySet());
        List<String> params = new ArrayList<>();
        for (String field : map.keySet()) {
            params.add("?");
        }
        String param = Joiner.on(',').join(params);
        String sql = "insert into " + tableName + " (" + cols + ") values(" + param + ") ";
        List<Object[]> batchs = Lists.newArrayList();
        batchs.add(map.values().toArray());
        this.addBatch(sql, key, batchs);
    }
}
