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

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.deploy.pradar.link.AbstractLinkCache;
import io.shulie.surge.data.deploy.pradar.link.LinkSqlContants;
import io.shulie.surge.data.deploy.pradar.link.TaskManager;
import io.shulie.surge.data.deploy.pradar.link.enums.TraceLogQueryScopeEnum;
import io.shulie.surge.data.deploy.pradar.link.model.LinkEdgeModel;
import io.shulie.surge.data.deploy.pradar.link.model.LinkNodeModel;
import io.shulie.surge.data.deploy.pradar.parser.MiddlewareType;
import io.shulie.surge.data.deploy.pradar.parser.PradarLogType;
import io.shulie.surge.data.deploy.pradar.parser.RpcBasedParser;
import io.shulie.surge.data.deploy.pradar.parser.unknown.UnknownNodeRpcBasedParser;
import io.shulie.surge.data.deploy.pradar.parser.utils.Md5Utils;
import io.shulie.surge.data.runtime.common.remote.DefaultValue;
import io.shulie.surge.data.runtime.common.remote.Remote;
import io.shulie.surge.data.runtime.common.utils.ApiProcessor;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: xingchen
 * @ClassName: UnKnowNodeSchedule
 * @Package: io.shulie.amdb.scheduled
 * @Date: 2021/1/2010:08
 * @Description:
 */
public class LinkUnKnowNodeProcessor extends AbstractProcessor {
    private static Logger logger = LoggerFactory.getLogger(LinkUnKnowNodeProcessor.class);
    private static final String UNKNOW_APP = "UNKNOWN";
    @Inject
    private LinkProcessor linkProcessor;
    @Inject
    private MysqlSupport mysqlSupport;
    public AbstractLinkCache linkCache;
    @Inject
    private TaskManager<String, String> taskManager;

    /**
     * 是否开启未知节点扫描功能
     */
    @Inject
    @DefaultValue("true")
    @Named("/pradar/config/rt/unknowNodeProcessDisable")
    private Remote<Boolean> unknowNodeProcessDisable;

    /**
     * 间隔时间,默认10分钟
     */
    @Inject
    @DefaultValue("600")
    @Named("/pradar/config/rt/unknowNodeProcess/delayTime")
    private Remote<Long> intervalTime;

