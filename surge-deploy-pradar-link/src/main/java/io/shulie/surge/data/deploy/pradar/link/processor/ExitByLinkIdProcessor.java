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

package io.shulie.surge.data.deploy.pradar.link.processor;


import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.pamirs.pradar.log.parser.constant.TenantConstants;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.deploy.pradar.link.AbstractLinkCache;
import io.shulie.surge.data.deploy.pradar.link.TaskManager;
import io.shulie.surge.data.deploy.pradar.link.enums.TraceLogQueryScopeEnum;
import io.shulie.surge.data.deploy.pradar.link.model.LinkEntranceModel;
import io.shulie.surge.data.deploy.pradar.link.util.ServiceUtils;
import io.shulie.surge.data.deploy.pradar.parser.PradarLogType;
import io.shulie.surge.data.deploy.pradar.parser.utils.Md5Utils;
import io.shulie.surge.data.runtime.common.remote.DefaultValue;
import io.shulie.surge.data.runtime.common.remote.Remote;
import io.shulie.surge.data.sink.clickhouse.ClickHouseSupport;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 出口(远程调用)梳理,按链路来梳理
 *
 * @author sunshiyu
 */
public class ExitByLinkIdProcessor extends AbstractProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ExitByLinkIdProcessor.class);

    //远程调用/白名单保存表
    private static final String LINK_ENTRANCE_NAME = "t_amdb_pradar_link_entrance";

    @Inject
    private LinkProcessor linkProcessor;

    @Inject
    private ClickHouseSupport clickHouseSupport;

    @Inject
    private MysqlSupport mysqlSupport;

    @Inject
    private TaskManager<String, String> taskManager;

    /**
     * 是否开启出口(远程调用)功能扫描
     */
    @Inject
    @DefaultValue("true")
    @Named("/pradar/config/rt/exitProcessDisable")
    private Remote<Boolean> exitProcessDisable;

    /**
     * 间隔时间,默认两分钟
     */
    @Inject
    @DefaultValue("120")
    @Named("/pradar/config/rt/exitProcess/delayTime")
    private Remote<Long> intervalTime;

    public AbstractLinkCache linkCache;

    String linkEntranceInsertSql = "";
    String linkEntranceDeleteSql = "";

    private static int DEFAULT_DELAY_TIME = 2;

    public void init(String dataSourceType) {
        //设置数据源
        this.setDataSourceType(dataSourceType);
        linkEntranceInsertSql = "INSERT INTO " + LINK_ENTRANCE_NAME + LinkEntranceModel.getCols() + " VALUES "
                + LinkEntranceModel.getParamCols() + LinkEntranceModel.getOnDuplicateCols();
    }

    /**
     * 保存链路出口(远程调用)/出口信息
     *
     * @param taskIds
     * @param currentTaskId
     * @throws IOException
     */
    @Override
    public void share(List<String> taskIds, String currentTaskId) {
        if (!exitProcessDisable.get()) {
            return;
        }
        /*if (!isHandler(intervalTime.get())) {
            return;
        }*/
        Map<String, Map<String, Object>> linkConfig = linkCache.getLinkConfig();
        if (linkConfig == null || linkConfig.isEmpty()) {
            return;
        }
        Set<String> linkIdSet = linkConfig.keySet();
        String linkId = "21f8392b2e105aa92a3a98ffe21bea64";
        if (linkIdSet.contains(linkId)) {
            Map<String, Object> link = linkConfig.get(linkId);
            processExitByLinkId(linkId, link);
        }
        //Map<String, List<String>> avgMap = taskManager.allotOfAverage(taskIds, new ArrayList<>(linkIdSet));
        //List<String> avgList = avgMap.get(currentTaskId);
        //if (CollectionUtils.isNotEmpty(avgList)) {
        //    for (int i = 0; i < avgList.size(); i++) {
        //        String linkId = avgList.get(i);
        //        Map<String, Object> link = linkConfig.get(avgList.get(i));
        //        processExitByLinkId(linkId, link);
        //    }
        //}
    }

    private void processExitByLinkId(String linkId, Map<String, Object> linkConfig) {
        // 根据链路配置查询traceId
        logger.info("processExitByLink{},{}", linkId, linkConfig);
        try {
            linkProcessor.setDataSourceType(this.getDataSourceType());
            Pair<List<RpcBased>, Map<String, String>> traceLog = linkProcessor.getTraceLog(linkConfig, TraceLogQueryScopeEnum.build(5));
            List<RpcBased> rpcBasedList = traceLog.getLeft();
            if (CollectionUtils.isEmpty(rpcBasedList)) {
                logger.warn("processExitByLink is empty{},{}", linkId, linkConfig);
                return;
            }
            // 按traceId分组
            Map<String, List<RpcBased>> rpcBasedMap = rpcBasedList.stream().collect(
                    Collectors.groupingBy(rpcBased -> rpcBased.getTraceId()));

            List<LinkEntranceModel> linkEntranceModels = Lists.newArrayList();
            String userAppKey = String.valueOf(linkConfig.get("userAppKey"));
            String envCode = String.valueOf(linkConfig.get("envCode"));
            rpcBasedMap.entrySet().forEach(entry -> {
                List<RpcBased> rpcBaseds = entry.getValue();
                // 只需要http客户端和dubbo客户端
                List<RpcBased> clientList = rpcBaseds.stream().filter(rpcBased ->
                        ServiceUtils.isHttpClient(rpcBased) || ServiceUtils.isRpcClient(rpcBased)).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(clientList)) {
                    return;
                }
                List<LinkEntranceModel> modelList = clientList.stream().map(client -> {
                    LinkEntranceModel model = new LinkEntranceModel();
                    model.setEntranceId(client.getEntranceId());
                    model.setAppName(client.getAppName());
                    model.setRpcType(client.getRpcType());
                    model.setServiceName(client.getParsedServiceName());
                    model.setMethodName(client.getMethodName());
                    model.setExtend(client.getParsedExtend());
                    String entrance = String.format("%s|%s|%s|%s|%s", client.getServiceName(),
                            client.getAppName(),
                            client.getRpcType(),
                            client.getMethodName(),
                            client.getMiddlewareName(),
                            "2");
                    model.setEntranceId(Md5Utils.md5(entrance));
                    model.setLinkId(linkId);
                    // 标记由链路维度梳理的
                    model.setLinkType("2");
                    model.setRpcId(client.getRpcId());
                    model.setMiddlewareName(client.getParsedMiddlewareName());
                    model.setMiddlewareDetail(client.getMiddlewareName());
                    model.setGmtModify(new Date());
                    model.setUserId(TenantConstants.DEFAULT_USERID);
                    model.setUserAppKey(userAppKey);
                    model.setEnvCode(envCode);
                    model.setUpAppName("");
                    model.setDownAppName("");
                    model.setDefaultWhiteInfo("");
                    return model;
                }).collect(Collectors.toList());
                linkEntranceModels.addAll(modelList);
            });

            mysqlSupport.batchUpdate(linkEntranceInsertSql, new ArrayList<>(
                    linkEntranceModels.stream().map(LinkEntranceModel::getValues).collect(Collectors.toSet())));
            logger.info("{} saveExitByLinkId is ok,size: {}", linkId, linkEntranceModels.size());
        } catch (Throwable e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public void setLinkCache(AbstractLinkCache linkCache) {
        this.linkCache = linkCache;
    }
}
