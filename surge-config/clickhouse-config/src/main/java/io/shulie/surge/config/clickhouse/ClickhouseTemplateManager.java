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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.pamirs.pradar.log.parser.constant.TenantConstants;
import io.shulie.surge.data.common.lifecycle.Lifecycle;
import io.shulie.surge.data.common.lifecycle.Stoppable;
import io.shulie.surge.data.common.pool.NamedThreadFactory;
import io.shulie.surge.data.sink.clickhouse.ClickHouseShardSupport;
import io.shulie.surge.data.sink.clickhouse.ClickHouseSupport;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import io.shulie.surge.deploy.pradar.common.CommonStat;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import ru.yandex.clickhouse.BalancedClickhouseDataSource;
import ru.yandex.clickhouse.ClickHouseConnection;
import ru.yandex.clickhouse.ClickhouseJdbcUrlParser;

import static io.shulie.surge.config.clickhouse.ClickhouseConfigurationSynchronizer.DEFAULT_BATCH_COUNT;

/**
 * 租户存储方案配置管理类，主要是保存了 租户@环境 和 clickhouse操作客户端 的映射关系
 * <pre>
 * 1、初始化租户@环境-clickhouse操作客户端映射关系
 *      根据配置文件中的clickhouse集群配置初始化clickhouse操作客户端：私有化部署和非多环境版本agent使用
 *      调用tro-web的存储方案接口初始化clickhouse操作客户端：多环境版本租户使用
 * 2、根据租户+环境获取clickhouse操作客户端
 * </pre>
 */
