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

package io.shulie.surge.data.deploy.pradar.digester;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.deploy.pradar.model.ResourceModel;
import io.shulie.surge.data.deploy.pradar.parser.MiddlewareType;
import io.shulie.surge.data.deploy.pradar.parser.PradarLogType;
import io.shulie.surge.data.runtime.common.remote.DefaultValue;
import io.shulie.surge.data.runtime.common.remote.Remote;
import io.shulie.surge.data.runtime.common.utils.ApiProcessor;
import io.shulie.surge.data.runtime.digest.DataDigester;
import io.shulie.surge.data.runtime.digest.DigestContext;
import io.shulie.surge.data.sink.kafka.KafkaSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 压测资源上报kafka
 *
 * @author pamirs
 */
@Singleton
public class ResourceDigester implements DataDigester<RpcBased> {
    private static final Logger logger = LoggerFactory.getLogger(ResourceDigester.class);
    @Inject
    private KafkaSupport kafkaSupport;
    @Inject
    @DefaultValue("false")
    @Named("/pradar/config/rt/kafkaDisable")
    private Remote<Boolean> kafkaDisable;
    @Inject
    @Named("config.resource.topic")
    private String topic;
    private static String TAG = "01"; //0x01 代表压测引擎生成的数据 转换PID=>ReportId
    private static String UNKNOW = "unknow";
    private static String IP_16 = "ffffffff";
    public static final String AITE = "@";
    public static final String SYSTEM_CODE = "SYSTEM_CODE";
    public static final String SERVICE_NAME = "SERVICE_NAME";
    public static final String SERVICE_DLE_NAME = "SERVICE_DLE_NAME";
    private static Map<Integer, String> rpcMap = new HashMap<>();
    private static Map<Integer, String> serverMap = new HashMap<>();
    private transient AtomicBoolean isRunning = new AtomicBoolean(false);

    private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private static Map<String, ResourceModel> resourceMap = new java.util.concurrent.ConcurrentHashMap<>();
    private static Map<String, ResourceModel> appMap = new java.util.concurrent.ConcurrentHashMap<>();
    //在发送时，允许丢一部分数据 true-正在发送kafka
    private volatile boolean isSendFlag = false;

    static {
        rpcMap.put(MiddlewareType.TYPE_MQ, "MQ");
        rpcMap.put(MiddlewareType.TYPE_DB, "DB");
        rpcMap.put(MiddlewareType.TYPE_CACHE, "CACHE");
        rpcMap.put(MiddlewareType.TYPE_SEARCH, "SEARCH");

        serverMap.put(MiddlewareType.TYPE_WEB_SERVER, "APP");
        serverMap.put(MiddlewareType.TYPE_RPC, "APP");
        serverMap.put(MiddlewareType.TYPE_MQ, "APP");
        serverMap.put(MiddlewareType.TYPE_JOB, "APP");
    }