    /**
     * 处理未知节点
     */
    public void processUnKnowNodeCommon(String linkId, Map<String, Object> linkConfig) {
        logger.info("processUnKnowNodeCommon{},{}", linkId, linkConfig);
        try {
            linkProcessor.setDataSourceType(this.getDataSourceType());
            List<RpcBased> rpcBasedList = linkProcessor.getTraceLog(linkConfig,
                    TraceLogQueryScopeEnum.build(5));
            if (CollectionUtils.isEmpty(rpcBasedList)) {
                logger.warn("processUnKnowNodeCommon is empty{},{}", linkId, linkConfig);
                return;
            }
            // 按traceId分组
            Map<String, List<RpcBased>> rpcBasedMap = rpcBasedList.stream().collect(
                    Collectors.groupingBy(rpcBased -> rpcBased.getTraceId()));
            rpcBasedMap.entrySet().forEach(entry -> {
                List<RpcBased> rpcBaseds = entry.getValue();
                // 只需要http客户端和dubbo客户端
                List<RpcBased> clientList = rpcBaseds.stream().filter(rpcBased ->
                        isHttpClient(rpcBased) || isRpcClient(rpcBased)).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(clientList)) {
                    return;
                }
                clientList.stream().forEach(client -> {
                    String appName = client.getAppName();
                    String service = formatService(client.getServiceName(), client.getRpcType());
                    String method = MethodHandler.convert(client.getMethodName());
                    String clientKey = appName + "|" + service + "|" + method;
                    boolean exist = false;
                    for (int i = 0; i < rpcBaseds.size(); i++) {
                        RpcBased allBased = rpcBaseds.get(i);
                        if (allBased.getLogType() == PradarLogType.LOG_TYPE_RPC_CLIENT) {
                            continue;
                        }
                        // 拼接服务端
                        String serverUpAppName = allBased.getUpAppName();
                        String serverService = allBased.getServiceName();
                        String serverMethod = MethodHandler.convert(allBased.getMethodName());
                        String serverKey = serverUpAppName + "|" + serverService + "|" + serverMethod;
                        if (clientKey.equals(serverKey)) {
                            exist = true;
                        }
                    }
                    // 不存在，则标记未知应用
                    if (!exist) {
                        Pair<LinkNodeModel, LinkEdgeModel> pair = analyseLinkCommon(linkId, client);
                        processLinkExt(pair);
                    }
                });
            });
            logger.info("processUnKnowNodeCommon is ok {},{}", linkId, linkConfig);
        } catch (Throwable e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * 写入点边信息
     *
     * @param pair
     */
    private void processLinkExt(Pair<LinkNodeModel, LinkEdgeModel> pair) {
        try {
            if (pair == null) {
                return;
            }
            mysqlSupport.update(LinkSqlContants.LINK_NODE_INSERT_SQL, new Object[]{pair.getLeft().getLinkId(), pair.getLeft().getAppName(), pair.getLeft().getTraceAppName(), pair.getLeft().getMiddlewareName(), pair.getLeft().getExtend(), pair.getLeft().getAppId()});

            mysqlSupport.update(LinkSqlContants.LINK_EDGE_INSERT_SQL, new Object[]{pair.getRight().getLinkId(), pair.getRight().getService(), pair.getRight().getMethod(), pair.getRight().getExtend(), pair.getRight().getAppName(), pair.getRight().getTraceAppName(), pair.getRight().getServerAppName(),
                    pair.getRight().getRpcType(), pair.getRight().getLogType(), pair.getRight().getMiddlewareName(), pair.getRight().getEntranceId(), pair.getRight().getFromAppId(), pair.getRight().getToAppId(), pair.getRight().getEdgeId()});
        } catch (Throwable e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }

    }

    private String formatService(String serviceName, int rpcType) {
        if (rpcType == MiddlewareType.TYPE_WEB_SERVER) {
            return ApiProcessor.parsePath(serviceName);
        }
        return serviceName;
    }

    private Pair<LinkNodeModel, LinkEdgeModel> analyseLinkCommon(String linkId, RpcBased rpcBased) {
        RpcBasedParser rpcBasedParser = new UnknownNodeRpcBasedParser();
        String edgeId = rpcBasedParser.edgeId("", rpcBased);
        Map<String, Object> edgeTags = rpcBasedParser.edgeTags("", rpcBased);
        String fromAppId = rpcBasedParser.fromAppId(linkId, rpcBased);

        Map<String, Object> toAppTags = rpcBasedParser.toAppTags(linkId, rpcBased);
        toAppTags.put("appName", UNKNOW_APP);
        toAppTags.put("middlewareName", rpcBased.getMiddlewareName());
        String toAppId = mapToKey(toAppTags);
        toAppTags.put("appId", toAppId);

        edgeTags.put("edgeId", edgeId);
        edgeTags.put("fromAppId", fromAppId);
        edgeTags.put("toAppId", toAppId);
        edgeTags.put("traceAppName", "");
        edgeTags.put("linkId", linkId);

        LinkNodeModel toNodeModel = LinkNodeModel.parseFromDataMap(toAppTags);
        if (StringUtils.isNotBlank((String) toAppTags.get("middlewareName"))) {
            Map<String, Object> toNodeExtendInfo = new HashMap();
            toNodeExtendInfo.put("ip", rpcBased.getRemoteIp());
            toNodeExtendInfo.put("port", rpcBased.getPort());
            toNodeModel.setExtend(JSON.toJSONString(toNodeExtendInfo));
        }
        return Pair.of(toNodeModel, LinkEdgeModel.parseFromDataMap(edgeTags));
    }

    protected String mapToKey(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        for (Object object : map.values()) {
            sb.append(object).append('|');
        }
        sb.deleteCharAt(sb.length() - 1);
        return Md5Utils.md5(sb.toString());
    }

    @Override
    public void share(List<String> taskIds, String currentTaskId) {
        if (!unknowNodeProcessDisable.get()) {
            return;
        }
        if (!isHandler(intervalTime.get())) {
            return;
        }
        Map<String, Map<String, Object>> linkConfig = linkCache.getLinkConfig();
        if (linkConfig == null || linkConfig.isEmpty()) {
            return;
        }
        Set<String> linkIdSet = linkConfig.keySet();
        Map<String, List<String>> avgMap = taskManager.allotOfAverage(taskIds, new ArrayList<>(linkIdSet));
        List<String> avgList = avgMap.get(currentTaskId);
        if (CollectionUtils.isNotEmpty(avgList)) {
            for (int i = 0; i < avgList.size(); i++) {
                String linkId = avgList.get(i);
                Map<String, Object> link = linkConfig.get(avgList.get(i));
                processUnKnowNodeCommon(linkId, link);
            }
        }
    }

    @Override
    public void share(int taskId) {
        if (!unknowNodeProcessDisable.get()) {
            return;
        }
        if (!isHandler(intervalTime.get())) {
            return;
        }
        Map<String, Map<String, Object>> linkConfig = linkCache.getLinkConfig();
        List<Map.Entry<String, Map<String, Object>>> linkList = Lists.newArrayList(linkConfig.entrySet());
        for (int i = 0; i < linkList.size(); i++) {
            if (i % taskId == 0) {
                Map.Entry<String, Map<String, Object>> link = linkList.get(i);
                processUnKnowNodeCommon(link.getKey(), link.getValue());
            }
        }
    }

    @Override
    public void share() {
        if (!unknowNodeProcessDisable.get()) {
            return;
        }
        if (!isHandler(intervalTime.get())) {
            return;
        }
        // 读取所有的配置
        Map<String, Map<String, Object>> linkConfig = linkCache.getLinkConfig();
        List<Map.Entry<String, Map<String, Object>>> linkList = Lists.newArrayList(linkConfig.entrySet());
        for (int i = 0; i < linkList.size(); i++) {
            Map.Entry<String, Map<String, Object>> link = linkList.get(i);
            processUnKnowNodeCommon(link.getKey(), link.getValue());
        }
    }

    @Override
    public void init() {
    }

    public void init(String dataSourceType) {
        this.setDataSourceType(dataSourceType);
    }

    public void setLinkCache(AbstractLinkCache linkCache) {
        this.linkCache = linkCache;
    }

    public boolean isHttpClient(RpcBased rpcBased) {
        return rpcBased.getLogType() == PradarLogType.LOG_TYPE_RPC_CLIENT
                && rpcBased.getRpcType() == MiddlewareType.TYPE_WEB_SERVER
                && !rpcBased.getMiddlewareName().toUpperCase().contains("FEIGN");
    }

    public boolean isRpcClient(RpcBased rpcBased) {
        return rpcBased.getLogType() == PradarLogType.LOG_TYPE_RPC_CLIENT
                && rpcBased.getRpcType() == MiddlewareType.TYPE_RPC
                && !rpcBased.getServiceName().toLowerCase().contains("oss")
                && !rpcBased.getMiddlewareName().toUpperCase().contains("FEIGN");
    }
}
