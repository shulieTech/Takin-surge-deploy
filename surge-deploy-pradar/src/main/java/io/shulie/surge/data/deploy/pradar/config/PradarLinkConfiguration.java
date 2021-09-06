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

package io.shulie.surge.data.deploy.pradar.config;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.shulie.surge.data.common.aggregation.Scheduler;
import io.shulie.surge.data.deploy.pradar.common.DataBootstrapEnhancer;
import io.shulie.surge.data.deploy.pradar.common.ParamUtil;
import io.shulie.surge.data.deploy.pradar.common.PradarStormConfigHolder;
import io.shulie.surge.data.deploy.pradar.link.AbstractLinkCache;
import io.shulie.surge.data.deploy.pradar.link.processor.*;
import io.shulie.surge.data.runtime.common.DataBootstrap;
import io.shulie.surge.data.runtime.common.DataRuntime;
import io.shulie.surge.data.sink.clickhouse.ClickHouseModule;
import io.shulie.surge.data.sink.mysql.MysqlModule;
import io.shulie.surge.deploy.pradar.common.CommonStat;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PradarLinkConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(PradarLinkConfiguration.class);

    /**
     * 启用数据写入到clickhouse,否则写入到mysql
     */
    private String dataSourceType;

    private static long defaultDelayTime = 30;

    private static long periodTime = 60;

    private static String defaultTaskId = "1";

    public PradarLinkConfiguration() {
    }

    public PradarLinkConfiguration(Object dataSourceType) {
        this.dataSourceType = Objects.toString(dataSourceType);
    }

    /**
     * 初始化initDataRuntime
     *
     * @throws Exception
     */
    public DataRuntime initDataRuntime() {
        DataBootstrap bootstrap = DataBootstrap.create("deploy.properties");
        DataBootstrapEnhancer.enhancer(bootstrap);
        bootstrap.install(new PradarModule(), new ClickHouseModule(), new MysqlModule());
        return bootstrap.startRuntime();
    }

    /**
     * 初始化
     *
     * @throws Exception
     */
    public void initWithTaskSize(List<String> allTaskIds, String currentTaskId) {
        DataRuntime dataRuntime = initDataRuntime();
        Scheduler scheduler = new Scheduler(6);
        try {
            /**
             * 链路梳理任务，此功能是将配置了业务活动的入口，梳理其链路图
             */
            LinkProcessor linkProcessor = dataRuntime.getInstance(LinkProcessor.class);
            linkProcessor.init(dataSourceType);
            scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        linkProcessor.share(allTaskIds, currentTaskId);
                    } catch (Exception e) {
                        logger.error("do link task error!", e);
                    }
                }
            }, defaultDelayTime, periodTime, TimeUnit.SECONDS);

            /**
             * 链路入口梳理
             */
            EntranceProcessor entranceProcessor = dataRuntime.getInstance(EntranceProcessor.class);
            entranceProcessor.init(dataSourceType);
            scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        entranceProcessor.share(allTaskIds, currentTaskId);
                    } catch (Throwable e) {
                        logger.error("do link_entrance task error!", e);
                    }
                }
            }, defaultDelayTime, periodTime, TimeUnit.SECONDS);

            scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        // 两天未更新删除
                        entranceProcessor.shareExpire(2);
                    } catch (Exception e) {
                        logger.error("do EntranceProcessor.shareExpire task error!", e);
                    }
                }
            }, defaultDelayTime, 5, TimeUnit.HOURS);

            processUnknow(dataRuntime, linkProcessor.getLinkCache(), allTaskIds, currentTaskId);
        } catch (Exception e) {
            logger.error("Build task error.", e);
        }

    }


    /**
     * 初始化
     *
     * @throws Exception
     */
    public void init() throws Exception {
        initWithTaskSize(Arrays.asList(defaultTaskId), defaultTaskId);
    }

    /**
     * 处理未知,此功能项是诊断链路中是否有存在未接入应用节点，确保链路梳理的完整性
     *
     * @param dataRuntime
     */
    private void processUnknow(DataRuntime dataRuntime, AbstractLinkCache linkCache, List<String> allTaskIds, String currentTaskId) {
        try {
            LinkUnKnowNodeProcessor linkUnKnowNodeProcessor = dataRuntime.getInstance(LinkUnKnowNodeProcessor.class);
            linkUnKnowNodeProcessor.init(dataSourceType);
            linkUnKnowNodeProcessor.setLinkCache(linkCache);
            ScheduledExecutorService linkUnKnowNodeExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("linkUnKnowNodeProcessor-%d").build());
            linkUnKnowNodeExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        linkUnKnowNodeProcessor.share(allTaskIds, currentTaskId);
                    } catch (Exception e) {
                        logger.error("do link task error!", e);
                    }
                }
            }, defaultDelayTime, periodTime, TimeUnit.SECONDS);

            LinkUnKnowMQProcessor linkUnKnownMqProcessor = dataRuntime.getInstance(LinkUnKnowMQProcessor.class);
            linkUnKnownMqProcessor.setLinkCache(linkCache);
            ScheduledExecutorService linkUnKnownMqService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("LinkUnKnowMQProcessor-%d").build());
            linkUnKnownMqProcessor.init();
            linkUnKnownMqService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        linkUnKnownMqProcessor.share(allTaskIds, currentTaskId);
                    } catch (Exception e) {
                        logger.error("do link task error!", e);
                    }
                }
            }, defaultDelayTime, periodTime * 10, TimeUnit.SECONDS);

            /**
             * 未知清理
             */
            LinkUnKnowNodeCleanProcessor linkUnKnowNodeCleanProcessor = dataRuntime.getInstance(LinkUnKnowNodeCleanProcessor.class);
            linkUnKnowNodeCleanProcessor.init();
            linkUnKnowNodeCleanProcessor.setLinkCache(linkCache);
            ScheduledExecutorService linkUnKnowNodeCleanExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("LinkUnKnowNodeCleanProcessor-%d").build());
            linkUnKnowNodeCleanExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        linkUnKnowNodeCleanProcessor.share(allTaskIds, currentTaskId);
                    } catch (Exception e) {
                        logger.error("do link task error!", e);
                    }
                }
            }, 0, 5, TimeUnit.HOURS);

        } catch (Throwable e) {
            logger.error("process unknow error" + ExceptionUtils.getStackTrace(e));
        }
    }


    /**
     * 简单使用 启动链路梳理功能, 此处功能默认数据链路数据存储到mysql
     * java -cp xxx.jar io.shulie.surge.data.deploy.pradar.config.PradarLinkConfiguration -D
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> inputMap = Maps.newHashMap();
        ParamUtil.parseInputParam(inputMap, args);
        // 此处默认使用mysql
        inputMap.put(ParamUtil.DATA_SOURCE_TYPE, CommonStat.MYSQL);
        PradarLinkConfiguration pradarLinkConfiguration = new
                PradarLinkConfiguration(inputMap.get(ParamUtil.DATA_SOURCE_TYPE));
        PradarStormConfigHolder.init(inputMap);
        pradarLinkConfiguration.init();
    }
}
