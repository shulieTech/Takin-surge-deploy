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
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.shulie.surge.data.deploy.pradar.link.AbstractLinkCache;
import io.shulie.surge.data.deploy.pradar.link.LinkSqlContants;
import io.shulie.surge.data.deploy.pradar.link.TaskManager;
import io.shulie.surge.data.deploy.pradar.link.model.LinkEdgeModel;
import io.shulie.surge.data.deploy.pradar.link.model.LinkNodeModel;
import io.shulie.surge.data.deploy.pradar.parser.MiddlewareType;
import io.shulie.surge.data.deploy.pradar.parser.PradarLogType;
import io.shulie.surge.data.deploy.pradar.parser.utils.Md5Utils;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import static io.shulie.surge.data.deploy.pradar.link.LinkSqlContants.LINK_EDGE_MQ_SELECT_SQL;
import static io.shulie.surge.data.deploy.pradar.link.LinkSqlContants.UNKNOW_APP;

/**
 * @Author: xingchen
 * @ClassName: UnKnowNodeSchedule
 * @Package: io.shulie.amdb.scheduled
 * @Date: 2021/1/2010:08
 * @Description:
 */
public class LinkUnKnowMQProcessor implements Processor {

    private static Logger logger = LoggerFactory.getLogger(LinkUnKnowMQProcessor.class);

    @Inject
    private MysqlSupport mysqlSupport;

    @Autowired
    public AbstractLinkCache linkCache = new AbstractLinkCache() {
        @Override
        public void save(String linkId, LinkedBlockingQueue<String> linkedBlockingQueue) {
            return;
        }
    };

    @Inject
    private TaskManager<String, String> taskManager;

    /**
     * 处理Mq未知
     */
    public void processUnKnownNodeMq(String linkId, Map<String, Object> linkConfig) {
        try {
            logger.info("processUnKnownNodeMq {},{}", linkId, linkConfig);
            List<Map<String, Object>> edgeList = mysqlSupport.queryForList(LINK_EDGE_MQ_SELECT_SQL + "'" + linkId + "'");

            Map<String, Map<String, Object>> mqMap = Maps.newHashMap();
            for (Map<String, Object> edge : edgeList) {
                if (String.valueOf(PradarLogType.LOG_TYPE_RPC_CLIENT).equals(edge.get("log_type"))) {
                    mqMap.put(String.valueOf(edge.get("to_app_id")), edge);
                }
            }

            for (Map<String, Object> edge : edgeList) {
                if (String.valueOf(PradarLogType.LOG_TYPE_RPC_SERVER).equals(edge.get("log_type"))) {
                    String fromAppId = String.valueOf(edge.get("from_app_id"));
                    mqMap.remove(fromAppId);
                }
            }
            for (Map.Entry<String, Map<String, Object>> entry : mqMap.entrySet()) {
                Pair<LinkNodeModel, LinkEdgeModel> pair = analyseLinkMq(linkId, entry.getValue());
                if (pair == null) {
                    logger.warn("processUnKnownNodeMq is empty {},{}", linkId, linkConfig);
                    return;
                }
                processLinkExt(pair);
                logger.info("processUnKnownNodeMq save is ok size {},{}", linkId, entry.getValue().size());
            }

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
            mysqlSupport.update(LinkSqlContants.LINK_NODE_INSERT_SQL, new Object[]{pair.getLeft().getLinkId(), pair.getLeft().getAppName(), pair.getLeft().getTraceAppName(), pair.getLeft().getMiddlewareName(), pair.getLeft().getExtend(), pair.getLeft().getAppId()});

            mysqlSupport.update(LinkSqlContants.LINK_EDGE_INSERT_SQL, new Object[]{pair.getRight().getLinkId(), pair.getRight().getService(), pair.getRight().getMethod(), pair.getRight().getExtend(), pair.getRight().getAppName(), pair.getRight().getTraceAppName(), pair.getRight().getServerAppName(),
                    pair.getRight().getRpcType(), pair.getRight().getLogType(), pair.getRight().getMiddlewareName(), pair.getRight().getEntranceId(), pair.getRight().getFromAppId(), pair.getRight().getToAppId(), pair.getRight().getEdgeId()});
        } catch (Throwable e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }

    }


