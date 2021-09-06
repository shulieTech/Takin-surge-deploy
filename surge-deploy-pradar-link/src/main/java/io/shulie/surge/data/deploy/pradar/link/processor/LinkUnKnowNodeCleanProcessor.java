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
import io.shulie.surge.data.deploy.pradar.link.AbstractLinkCache;
import io.shulie.surge.data.deploy.pradar.link.TaskManager;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.shulie.surge.data.deploy.pradar.link.LinkSqlContants.*;

/**
 * @Author: xingchen
 * @ClassName: UnKnowNodeSchedule
 * @Package: io.shulie.amdb.scheduled
 * @Date: 2021/1/2010:08
 * @Description:
 */
public class LinkUnKnowNodeCleanProcessor extends AbstractProcessor {
    private static Logger logger = LoggerFactory.getLogger(LinkUnKnowNodeCleanProcessor.class);
    @Inject
    private MysqlSupport mysqlSupport;

    public AbstractLinkCache linkCache;

    @Inject
    private TaskManager<String, String> taskManager;

    public void clearUnknownNode(String linkId) {
        logger.info("clearUnknownNode {}", linkId);

        String linkNodeDeleteSql = LINK_NODE_DELETE_SQL;
        String linkEdgeDeleteSql = LINK_EDGE_DELETE_SQL;
        String linkNodeSelectSql = LINK_NODE_SELECT_SQL;
        String linkEdgeSelectSql = LINK_EDGE_SELECT_SQL;

        if (StringUtils.isNotBlank(linkId)) {
            linkNodeSelectSql += " and link_id = \'" + linkId + "\'";
            linkEdgeSelectSql = String.format(linkEdgeSelectSql, linkId);
            // 处理点
            List<Map<String, Object>> linkNodes = mysqlSupport.queryForList(linkNodeSelectSql);
            // 处理边信息
            List<Map<String, Object>> linkEdges = mysqlSupport.queryForList(linkEdgeSelectSql);

            if (CollectionUtils.isNotEmpty(linkNodes)) {
                StringBuffer nodeId = new StringBuffer();
                for (int i = 0; i < linkNodes.size(); i++) {
                    nodeId = nodeId.append("\'").append(linkNodes.get(i).get("id")).append("\'").append(",");
                }
                nodeId = nodeId.deleteCharAt(nodeId.length() - 1);
                linkNodeDeleteSql += " id in (" + nodeId.toString() + ")";

                mysqlSupport.update(linkNodeDeleteSql, new Object[]{});
            }


            if (CollectionUtils.isNotEmpty(linkEdges)) {
                StringBuffer edgeId = new StringBuffer();
                for (int i = 0; i < linkEdges.size(); i++) {
                    edgeId = edgeId.append("\'").append(linkEdges.get(i).get("id")).append("\'").append(",");
                }
                edgeId = edgeId.deleteCharAt(edgeId.length() - 1);
                linkEdgeDeleteSql += " id in (" + edgeId.toString() + ")";

                mysqlSupport.update(linkEdgeDeleteSql, new Object[]{});
            }
        }
        logger.info("clearUnknownNode is ok {}", linkId);

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
                clearUnknownNode(linkId);
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
                clearUnknownNode(link.getKey());
            }
        }
    }

    @Override
    public void share() {
        clearUnknownNode("");
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
}