public class ClickhouseTemplateManager implements Lifecycle, Stoppable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClickhouseTemplateManager.class);

    /**
     * clickhouse集群配置
     */
    public static final String CONFIG_CLICKHOUSE_URL = "config.clickhouse.url";
    public static final String CONFIG_CLICKHOUSE_USER_NAME = "config.clickhouse.userName";
    public static final String CONFIG_CLICKHOUSE_PASSWORD = "config.clickhouse.password";
    public static final String CONFIG_CLICKHOUSE_BATCH_COUNT = "config.clickhouse.batchCount";
    public static final String CONFIG_CLICKHOUSE_ENABLE_ROUND = "config.clickhouse.enableRound";

    /**
     * 控制台配置
     */
    public static final String TRO_IP = ClickhouseConfigurationSynchronizer.TRO_IP;
    public static final String TRO_PORT = ClickhouseConfigurationSynchronizer.TRO_PORT;
    public static final String TRO_CONFIG_PATH = ClickhouseConfigurationSynchronizer.TRO_CONFIG_PATH;

    /**
     * 脚本文件配置
     */
    private static final String LOCAL_TABLE_NAME = "t_trace";
    private static final String DISTRIBUTE_TABLE_NAME = "t_trace_all";
    private static final String SCRIPT_FILE_SUFFIX = ".sql";
    private static final String LOCAL_TABLE_SCRIPT_FILE_NAME = LOCAL_TABLE_NAME + SCRIPT_FILE_SUFFIX;
    private static final String DISTRIBUTE_TABLE_SCRIPT_FILE_NAME = DISTRIBUTE_TABLE_NAME + SCRIPT_FILE_SUFFIX;
    private static final String TABLE_NAME_PLACEHOLDER = "{local_table_name}";
    private static final String DISTRIBUTE_TABLE_NAME_PLACEHOLDER = "{distribute_table_name}";
    private static final String CLUSTER_NAME_PLACEHOLDER = "{cluster_name}";
    private static final String DEFAULT_CLUSTER_NAME = " on cluster ck_cluster ";
    private static final String TTL_PLACEHOLDER = "{ttl}";
    private String fileParentPath;
    // 供amdb保存holder使用
    public final static ThreadLocal<ClickhouseTemplateHolder> HOLDER = new ThreadLocal<>();

    public static final String DELIMITER = "@";

    public static final String PROD_ENV = "prod";

    /**
     * 使用数据类型key，保存到 DataBootStrap中
     */
    public static final String DATA_SOURCE_KEY = "datasource-type";

    /**
     * 通过控制台获取的租户存储方案配置生成的数据源：key=租户标识@环境标识
     * <pre>
     * 查询数据时使用：QUERY_USED_TEMPLATE_HOLDER，使用 distribute table
     * 新增数据时使用：INSERT_USED_TEMPLATE_HOLDER，使用 local table
     * </pre>
     */
    private final Map<String, ClickhouseTemplateHolder> QUERY_USED_TEMPLATE_HOLDER = new ConcurrentHashMap<>();
    private final Map<String, ClickhouseTemplateHolder> INSERT_USED_TEMPLATE_HOLDER = new ConcurrentHashMap<>();

    /**
     * 默认数据源：低版本agent及私有化部署时使用
     * <pre>
     * 查询使用分布式表：support
     * 新增使用本地表：shardSupport
     * </pre>
     */
    private ClickHouseSupport support;
    private ClickHouseShardSupport shardSupport;

    /**
     * mysql存储trace日志
     */
    private final MysqlSupport mysqlSupport;
    private final ClickhouseTemplateHolder mysqlHolder;

    /**
     * 数据组件类型：{@link CommonStat#isUseCk}
     */
    private final String dataSourceType;

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    @Inject
    public ClickhouseTemplateManager(MysqlSupport mysqlSupport, @Named(DATA_SOURCE_KEY) String dataSourceType) {
        Assert.notNull(mysqlSupport, "mysqlSupport must not null");
        this.mysqlSupport = mysqlSupport;
        this.dataSourceType = dataSourceType;
        mysqlHolder = new ClickhouseTemplateHolder(mysqlSupport, DISTRIBUTE_TABLE_NAME);
    }

    /**
     * 构造之后进行调用进行数据初始化,该方法只用来初始化clickhouse数据源
     *
     * @param properties 配置参数
     */
    public void initClusterConfiguration(Properties properties) {
        if (isUseCk() && initialized.compareAndSet(false, true)) {
            // 默认数据源：私有化部署和非多环境版本agent使用,现阶段必须配置
            boolean configured = initDefaultClusterConfiguration(properties);
            if (!configured) {
                throw new IllegalStateException(
                    String.format("configuration file must configure [%s, %s]", CONFIG_CLICKHOUSE_URL, CONFIG_CLICKHOUSE_PASSWORD));
            }
            // 根据租户配置设置clickhouse操作客户端
            refreshTenantClusterConfiguration(properties);
        }
    }

    /**
     * 根据租户集群配置生成clickhouse客户端
     *
     * @param synchronizer 租户集群配置同步器
     */
    private void syncTenantClusterConfigurationAndRefreshHolder(ClickhouseConfigurationSynchronizer synchronizer) {
        List<ClickhouseClusterConfigEntity> data = synchronizer.syncTenantClusterConfiguration();
        for (ClickhouseClusterConfigEntity configEntity : data) {
            String clusterAddress = configEntity.getClusterAddress();
            String userName = configEntity.getUserName();
            String password = configEntity.getPassword();
            String url = reBuildUrlIfNecessary(clusterAddress);
            Integer batchCount = configEntity.getBatchCount();
            ClickHouseSupport support = new ClickHouseSupport(url, userName, password, batchCount, true);
            String uniqueKey = generateUniqueKey(configEntity.getUserAppKey(), configEntity.getEnvCode());
            ClickHouseShardSupport shardSupport = new ClickHouseShardSupport(url, userName, password, batchCount, true);
            ClickhouseTableNameGenerator router = new DefaultClickhouseTableNameGenerator(shardSupport.isCluster());
            INSERT_USED_TEMPLATE_HOLDER.put(uniqueKey,
                new ClickhouseTemplateHolder(shardSupport, router.generateLocalTableName(configEntity)));
            QUERY_USED_TEMPLATE_HOLDER.put(uniqueKey,
                new ClickhouseTemplateHolder(support, router.generateDistributeTableName(configEntity)));
        }
    }

    /**
     * 根据配置文件注入默认clickhouse客户端
     *
     * @param properties 配置文件
     * @return 默认数据源是否初始化成功，true-成功
     */
    public boolean initDefaultClusterConfiguration(Properties properties) {
        String url = properties.getProperty(CONFIG_CLICKHOUSE_URL);
        String userName = properties.getProperty(CONFIG_CLICKHOUSE_USER_NAME);
        String password = properties.getProperty(CONFIG_CLICKHOUSE_PASSWORD);
        if (StringUtils.isNotBlank(url)) {
            String batchCount = properties.getProperty(CONFIG_CLICKHOUSE_BATCH_COUNT, DEFAULT_BATCH_COUNT);
            if (!StringUtils.isNumeric(batchCount)) {
                batchCount = DEFAULT_BATCH_COUNT;
            }
            boolean enableRound = "true".equals(properties.getProperty(CONFIG_CLICKHOUSE_ENABLE_ROUND, "true"));
            int numericBatchCount = Integer.parseInt(batchCount);
            support = new ClickHouseSupport(url, userName, password, numericBatchCount, enableRound);
            shardSupport = new ClickHouseShardSupport(url, userName, password, numericBatchCount, enableRound);
            ClickhouseClusterConfigEntity defaultEntity = buildDefaultEntity(url, userName, password);
            ClickhouseTableNameGenerator router = new DefaultClickhouseTableNameGenerator(shardSupport.isCluster());
            String uniqueKey = generateUniqueKey(TenantConstants.DEFAULT_USER_APP_KEY, TenantConstants.DEFAULT_ENV_CODE);
            QUERY_USED_TEMPLATE_HOLDER.put(uniqueKey,
                new ClickhouseTemplateHolder(support, router.generateDistributeTableName(defaultEntity)));
            INSERT_USED_TEMPLATE_HOLDER.put(uniqueKey,
                new ClickhouseTemplateHolder(shardSupport, router.generateLocalTableName(defaultEntity)));
            return true;
        }
        return false;
    }

    /**
     * 根据配置文件配置构建默认clickhouse数据源entity
     *
     * @param clusterAddress 数据源url
     * @param userName       用户名
     * @param password       密码
     * @return clickhouse集群配置实体
     */
    private ClickhouseClusterConfigEntity buildDefaultEntity(String clusterAddress, String userName, String password) {
        ClickhouseClusterConfigEntity entity = new ClickhouseClusterConfigEntity();
        entity.setEnvCode(TenantConstants.DEFAULT_ENV_CODE);
        entity.setUserAppKey(TenantConstants.DEFAULT_ENV_CODE);
        entity.setClusterAddress(clusterAddress);
        entity.setUserName(userName);
        entity.setPassword(password);
        entity.setClusterName("配置文件-默认集群");
        entity.setClusterDesc("配置文件-默认集群");
        entity.setTenantName("");
        return entity;
    }

    public ClickhouseTemplateHolder getTemplateHolder(String userAppKey, String envCode, boolean isAdd) {
        return getTemplateHolder(generateUniqueKey(userAppKey, envCode), isAdd);
    }

    /**
     * 获取clickhouse操作客户端及表名
     *
     * @param uniqueKey 租户标识@环境标识
     * @param isAdd     是否新增(新增数据-true，查询数据-false)
     * @return {@link ClickhouseTemplateHolder}
     */
    public ClickhouseTemplateHolder getTemplateHolder(String uniqueKey, boolean isAdd) {
        if (isUseCk()) {
            ClickhouseTemplateHolder templateHolder;
            if (isAdd) {
                templateHolder = INSERT_USED_TEMPLATE_HOLDER.get(uniqueKey);
            } else {
                templateHolder = QUERY_USED_TEMPLATE_HOLDER.get(uniqueKey);
            }
            // 租户未配置存储方案时使用默认数据源
            if (templateHolder == null) {
                LOGGER.info("not found clickhouse-cluster configuration by uniqueKey, use default configuration. uniqueKey=[{}]", uniqueKey);
                return getTemplateHolder(generateUniqueKey("", ""), isAdd);
            }
            return templateHolder;
        }
        return mysqlHolder;
    }

    /**
     * 生成数据源 map key
     *
     * @param userAppKey 租户标识
     * @param envCode    环境标识
     * @return unique key
     */
    private String generateUniqueKey(String userAppKey, String envCode) {
        if (StringUtils.isBlank(userAppKey)) {
            userAppKey = TenantConstants.DEFAULT_USER_APP_KEY;
        }
        if (StringUtils.isBlank(envCode)) {
            envCode = TenantConstants.DEFAULT_ENV_CODE;
        }
        return userAppKey + DELIMITER + envCode;
    }

    /**
     * 此建表方法提供给amdb使用，将操作clickhouse的职责维护在此处
     *
     * @param entity clickhouse集群配置实体
     * @throws Exception 文件读取/建表异常
     */
    public void createTableByScriptFile(ClickhouseClusterConfigEntity entity) throws Exception {
        Assert.notNull(entity, "clickhouse-cluster configuration entity must not null");
        if (entity.isProd()) {
            ClickHouseShardSupport shardSupport = new ClickHouseShardSupport();
            List<String> urls = shardSupport.splitUrl(reBuildUrlIfNecessary(entity.getClusterAddress()));
            // 建立本地表
            createLocalTable(entity, urls);
            // 建立分布式表
            createDistributeTable(entity, urls);
        }
    }

    /**
     * 创建本地表，此处脚本是on cluster模式，只需要选取一个节点执行就行了,失败不重试
     *
     * @param entity clickhouse集群配置实体
     * @param urls   集群url
     * @throws Exception 文件读取/建表异常
     */
    private void createLocalTable(ClickhouseClusterConfigEntity entity, List<String> urls) throws Exception {
        String userName = entity.getUserName();
        String password = entity.getPassword();
        String createTableScript = findCreateTableSql(entity, true, isCluster(urls));
        BalancedClickhouseDataSource dataSource = buildDataSource(urls.get(0));
        try (ClickHouseConnection connection = dataSource.getConnection(userName, password)) {
            connection.createStatement().execute(createTableScript);
        }
    }

    /**
     * 创建分布式表,失败不重试
     *
     * @param entity clickhouse集群配置实体
     * @param urls   集群url
     * @throws Exception 文件读取/建表异常
     */
    private void createDistributeTable(ClickhouseClusterConfigEntity entity, List<String> urls) throws Exception {
        if (isCluster(urls)) {
            String userName = entity.getUserName();
            String password = entity.getPassword();
            String createTableScript = findCreateTableSql(entity, false, true);
            BalancedClickhouseDataSource dataSource = buildDataSource(urls.get(0));
            try (ClickHouseConnection connection = dataSource.getConnection(userName, password)) {
                connection.createStatement().execute(createTableScript);
            }
        }
    }

    /**
     * 重建url，自动添加前缀
     *
     * @param url 数据源url
     * @return 重建后url
     */
    private String reBuildUrlIfNecessary(String url) {
        if (StringUtils.startsWith(url, ClickhouseJdbcUrlParser.JDBC_CLICKHOUSE_PREFIX)) {
            return url;
        }
        return ClickhouseJdbcUrlParser.JDBC_CLICKHOUSE_PREFIX + "//" + url + "/default";
    }

    /**
     * 通过url构造dataSource
     *
     * @param url 数据源url,会自动判断是否需要添加前缀
     * @return 数据源 {@link BalancedClickhouseDataSource}
     */
    private BalancedClickhouseDataSource buildDataSource(String url) {
        return new BalancedClickhouseDataSource(reBuildUrlIfNecessary(url));
    }

    /**
     * 返回表结构，并替换其中的 {table_name}, {distribute_table_name}, {cluster_name}, {ttl}
     *
     * @param entity     clickhouse集群配置实体
     * @param localTable 是否本地表
     * @param isCluster  是否集群
     * @return 建表语句
     * @throws IOException 脚本文件内容读取发生IO异常
     */
    private String findCreateTableSql(ClickhouseClusterConfigEntity entity, boolean localTable, boolean isCluster) throws IOException {
        File scriptFile = new File(this.fileParentPath, localTable ? LOCAL_TABLE_SCRIPT_FILE_NAME : DISTRIBUTE_TABLE_SCRIPT_FILE_NAME);
        if (scriptFile.exists()) {
            // 获取脚本文件内容
            String tableScript = FileUtils.readFileToString(scriptFile, StandardCharsets.UTF_8.name());
            if (StringUtils.isBlank(tableScript)) {
                throw new IllegalStateException("clickhouse-cluster script file [" + scriptFile.getPath() + "] content is empty");
            }
            ClickhouseTableNameGenerator router = new DefaultClickhouseTableNameGenerator(isCluster);
            return tableScript.replace(TABLE_NAME_PLACEHOLDER, router.generateLocalTableName(entity))
                .replace(DISTRIBUTE_TABLE_NAME_PLACEHOLDER, router.generateDistributeTableName(entity))
                .replace(TTL_PLACEHOLDER, entity.getTtl())
                .replace(CLUSTER_NAME_PLACEHOLDER, isCluster ? DEFAULT_CLUSTER_NAME : "");
        }
        throw new IllegalStateException("clickhouse-cluster script file[" + scriptFile.getPath() + "] not exists");
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() throws Exception {
        if (support != null) {
            support.stop();
        }
        if (shardSupport != null) {
            shardSupport.stop();
        }
        if (mysqlSupport != null) {
            mysqlSupport.stop();
        }
        if (!CollectionUtils.isEmpty(QUERY_USED_TEMPLATE_HOLDER)) {
            QUERY_USED_TEMPLATE_HOLDER.values().forEach(holder -> {
                try {
                    holder.getTemplate().stop();
                } catch (Exception ignore) {
                }
            });
        }
        if (!CollectionUtils.isEmpty(INSERT_USED_TEMPLATE_HOLDER)) {
            INSERT_USED_TEMPLATE_HOLDER.values().forEach(holder -> {
                try {
                    holder.getTemplate().stop();
                } catch (Exception ignore) {
                }
            });
        }
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    /**
     * 自动任务，调用控制台接口，刷新数据源缓存
     *
     * @param properties 配置文件，主要是获取控制台配置
     */
    private void refreshTenantClusterConfiguration(Properties properties) {
        if (!StringUtils.isAnyBlank(properties.getProperty(TRO_IP), properties.getProperty(TRO_PORT), TRO_CONFIG_PATH)) {
            ClickhouseConfigurationSynchronizer synchronizer = new ClickhouseConfigurationSynchronizer(mysqlSupport, properties);
            Executors.newScheduledThreadPool(1, new NamedThreadFactory("refresh-clickhouse-cluster-configuration"))
                .scheduleAtFixedRate(() -> {
                    try {
                        syncTenantClusterConfigurationAndRefreshHolder(synchronizer);
                    } catch (Exception e) {
                        LOGGER.error("init clickhouse-cluster configuration fail", e);
                    }
                }, 0, 2, TimeUnit.MINUTES);
        }
    }

    public void setFileParentPath(String fileParentPath) {
        Assert.hasText(fileParentPath, "script file path must not empty");
        this.fileParentPath = fileParentPath;
    }

    public boolean isUseCk() {
        return CommonStat.isUseCk(this.dataSourceType);
    }

    public Map<String, ClickhouseTemplateHolder> getQueryTemplateMap() {
        return Collections.unmodifiableMap(QUERY_USED_TEMPLATE_HOLDER);
    }

    private boolean isCluster(List<String> urls) {
        return new ClickHouseShardSupport().isCluster(urls);
    }
}
