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

package io.shulie.surge.data.deploy.pradar.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.shulie.surge.data.common.utils.HttpUtil;
import io.shulie.surge.data.common.zk.ZkClient;
import io.shulie.surge.data.deploy.pradar.link.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Singleton
public class AppConfigUtil {
    private final Logger logger = LoggerFactory.getLogger(AppConfigUtil.class);

    @Inject
    @Named("config.simpling.app.zk.path")
    private String appSamplingZkPath;

    @Inject
    @Named("config.simpling.global.zk.path")
    private String globalSamplingPath;

    @Inject
    @Named("config.slowSql.app.zk.path")
    private String appSlowSqlZkPath;

    @Inject
    @Named("config.slowSql.global.zk.path")
    private String globalSlowSqlPath;

    @Inject
    @Named("amdb.url.ip")
    private String URI;

    @Inject
    @Named("amdb.api.troData.path")
    private String PATH;

    @Inject
    @Named("amdb.port")
    private String PORT;

    @Inject(optional = true)
    private ZkClient zkClient;

    @Inject(optional = true)
    private ConfigService configService;

    private volatile Map<String, Object> nacosConfigs;

    /**
     * 采样率缓存
     */
    LoadingCache<String, Integer> samplingCache = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.MINUTES).build(new CacheLoader<String, Integer>() {
        @Override
        public Integer load(String appName) throws Exception {
            // 应用自定义采样率
            String[] params = appName.split("@~@");
            String sampling = null;
            if (params.length == 4) {
                Map<String, Object> res = null;
                Map<String, String> paramMap = Maps.newHashMap();
                paramMap.put("userAppKey", params[0]);
                paramMap.put("envCode", params[1]);
                paramMap.put("appName", params[2]);

                //如果是压测流量,取压测流量采样率
                if ("true".equals(params[3])) {
                    //获取应用压测采样率
                    paramMap.put("configKey", "trace.ct.samplingInterval");
                } else {
                    //获取应用采样率
                    paramMap.put("configKey", "trace.samplingInterval");
                }
                try {
                    res = JSON.parseObject(HttpUtil.doGet(URI, Integer.valueOf(PORT), PATH, null, paramMap));
                    if (res != null && res.containsKey("data")) {
                        sampling = StringUtil.formatString(res.get("data"));
                        logger.info("get app sampling:{},params is {}", sampling, paramMap);
                    }
                } catch (Throwable e) {
                    logger.error("get app sampling catch exception:{},{}", e, e.getStackTrace());
                }
            }

            // 全局采样率
            if (StringUtils.isBlank(sampling)) {
                sampling = getValueFromRemoteConfigs(globalSamplingPath);
                logger.info("get global sampling:{}", sampling);
            }
            return NumberUtils.toInt(sampling, 1);
        }
    });

    /**
     * 慢SQL配置缓存
     */
    LoadingCache<String, Integer> slowSqlCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build(new CacheLoader<String, Integer>() {
        @Override
        public Integer load(String appName) throws Exception {
            // 应用自定义采样率
            String zkPath = appSlowSqlZkPath.replace("{appName}", appName);
            String simpling = getValueFromRemoteConfigs(zkPath);
            // 全局采样率
            if (StringUtils.isBlank(simpling)) {
                simpling = getValueFromRemoteConfigs(globalSlowSqlPath);
            }
            return NumberUtils.toInt(simpling, 1000);
        }
    });

    private String getValueFromRemoteConfigs(String dataId) throws Exception {
        Object value = null;
        if (zkClient != null) {
            if (zkClient.exists(dataId)) {
                byte[] data = zkClient.getData(dataId);
                if (data != null) {
                    value = new String(data, "utf-8");
                }
            }
        }
        if (configService != null) {
            if (nacosConfigs == null) {
                String nacosId = "pradarConfig", group = "PRADAR_CONFIG";
                configService.addListener(nacosId, group, new AbstractListener() {
                    @Override
                    public void receiveConfigInfo(String s) {
                        nacosConfigs = JSON.parseObject(s, Map.class);
                    }
                });
                String content = configService.getConfig(nacosId, group, 3000);
                nacosConfigs = JSON.parseObject(content, Map.class);
                value = nacosConfigs.get(dataId);
            } else {
                value = nacosConfigs.get(dataId);
            }
        }
        return value != null ? value.toString() : null;
    }

    /**
     * 获取应用采样率配置
     *
     * @param appName
     * @return
     */
    public int getAppSamplingByAppName(String userAppKey, String envCode, String appName, String clusterTest) {
        try {
            return samplingCache.get(userAppKey + "@~@" + envCode + "@~@" + appName + "@~@" + clusterTest);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * 获取应用慢SQL配置
     *
     * @param appName
     * @return
     */
    public int getAppSlowSqlConfigByAppName(String appName) {
        try {
            return slowSqlCache.get(appName);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return 1000;
    }
}
