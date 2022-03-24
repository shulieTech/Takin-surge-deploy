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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.pamirs.pradar.log.parser.constant.TenantConstants;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.pradar.log.rule.RuleFactory;
import io.shulie.pradar.log.rule.RuleFactory.Rule;
import io.shulie.surge.data.common.aggregation.AggregateSlot;
import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.deploy.pradar.agg.E2ETraceMetricsAggarator;
import io.shulie.surge.data.deploy.pradar.agg.TraceMetrics2Aggarator;
import io.shulie.surge.data.deploy.pradar.agg.TraceMetricsAggarator;
import io.shulie.surge.data.deploy.pradar.common.*;
import io.shulie.surge.data.deploy.pradar.parser.MiddlewareType;
import io.shulie.surge.data.deploy.pradar.parser.PradarLogType;
import io.shulie.surge.data.deploy.pradar.parser.RpcBasedParser;
import io.shulie.surge.data.deploy.pradar.parser.RpcBasedParserFactory;
import io.shulie.surge.data.deploy.pradar.parser.utils.Md5Utils;
import io.shulie.surge.data.runtime.common.remote.DefaultValue;
import io.shulie.surge.data.runtime.common.remote.Remote;
import io.shulie.surge.data.runtime.common.utils.ApiProcessor;
import io.shulie.surge.data.runtime.digest.DataDigester;
import io.shulie.surge.data.runtime.digest.DigestContext;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import io.shulie.surge.deploy.pradar.constants.TenantEnvConstants;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Singleton
public class TraceMetrics2Diggester implements DataDigester<RpcBased> {
    private static Logger logger = LoggerFactory.getLogger(TraceMetrics2Diggester.class);
    @Inject
    private TraceMetrics2Aggarator traceMetrics2Aggarator;

    @Inject
    private E2ETraceMetricsAggarator e2eTraceMetricsAggarator;

    @Inject
    @DefaultValue("false")
    @Named("/pradar/config/rt/traceMetricsDisable")
    protected Remote<Boolean> traceMetricsDisable;

    @Inject
    @DefaultValue("")
    @Named("/pradar/config/rt/traceMetricsTenantConfig")
    protected Remote<String> traceMetricsTenantConfig;

    @Inject
    protected AppConfigUtil appConfigUtil;

    @Inject
    private MysqlSupport mysqlSupport;

    protected E2ENodeCache e2eNodeCache = new E2ENodeCache();


    private boolean conditionWithHostIp(){
        return true;
    }

