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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.pradar.log.rule.RuleFactory;
import io.shulie.pradar.log.rule.RuleFactory.Rule;
import io.shulie.surge.data.common.aggregation.AggregateSlot;
import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.deploy.pradar.agg.E2ETraceMetricsAggarator;
import io.shulie.surge.data.deploy.pradar.common.AppConfigUtil;
import io.shulie.surge.data.deploy.pradar.common.E2ENodeCache;
import io.shulie.surge.data.deploy.pradar.common.PradarRtConstant;
import io.shulie.surge.data.deploy.pradar.parser.RpcBasedParser;
import io.shulie.surge.data.deploy.pradar.parser.RpcBasedParserFactory;
import io.shulie.surge.data.deploy.pradar.parser.utils.Md5Utils;
import io.shulie.surge.data.runtime.common.remote.DefaultValue;
import io.shulie.surge.data.runtime.common.remote.Remote;
import io.shulie.surge.data.runtime.digest.DataDigester;
import io.shulie.surge.data.runtime.digest.DigestContext;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Singleton
public class E2EAssertionDigester implements DataDigester<RpcBased> {

    private static Logger logger = LoggerFactory.getLogger(E2EAssertionDigester.class);

    private static final String METRICS_ID = "tro_pradar";

    @Inject
    @DefaultValue("true")
    @Named("/pradar/config/rt/e2eMetricsDisable")
    private Remote<Boolean> e2eMetricsDisable;

    @Inject
    private MysqlSupport mysqlSupport;

    @Inject
    private AppConfigUtil appConfigUtil;

    private E2ENodeCache e2eNodeCache = new E2ENodeCache();

    @Inject
    private E2ETraceMetricsAggarator e2eTraceMetricsAggaration;

    @Override
    public void digest(DigestContext<RpcBased> context) {
        if (e2eMetricsDisable.get()) {
            return;
        }
        RpcBased rpcBased = context.getContent();
        RpcBasedParser rpcBasedParser = RpcBasedParserFactory.getInstance(rpcBased.getLogType(), rpcBased.getRpcType());
        if (rpcBasedParser == null) {
            return;
        }
        String parsedAppName = StringUtils.defaultString(rpcBasedParser.appNameParse(rpcBased), "");
        String parsedServiceName = StringUtils.defaultString(rpcBasedParser.serviceParse(rpcBased), "");
        String parsedMethod = StringUtils.defaultString(rpcBasedParser.methodParse(rpcBased), "");
        String rpcType = rpcBased.getRpcType() + "";
        String nodeId = getNodeId(parsedAppName, parsedServiceName, parsedMethod, rpcType);
        if (!e2eNodeCache.getE2eNodeConfig().containsKey(nodeId)) {
//            logger.info("e2e config donot match:{},{},{},{},{}", parsedAppName, parsedServiceName, parsedMethod, rpcType, nodeId);
            return;
        }
        // 断言列表
        Map<String, Rule> nodeAssertListMap = e2eNodeCache.getE2eAssertConfig().get(nodeId);
        long timeStamp = rpcBased.getLogTime();
        AggregateSlot<Metric, CallStat> slot = e2eTraceMetricsAggaration.getSlotByTimestamp(timeStamp);
        /**
         * 汇总信息实现：单独存储一张表，不区分断言类型，断言不通过的要算做失败
         * 每个断言命中次数：遍历断言，记录每个断言的命中情况，命中则失败次数+1，没命中则成功次数+1
         * 断言的单独存储一张表，断言的仅记录命中的失败记录
         * 一条数据过来需要调用多次slot.addToSlot方法
         * 统计每个断言命中情况的时候就通过查询 group by exceptionType
         * exceptionType 公有三种类型：exception、resultCode、assertCode
         * 因为状态码会有很多，不可能每个状态码写一个断言，所以这种的最终的exceptionType应该要取resultCode
         */
        //三种异常类型：exception、resultCode、assertCode
        List<String> exceptionTypeList = new ArrayList<>();
        if (!"00".equals(rpcBased.getResultCode()) && !"200".equals(rpcBased.getResultCode())) {
            if (StringUtils.isNotBlank(rpcBased.getResponse()) && rpcBased.getResponse().split(":")[0].endsWith(
                    "Exception")) {
                exceptionTypeList.add("exception-" + rpcBased.getResponse().split(":")[0]);
            }
            exceptionTypeList.add("resultCode-" + rpcBased.getResultCode());
        }
        try {
            // 断言判定
            if (MapUtils.isNotEmpty(nodeAssertListMap)) {
                for (String assertCode : nodeAssertListMap.keySet()) {
                    Rule rule = nodeAssertListMap.get(assertCode);
                    try {
                        if (Boolean.parseBoolean(String
                                .valueOf(
                                        RuleFactory.INSTANCE.eval("node", rpcBased, rule.getRuleType(),
                                                rule.getCondition().replaceAll("@node", "node"))))) {
                            exceptionTypeList.add("assertCode-" + assertCode);
                        }
                    } catch (Throwable e) {
                        logger.error("rule " + rule.toString());
                    }

                }
            }
            // 是否压测流量
            String clusterTest = rpcBased.isClusterTest() ? "1" : "0";
            Integer simpling = appConfigUtil.getAppSamplingByAppName(rpcBased.getAppName());
            // 写入断言指标
            for (String exceptionType : exceptionTypeList) {
                long successCount = 0;
                long errorCount = 1;
                String[] tags = new String[]{nodeId, parsedAppName, parsedServiceName, parsedMethod, rpcType,
                        clusterTest,
                        exceptionType};
                CallStat callStat = new CallStat(
                        simpling * 1L, simpling * successCount, simpling * rpcBased.getCost(),
                        simpling * errorCount, simpling);
                slot.addToSlot(Metric.of(PradarRtConstant.E2E_ASSERT_METRICS_ID_TRACE, tags, "", new String[]{}),
                        callStat);
            }
            // 写入统计指标
            long successCount = exceptionTypeList.size() > 0 ? 0 : 1;
            long errorCount = 1 - successCount;
            String[] tags = new String[]{nodeId, parsedAppName, parsedServiceName, parsedMethod, rpcType, clusterTest,
                    "-1"};
            CallStat callStat = new CallStat(
                    simpling * 1L, simpling * successCount, simpling * rpcBased.getCost(),
                    simpling * errorCount, simpling);
            slot.addToSlot(Metric.of(PradarRtConstant.E2E_METRICS_ID_TRACE, tags, "", new String[]{}), callStat);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public int threadCount() {
        return 1;
    }

    @Override
    public void stop() throws Exception {
        if (e2eTraceMetricsAggaration != null) {
            e2eTraceMetricsAggaration.stop();
        }
    }

    private String getNodeId(String parsedAppName, String parsedServiceName, String parsedMethod, String rpcType) {
        return Md5Utils.md5(parsedAppName + "|" + parsedServiceName + "|" + parsedMethod + "|" + rpcType);
    }

    public void init() {
        e2eNodeCache.autoRefresh(mysqlSupport);
        RuleFactory.INSTANCE.regsiterVariant(new Class[]{RpcBased.class}, new String[]{"node"});
        logger.info("e2eNodeCache:{}", e2eNodeCache);
    }
}
