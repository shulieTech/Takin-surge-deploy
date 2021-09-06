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

import com.google.common.collect.Maps;
import io.shulie.pradar.log.rule.RuleFactory.Rule;
import io.shulie.pradar.log.rule.RuleFactory.Rule.RuleType;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class E2ENodeCache {

    private static final Logger logger = LoggerFactory.getLogger(E2ENodeCache.class);

    private static final String E2E_CONFIG_TABLENAME = "t_amdb_pradar_e2e_config";
    private static final String E2E_ASSERT_CONFIG_TABLENAME = "t_amdb_pradar_e2e_assert_config";

    private LinkedHashMap<String, Map<String, Object>> e2eConfig = Maps.newLinkedHashMap();
    private LinkedHashMap<String, Map<String, Rule>> e2eAssertConfig = Maps.newLinkedHashMap();

    /**
     * 自动刷新hbase数据
     */
    public void autoRefresh(MysqlSupport mysqlSupport) {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                refreshE2eConfig(mysqlSupport);
                refreshE2eAssertConfig(mysqlSupport);
            }
        }, 10, 5, TimeUnit.SECONDS);
        refreshE2eConfig(mysqlSupport);
        refreshE2eAssertConfig(mysqlSupport);
    }

    private void refreshE2eConfig(MysqlSupport mysqlSupport) {
        try {
            List<Map<String, Object>> e2eConfigModels = mysqlSupport.queryForList(
                    "select * from " + E2E_CONFIG_TABLENAME + " limit 9999");
            Set<String> newNodeIdList = new HashSet<>();
            for (Map<String, Object> e2eConf : e2eConfigModels) {
                Map<String, Object> e2eConfMap = Maps.newHashMap();
                for (String key : e2eConf.keySet()) {
                    e2eConfMap.put(lineToHump(key), e2eConf.get(key));
                }
                String nodeId = (String) e2eConfMap.get("nodeId");
                doE2eNodeConfig(nodeId, e2eConfMap);
                newNodeIdList.add(nodeId);
            }
            //老的有，新的没有，表示已被删除
            Set<String> allNodeIdList = e2eConfig.keySet();
            List<String> deletedNodeIdList = (List<String>) CollectionUtils.subtract(allNodeIdList, newNodeIdList);
            if (CollectionUtils.isNotEmpty(deletedNodeIdList)) {
                for (String nodeId : deletedNodeIdList) {
                    e2eConfig.remove(nodeId);
                }
            }
        } catch (Exception e) {
            logger.error("Query linkId configuration faild.", e);
        }
    }

    /**
     * 刷新断言配置
     *
     * @param mysqlSupport
     */
    private void refreshE2eAssertConfig(MysqlSupport mysqlSupport) {
        try {
            List<Map<String, Object>> e2eConfigModels = mysqlSupport.queryForList(
                    "select * from " + E2E_ASSERT_CONFIG_TABLENAME + " limit 9999");
            Set<String> newNodeIdAssertCodeList = new HashSet<>();
            for (Map<String, Object> e2eConf : e2eConfigModels) {
                Map<String, Object> e2eConfMap = Maps.newHashMap();
                for (String key : e2eConf.keySet()) {
                    e2eConfMap.put(lineToHump(key), e2eConf.get(key));
                }
                String nodeId = (String) e2eConfMap.get("nodeId");
                String assertCode = (String) e2eConfMap.get("assertCode");
                doE2eAssertConfig(nodeId, assertCode, e2eConfMap);
                newNodeIdAssertCodeList.add(nodeId + "#" + assertCode);
            }
            //老的有，新的没有，表示已被删除
            Set<String> allNodeIdList = new HashSet<>();
            e2eAssertConfig.forEach((k, v) -> {
                String nodeId = k;
                if (MapUtils.isNotEmpty(v)) {
                    Set<String> assertCodeSet = v.keySet();
                    assertCodeSet.forEach(assertCode -> allNodeIdList.add(nodeId + "#" + assertCode));
                }
            });
            List<String> deletedNodeIdList = (List<String>) CollectionUtils.subtract(allNodeIdList,
                    newNodeIdAssertCodeList);
            if (CollectionUtils.isNotEmpty(deletedNodeIdList)) {
                for (String nodeIdAndAssertCode : deletedNodeIdList) {
                    String[] nodeIdAndAssertCodes = nodeIdAndAssertCode.split("#");
                    String nodeId = nodeIdAndAssertCodes[0];
                    String assertCode = nodeIdAndAssertCodes[1];
                    e2eAssertConfig.get(nodeId).remove(assertCode);
                    if (MapUtils.isEmpty(e2eAssertConfig.get(nodeId))) {
                        e2eAssertConfig.remove(nodeId);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Query linkId configuration faild.", e);
        }
    }

    private void doE2eNodeConfig(String nodeId, Map<String, Object> e2eConf) {
        if (e2eConf == null || e2eConf.isEmpty()) {
            return;
        }
        e2eConfig.put(nodeId, e2eConf);
    }

    private void doE2eAssertConfig(String nodeId, String assertCode, Map<String, Object> e2eConf) {
        if (e2eConf == null || e2eConf.isEmpty()) {
            return;
        }
        if (!e2eAssertConfig.containsKey(nodeId)) {
            e2eAssertConfig.put(nodeId, new HashMap<>());
        }
        e2eAssertConfig.get(nodeId).put(assertCode, getRuleByAssertConf(e2eConf));
    }

    public LinkedHashMap<String, Map<String, Object>> getE2eNodeConfig() {
        return e2eConfig;
    }

    public LinkedHashMap<String, Map<String, Rule>> getE2eAssertConfig() {
        return e2eAssertConfig;
    }

    private static Pattern linePattern = Pattern.compile("_(\\w)");

    /**
     * 下划线转驼峰
     */
    public static String lineToHump(String str) {
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


    /**
     * 根据配置生成规则对象
     *
     * @param e2eAssertConf
     * @return
     */
    public Rule getRuleByAssertConf(Map<String, Object> e2eAssertConf) {
        String code = (String) e2eAssertConf.get("assertCode");
        String condition = (String) e2eAssertConf.get("assertCondition");
        String result = (String) e2eAssertConf.get("result");
        Rule.RuleType ruleType = RuleType.tiny;
        return new Rule(code, condition, result, ruleType);
    }
}