    @Override
    public void digest(DigestContext<RpcBased> context) {
        if (traceMetricsDisable.get()) {
            return;
        }

        RpcBased rpcBased = context.getContent();
        //客户端rpc日志不计算指标,只计算服务端日志,和链路拓扑图保持一致
        if (PradarLogType.LOG_TYPE_FLOW_ENGINE == rpcBased.getLogType() || (PradarLogType.LOG_TYPE_RPC_CLIENT == rpcBased.getLogType() && MiddlewareType.TYPE_RPC == rpcBased.getRpcType())) {
            return;
        }
        RpcBasedParser rpcBasedParser = RpcBasedParserFactory.getInstance(rpcBased.getLogType(), rpcBased.getRpcType());
        if (rpcBasedParser == null) {
            return;
        }

        //对于1.6以及之前的老版本探针,没有租户相关字段,根据应用名称获取租户配置,没有设默认值
        if (StringUtils.isBlank(rpcBased.getUserAppKey()) || TenantConstants.DEFAULT_USER_APP_KEY.equals(rpcBased.getUserAppKey())) {
            rpcBased.setUserAppKey(ApiProcessor.getTenantConfigByAppName(rpcBased.getAppName()).get("tenantAppKey"));
        }
        if (StringUtils.isBlank(rpcBased.getEnvCode())) {
            rpcBased.setEnvCode(ApiProcessor.getTenantConfigByAppName(rpcBased.getAppName()).get("envCode"));
        }

        // 生成唯一边Id ,同步zk集合，判断此流量是否要统计
        String edgeId = rpcBasedParser.edgeId("", rpcBased);
        Map<String, Object> eagleTags = rpcBasedParser.edgeTags("", rpcBased);

        //获取是否压测流量
        String clusterTest = String.valueOf(rpcBased.isClusterTest());
        //解析关键指标用于断言指标tag
        String parsedAppName = StringUtils.defaultString(rpcBasedParser.appNameParse(rpcBased), "");
        String parsedServiceName = StringUtils.defaultString(rpcBasedParser.serviceParse(rpcBased), "");
        String parsedMethod = StringUtils.defaultString(rpcBasedParser.methodParse(rpcBased), "");
        String rpcType = rpcBased.getRpcType() + "";
        String nodeId = getNodeId(parsedAppName, parsedServiceName, parsedMethod, rpcType);
        //todo 验证取采样率是否兼容租户
        String userAppKey = rpcBased.getUserAppKey();
        String envCode = rpcBased.getEnvCode();
        //获取每个应用的采样率
        int sampling = 1;
        //对于调试流量,agent不采样,采样率默认为1
        if (!rpcBased.getFlags().isDebugTest()) {
            sampling = appConfigUtil.getAppSamplingByAppName(userAppKey, envCode, rpcBased.getAppName(), clusterTest);
        }

        // 断言列表,兼容老的nodeId查询
        Map<String, Rule> nodeAssertListMap = Maps.newHashMap();
        LinkedHashMap<String, Map<String, Rule>> e2eAssertConfig = e2eNodeCache.getE2eAssertConfig();
        if (!e2eAssertConfig.isEmpty()) {
            //查询新配置的断言
            Map<String, Rule> nodeRule = e2eAssertConfig.get(nodeId);
            //查询历史配置断言
            Map<String, Rule> edgeRule = e2eAssertConfig.get(edgeId);
            if (nodeRule != null) {
                nodeAssertListMap.putAll(nodeRule);
            }
            if (edgeRule != null) {
                nodeAssertListMap.putAll(edgeRule);
            }
        }
        //日志时间
        Long traceTime = rpcBased.getLogTime();
        AggregateSlot<Metric, CallStat> slot = traceMetrics2Aggarator.getSlotByTimestamp(traceTime);

        /**
         * 汇总信息实现：单独存储一张表，不区分断言类型，断言不通过的要算做失败
         * 每个断言命中次数：遍历断言，记录每个断言的命中情况，命中则失败次数+1，没命中则成功次数+1
         * 断言的单独存储一张表，断言的仅记录命中的失败记录
         * 一条数据过来需要调用多次slot.addToSlot方法
         * 统计每个断言命中情况的时候就通过查询 group by exceptionType
         * exceptionType 共有三种类型：exception、resultCode、assertCode
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
                    logger.error("rule:{} calculate fail:{},{}", rule.toString(), e, e.getStackTrace());
                }

            }
        }

        //汇总分组的tag标签,包含生成边的一些关键指标
        ArrayList<String> tags = Lists.newArrayList();
        tags.add(edgeId);
        tags.add(clusterTest);
        for (Map.Entry<String, Object> entry : eagleTags.entrySet()) {
            tags.add(Objects.toString(entry.getValue()));
        }

        String sqlStatement = null;
        //如果是数据库调用,放入sql语句,否则放入null
        if (rpcBased.getRpcType() == 4) {
            //sql统计转大写,防止相同sql不同大小写导致的分组过多
            sqlStatement = StringUtils.isNotBlank(rpcBased.getCallbackMsg()) ? rpcBased.getCallbackMsg().toUpperCase() : "null";
        } else {
            sqlStatement = "null";
        }
        tags.add(Md5Utils.md5(sqlStatement));
        tags.add(userAppKey);
        tags.add(envCode);

        //如果sql长度超过1024,做截取,必须要在生成md5后做处理
        if (sqlStatement.length() > 1024) {
            sqlStatement = sqlStatement.substring(0, 1024);
        }
        //zcc:当前应用的ip
        String hostIp = rpcBased.getHostIp();
        //zcc:从RpcBased中取指标数据,以traceId为纬度
        TraceMetrics traceMetrics = TraceMetrics.convert(rpcBased, sampling, exceptionTypeList);
        // 冗余字段信息
        String traceId = traceMetrics.getTraceId();
        // 总次数/成功次数/totalRt/错误次数/hitCount/totalTps/总次数(不计算采样率)/e2e成功次数/e2e失败次数/最大耗时
        CallStat callStat = new CallStat(traceId, sqlStatement,
                traceMetrics.getTotalCount(), traceMetrics.getSuccessCount(), traceMetrics.getTotalRt(),
                traceMetrics.getFailureCount(), traceMetrics.getHitCount(), traceMetrics.getQps().longValue(), 1, traceMetrics.getE2eSuccessCount(), traceMetrics.getE2eErrorCount(), traceMetrics.getMaxRt());
        if (conditionWithHostIp()) tags.add(hostIp);
        slot.addToSlot(Metric.of(PradarRtConstant.METRICS_ID_TRACE, tags.toArray(new String[tags.size()]), "", new String[]{}), callStat);
    }

    @Override
    public int threadCount() {
        return 1;
    }

    @Override
    public void stop() {
        try {
            if (traceMetrics2Aggarator != null) {
                traceMetrics2Aggarator.stop();
            }
        } catch (Throwable e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public void init() {
        e2eNodeCache.autoRefresh(mysqlSupport);
        RuleFactory.INSTANCE.regsiterVariant(new Class[]{RpcBased.class}, new String[]{"node"});
        logger.info("e2eNodeCache:{}", e2eNodeCache);
    }

    protected String getNodeId(String parsedAppName, String parsedServiceName, String parsedMethod, String rpcType) {
        return Md5Utils.md5(parsedAppName + "|" + parsedServiceName + "|" + parsedMethod + "|" + rpcType);
    }

}
