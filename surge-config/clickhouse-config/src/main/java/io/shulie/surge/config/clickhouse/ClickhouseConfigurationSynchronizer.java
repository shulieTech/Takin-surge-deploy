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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import io.shulie.surge.config.common.ConfigurationTypeEnum;
import io.shulie.surge.config.common.model.Configuration;
import io.shulie.surge.config.common.model.ConfigurationItem;
import io.shulie.surge.config.common.model.TenantConfigItemEntity;
import io.shulie.surge.config.common.model.TenantConfigEntity;
import io.shulie.surge.config.common.response.TenantConfigurationResponse;
import io.shulie.surge.data.common.utils.HttpUtil;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * clickhouse 配置同步器
 * <pre>
 * 1、获取amdb表中配置的集群信息
 * 2、获取takin-web中维护的租户集群配置
 * </pre>
 */
public class ClickhouseConfigurationSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClickhouseConfigurationSynchronizer.class);

    /**
     * 控制台地址及存储方案地址配置
     */
    public static final String TRO_IP = "tro.url.ip";
    public static final String TRO_PORT = "tro.port";
    public static final String TRO_CONFIG_PATH = "tro.api.config.path";

    /**
     * clickhouse集群配置项
     */
    private static final String URL_KEY = "clickhouse.cluster.url";
    private static final String USER_NAME_KEY = "clickhouse.cluster.userName";
    private static final String PASSWORD_KEY = "clickhouse.cluster.password";
    private static final String BATCH_COUNT_KEY = "clickhouse.cluster.batchCount";
    public static final String DEFAULT_BATCH_COUNT = "10000";

    private final MysqlSupport mysqlSupport;
    private final String troIp;
    private final Integer troPort;
    private final String troConfigPath;

    public ClickhouseConfigurationSynchronizer(MysqlSupport mysqlSupport, Properties properties) {
        Assert.notNull(mysqlSupport, "mysqlSupport must not null");
        Assert.notNull(properties, "properties must not null");
        this.mysqlSupport = mysqlSupport;
        this.troIp = properties.getProperty(TRO_IP);
        this.troConfigPath = properties.getProperty(TRO_CONFIG_PATH);
        String port = properties.getProperty(TRO_PORT);
        if (StringUtils.isAnyBlank(troIp, port, troConfigPath)) {
            throw new RuntimeException(String.format("configuration [%s, %s, %s] must config", TRO_IP, TRO_PORT, TRO_CONFIG_PATH));
        }
        this.troPort = Integer.parseInt(port);
    }

    public List<ClickhouseClusterConfigEntity> syncTenantClusterConfiguration() {
        Map<String, Configuration> configurationMap = loadLocalClusterConfiguration();
        if (CollectionUtils.isEmpty(configurationMap)) {
            return Lists.newArrayList();
        }
        List<TenantConfigEntity> data = queryTenantClusterConfiguration();
        if (CollectionUtils.isEmpty(data)) {
            return Lists.newArrayList();
        }
        return mergeConfiguration(data, configurationMap);
    }

    /**
     * 获取租户存储方案
     *
     * @return 租户存储方案
     */
    private List<TenantConfigEntity> queryTenantClusterConfiguration() {
        TenantConfigurationResponse response = JSON.parseObject(
            HttpUtil.doGet(troIp, troPort, troConfigPath, null, null), TenantConfigurationResponse.class);
        return response == null ? null : response.getData();
    }

    /**
     * 转换 response -> ClickhouseClusterConfigEntity
     *
     * @param data                 控制台配置的租户存储方案配置
     * @param clusterConfiguration 集群配置信息
     * @return {@link ClickhouseClusterConfigEntity}
     */
    private List<ClickhouseClusterConfigEntity> mergeConfiguration(List<TenantConfigEntity> data, Map<String, Configuration> clusterConfiguration) {
        List<InnerClickhouseConfigEntity> entityList = data.stream().map(this::convertConfigEntityToInnerConfig)
            .filter(entities -> !CollectionUtils.isEmpty(entities))
            .flatMap(Collection::stream).collect(Collectors.toList());
        return entityList.stream().filter(configEntity -> {
            ClickhouseConfigValueEntity entity = configEntity.getEntity();
            boolean available = entity != null && clusterConfiguration.containsKey(entity.getClickhouseNumber());
            if (!available && LOGGER.isInfoEnabled()) {
                LOGGER.info("inValid configuration：key: [{}], value: [{}]", configEntity.getConfigKey(), configEntity.getConfigValue());
            }
            return available;
        }).map(configEntity -> buildClusterConfigurationEntity(clusterConfiguration, configEntity)).collect(Collectors.toList());
    }

    /**
     * 构建完整clickhouse租户配置实体
     *
     * @param clusterConfiguration 本地clickhouse集群配置
     * @param configEntity         租户clickhouse集群配置
     * @return 完整clickhouse租户配置
     */
    private ClickhouseClusterConfigEntity buildClusterConfigurationEntity(
        Map<String, Configuration> clusterConfiguration, InnerClickhouseConfigEntity configEntity) {

        ClickhouseClusterConfigEntity clusterConfigEntity = new ClickhouseClusterConfigEntity();
        ClickhouseConfigValueEntity entity = configEntity.getEntity();
        Configuration configuration = clusterConfiguration.get(entity.getClickhouseNumber());
        clusterConfigEntity.setUserAppKey(configEntity.getTenantAppKey());
        clusterConfigEntity.setTenantName(configEntity.getTenantCode());
        clusterConfigEntity.setEnvCode(configEntity.getEnvCode());
        clusterConfigEntity.setClusterName(configuration.getName());
        clusterConfigEntity.setClusterDesc(configuration.getDesc());
        clusterConfigEntity.setTtl(entity.getTtl());

        Map<String, String> itemMap = configuration.convertItemsToMap();
        clusterConfigEntity.setClusterAddress(itemMap.get(URL_KEY));
        clusterConfigEntity.setUserName(itemMap.get(USER_NAME_KEY));
        clusterConfigEntity.setPassword(itemMap.get(PASSWORD_KEY));
        String batchCount = itemMap.get(BATCH_COUNT_KEY);
        if (!StringUtils.isNumeric(batchCount)) {
            batchCount = DEFAULT_BATCH_COUNT;
        }
        clusterConfigEntity.setBatchCount(Integer.parseInt(batchCount));
        return clusterConfigEntity;
    }

    /**
     * 获取amdb维护的clickhouse集群信息
     *
     * @return key=集群配置number，value=clickhouse集群配置
     */
    private Map<String, Configuration> loadLocalClusterConfiguration() {
        String sql = "select conf.`number` `number`, conf.`name` `name`, conf.`desc` `desc`, conf.`type` `type`, "
            + "conf.`available_env` `availableEnv`, item.`key` `key`, item.`value` `value` from t_amdb_configuration conf "
            + "left join t_amdb_configuration_item item on conf.id = item.configuration_id "
            + "where conf.type = '" + ConfigurationTypeEnum.clickhouse.name() + "'";
        List<ConfigurationRowEntity> configurationList = mysqlSupport.query(sql, new BeanPropertyRowMapper<>(ConfigurationRowEntity.class));
        return convertConfigurationEntity(configurationList);
    }

    /**
     * 转换amdbclickhouse集群配置
     *
     * @param configurationList clickhouse集群配置
     * @return clickhouse集群配置，key=集群配置number，value=clickhouse集群配置
     */
    private Map<String, Configuration> convertConfigurationEntity(List<ConfigurationRowEntity> configurationList) {
        if (CollectionUtils.isEmpty(configurationList)) {
            return null;
        }
        return configurationList.stream().map(ConfigurationRowEntity::convertToConfiguration)
            .filter(entity -> !CollectionUtils.isEmpty(entity.getItems()))
            .collect(Collectors.toMap(Configuration::getNumber, Function.identity(),
                (oldValue, newValue) -> {
                    newValue.getItems().addAll(oldValue.getItems());
                    return newValue;
                }));
    }

    /**
     * amdb clickhouse集群配置实体
     */
    @Data
    static class ConfigurationRowEntity {
        private String number;  // 集群编码
        private String name;    // 集群名称
        private String desc;    // 集群描述
        private String type;    // 集群类型，见 ConfigurationTypeEnum
        private String availableEnv;    // 适用环境
        private String key;     // 配置项key
        private String value;   // 配置项value

        public Configuration convertToConfiguration() {
            Configuration configuration = new Configuration();
            configuration.setName(getName());
            configuration.setNumber(getNumber());
            configuration.setDesc(getDesc());
            configuration.setType(getType());
            configuration.setAvailableEnv(getAvailableEnv());
            String key = getKey();
            String value = getValue();
            if (!StringUtils.isAnyBlank(key, value)) {
                List<ConfigurationItem> items = new ArrayList<>(5);
                items.add(new ConfigurationItem(key, value));
                configuration.setItems(items);
            }
            return configuration;
        }
    }

    /**
     * 配置项值
     */
    @Data
    static class ClickhouseConfigValueEntity implements Serializable {
        private String clickhouseNumber;    // clickhouse集群编码
        private String clickhouseName;      // clickhouse集群名称
        private String clickhouseDesc;      // clickhouse集群描述
        private String ttl;                 // clickhouse数据过期时间
    }

    /**
     * 内部clickhouse配置
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    static class InnerClickhouseConfigEntity extends TenantConfigItemEntity {
        private String tenantAppKey;
        private String tenantCode;
        private String envCode;
        private ClickhouseConfigValueEntity entity;

        /**
         * 转换配置项详细信息为clickhouse配置结构
         *
         * @return clickhouse配置结构
         */
        private ClickhouseConfigValueEntity getEntity() {
            String configValue;
            if (entity == null && StringUtils.isNotBlank(configValue = getConfigValue())) {
                entity = JSON.parseObject(configValue, ClickhouseConfigValueEntity.class);
            }
            return entity;
        }
    }

    /**
     * 转换控制台配置数据结构为surge内部使用配置结构，并过滤非clickhouse数据
     *
     * @param configEntity 控制台配置
     * @return clickhouse内部配置
     */
    private List<InnerClickhouseConfigEntity> convertConfigEntityToInnerConfig(TenantConfigEntity configEntity) {
        Map<String, List<TenantConfigItemEntity>> configs;
        if (configEntity == null || CollectionUtils.isEmpty(configs = configEntity.getConfigs())) {
            return null;
        }
        List<InnerClickhouseConfigEntity> entityList = new ArrayList<>(configs.size());
        String tenantAppKey = configEntity.getTenantAppKey();
        String tenantCode = configEntity.getTenantCode();
        configs.forEach((key, value) -> value.stream().filter(
            itemEntity -> ConfigurationTypeEnum.clickhouse.getConfigKey().equals(itemEntity.getConfigKey()))
            .forEach(itemEntity -> {
                InnerClickhouseConfigEntity entity = new InnerClickhouseConfigEntity();
                entity.setTenantAppKey(tenantAppKey);
                entity.setTenantCode(tenantCode);
                entity.setEnvCode(key);
                entity.setConfigKey(itemEntity.getConfigKey());
                entity.setConfigValue(itemEntity.getConfigValue());
                entity.setConfigDesc(itemEntity.getConfigDesc());
                entityList.add(entity);
            }));
        return entityList;
    }
}