    public void init() {
        //启动一个定时任务,每隔1分钟运行一次，将resourceMap数据发送kafka
        executor.scheduleAtFixedRate(() -> {
            if(resourceMap.isEmpty() && appMap.isEmpty()) {
                return;
            }
            logger.info("ready send kafka data resource size={}, app size={}", resourceMap.size(), appMap.size());
            isSendFlag = true;
            resourceMap.forEach((key, value) -> {
                try {
                    kafkaSupport.sendMq(topic, value.getResourceName()+"-"+value.getResourceUrl(), JSON.toJSONString(value));
                } catch (Exception e) {
                    logger.warn("fail to send resource to kafka, error:" + ExceptionUtils.getStackTrace(e));
                }
            });
            resourceMap.clear();
            appMap.forEach((key, value) -> {
                try {
                    kafkaSupport.sendMq(topic, value.getResourceName()+"-"+value.getResourceUrl(), JSON.toJSONString(value));
                } catch (Exception e) {
                    logger.warn("fail to send resource-app to kafka, error:" + ExceptionUtils.getStackTrace(e));
                }
            });
            appMap.clear();
            isSendFlag = false;
        }, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void digest(DigestContext<RpcBased> context) {
        if (kafkaDisable.get()) {
            return;
        }
        if (isSendFlag) {
            return;
        }
        if (isRunning.compareAndSet(false, true)) {
            init();
        }
        RpcBased rpcBased = context.getContent();
        try {
            if (rpcBased == null) {
                return;
            }
            //非压测且非压力引擎的流量，忽略掉
            if(!rpcBased.isClusterTest() && !rpcBased.getTraceId().endsWith(TAG)) {
                return;
            }
            //资源类
            if(rpcMap.keySet().contains(rpcBased.getRpcType())) {
                dealResource(rpcBased);
            }
            //应用类
            if(serverMap.keySet().contains(rpcBased.getRpcType())) {
                dealApp(rpcBased);
            }
        } catch (Throwable e) {
            logger.warn("fail to save resource to cache, log: " + rpcBased.getLog() + ", error:" + ExceptionUtils.getStackTrace(e));
        }
    }

    private static void dealResource(RpcBased rpcBased) {
        String remoteIp = rpcBased.getRemoteIp();
        //错误异常，忽略掉
        if(StringUtils.isBlank(remoteIp) || StringUtils.containsIgnoreCase(remoteIp, UNKNOW)) {
            return;
        }
        //报告ID
        Long reportId = parseReportId(rpcBased.getTraceId());
        ResourceModel resourceModel = new ResourceModel();
        resourceModel.setPressureTime(calcMillSeconds(rpcBased.getStartTime()));
        resourceModel.setResourceType(rpcMap.get(rpcBased.getRpcType()));
        resourceModel.setResourceName(rpcBased.getMiddlewareName());
        int port = NumberUtils.toInt(rpcBased.getPort(), 0);
        if(port == 0) {
            resourceModel.setResourceUrl(remoteIp);
        } else {
            resourceModel.setResourceUrl(remoteIp + ":" + port);
        }
        //能解析出报告ID，则获取场景ID，否则直接保存
        if(reportId > 0) {
            Long sceneId = ApiProcessor.matchReportId(reportId);
            if (sceneId == null || sceneId <= 0) {
                return;
            }
            resourceModel.setSceneIds(Sets.newHashSet(sceneId));
        } else {
            resourceModel.setSceneIds(Sets.newHashSet(reportId));
        }
        String cacheKey = resourceModel.getPressureTime() +"_" + resourceModel.getResourceType() + "_" + resourceModel.getResourceUrl();
        ResourceModel cacheModel = resourceMap.get(cacheKey);
        if(cacheModel == null) {
            resourceMap.put(cacheKey, resourceModel);
        } else {
            cacheModel.getSceneIds().addAll(resourceModel.getSceneIds());
        }
    }

    private static void dealApp(RpcBased rpcBased) {
        //只要服务端的trace
        if(rpcBased.getLogType() != PradarLogType.LOG_TYPE_RPC_SERVER && rpcBased.getLogType() != PradarLogType.LOG_TYPE_TRACE) {
            return;
        }
        Map<String, Object> localAttributes = rpcBased.getLocalAttributes();
        if(localAttributes == null || localAttributes.isEmpty()) {
            return;
        }
        String systemCode = (String) localAttributes.get(AITE + SYSTEM_CODE);
        if(StringUtils.isBlank(systemCode)) {
            return;
        }
        String serviceName = (String) localAttributes.get(AITE + SERVICE_NAME);
        if(StringUtils.isBlank(serviceName)) {
            return;
        }
        String serviceDleName = (String) localAttributes.get(AITE + SERVICE_DLE_NAME);
        if(StringUtils.isBlank(serviceDleName)) {
            return;
        }
        //报告ID
        Long reportId = parseReportId(rpcBased.getTraceId());
        ResourceModel resourceModel = new ResourceModel();
        resourceModel.setPressureTime(calcMillSeconds(rpcBased.getStartTime()));
        resourceModel.setResourceType(serverMap.get(rpcBased.getRpcType()));
        resourceModel.setResourceName(resourceModel.getResourceType());
        resourceModel.setResourceUrl(rpcBased.getAppName());
        //能解析出报告ID，则获取场景ID，否则直接保存
        if(reportId > 0) {
            Long sceneId = ApiProcessor.matchReportId(reportId);
            if (sceneId == null || sceneId <= 0) {
                return;
            }
            resourceModel.setSceneIds(Sets.newHashSet(sceneId));
        } else {
            resourceModel.setSceneIds(Sets.newHashSet(reportId));
        }
        List<Map<String, String>> clusterList = Lists.newArrayList();
        Map<String, String> clusterMap = new HashMap<>();
        clusterMap.put(SYSTEM_CODE, systemCode);
        clusterMap.put(SERVICE_NAME, serviceName);
        clusterMap.put(SERVICE_DLE_NAME, serviceDleName);
        clusterList.add(clusterMap);
        resourceModel.setClusterIds(clusterList);
        resourceModel.setClusterSet(Sets.newHashSet(JSON.toJSONString(clusterMap)));
        String cacheKey = resourceModel.getPressureTime() +"_" + resourceModel.getResourceType() + "_" + resourceModel.getResourceUrl();
        ResourceModel cacheModel = appMap.get(cacheKey);
        if(cacheModel == null) {
            appMap.put(cacheKey, resourceModel);
        } else {
            cacheModel.getSceneIds().addAll(resourceModel.getSceneIds());
            if(!cacheModel.getClusterSet().containsAll(resourceModel.getClusterSet())) {
                cacheModel.getClusterSet().addAll(resourceModel.getClusterSet());
                cacheModel.getClusterIds().addAll(resourceModel.getClusterIds());
            }
        }
    }

    private static Long parseReportId(String traceId) {
        try {
            String taskId = traceId.substring(0, IP_16.length());
            int pos = taskId.indexOf("z");
            if(pos == -1) {
                return -1L;
            } else {
                return Long.parseLong(taskId.substring(0, pos), 16);
            }
        } catch (Exception e) {
            return -1L;
        }
    }

    private static Long calcMillSeconds(Long startTime) {
        return (startTime / (60 * 1000L)) * 60 * 1000L;
    }

    @Override
    public int threadCount() {
        return 1;
    }

    @Override
    public void stop() {
        try {
            this.kafkaSupport.stop();
        } catch (Throwable e) {
            logger.error("clickhouse stop fail");
        }
    }

    public Remote<Boolean> getKafkaDisable() {
        return kafkaDisable;
    }
}