    private Pair<LinkNodeModel, LinkEdgeModel> analyseLinkMq(String linkId, Map<String, Object> mqEdge) {
        //构建一个虚拟节点和边
        LinkNodeModel nodeModel = new LinkNodeModel();
        Map<String, Object> nodeMap = new HashMap<>();
        nodeMap.put("linkId", linkId);
        nodeMap.put("appName", UNKNOW_APP);
        nodeMap.put("middlewareName", "");
        nodeMap.put("extend", mqEdge.get("extend"));
        String toAppId = Md5Utils.md5(mapToKey(nodeMap));

        nodeModel.setLinkId(linkId);
        nodeModel.setAppId(toAppId);
        nodeModel.setTraceAppName("");
        nodeModel.setAppName(UNKNOW_APP);
        nodeModel.setMiddlewareName("");
        nodeModel.setExtend(String.valueOf(mqEdge.get("extend")));

        //构建一个虚拟节点和边
        LinkEdgeModel edgeModel = new LinkEdgeModel();
        edgeModel.setAppName(UNKNOW_APP);
        edgeModel.setExtend(String.valueOf(mqEdge.get("extend")));
        edgeModel.setLogType(String.valueOf(PradarLogType.LOG_TYPE_RPC_SERVER));
        edgeModel.setRpcType(String.valueOf(MiddlewareType.TYPE_MQ));
        edgeModel.setService(String.valueOf(mqEdge.get("service")));
        edgeModel.setMethod(String.valueOf(mqEdge.get("method")));
        edgeModel.setLinkId(linkId);
        edgeModel.setServerAppName(String.valueOf(mqEdge.get("app_name")));
        edgeModel.setTraceAppName(String.valueOf(mqEdge.get("trace_app_name")));
        edgeModel.setMiddlewareName(String.valueOf(mqEdge.get("middleware_name")));
        edgeModel.setFromAppId(String.valueOf(mqEdge.get("to_app_id")));
        edgeModel.setToAppId(toAppId);

        Map<String, Object> tags = Maps.newLinkedHashMap();
        tags.put("linkId", linkId);
        tags.put("service", edgeModel.getService());
        tags.put("method", edgeModel.getMethod());
        tags.put("extend", edgeModel.getExtend());
        tags.put("appName", UNKNOW_APP);
        tags.put("traceAppName", "");
        tags.put("serverAppName", edgeModel.getServerAppName());
        tags.put("rpcType", edgeModel.getRpcType());
        tags.put("logType", edgeModel.getLogType());
        tags.put("middlewareName", edgeModel.getMiddlewareName());
        tags.put("entranceId", edgeModel.getEntranceId());
        edgeModel.setEdgeId(Md5Utils.md5(mapToKey(tags)));
        return Pair.of(nodeModel, edgeModel);
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
                processUnKnownNodeMq(linkId, link);
            }
        }
    }

    @Override
    public void share(int taskId) {
        if (taskId == -1) {
            return;
        }
        Map<String, Map<String, Object>> linkConfig = linkCache.getLinkConfig();
        List<Map.Entry<String, Map<String, Object>>> linkList = Lists.newArrayList(linkConfig.entrySet());
        for (int i = 0; i < linkList.size(); i++) {
            if (i % taskId == 0) {
                Map.Entry<String, Map<String, Object>> link = linkList.get(i);
                processUnKnownNodeMq(link.getKey(), link.getValue());
            }
        }
    }

    @Override
    public void share() {
        Map<String, Map<String, Object>> linkConfig = linkCache.getLinkConfig();
        List<Map.Entry<String, Map<String, Object>>> linkList = Lists.newArrayList(linkConfig.entrySet());
        for (int i = 0; i < linkList.size(); i++) {
            Map.Entry<String, Map<String, Object>> link = linkList.get(i);
            processUnKnownNodeMq(link.getKey(), link.getValue());
        }
    }

    @Override
    public void init() {
    }

    public void setLinkCache(AbstractLinkCache linkCache) {
        this.linkCache = linkCache;
    }
}
