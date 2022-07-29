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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
import io.shulie.surge.data.common.aggregation.DefaultAggregator;
import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.deploy.pradar.common.AppConfigUtil;
import io.shulie.surge.data.deploy.pradar.common.E2ENodeCache;
import io.shulie.surge.data.deploy.pradar.common.EagleLoader;
import io.shulie.surge.data.deploy.pradar.common.PradarRtConstant;
import io.shulie.surge.data.deploy.pradar.config.metrics.E2eTraceMetrics;
import io.shulie.surge.data.deploy.pradar.listener.E2EMetricsResultListener;
import io.shulie.surge.data.deploy.pradar.listener.E2ETraceMetricsResultListener;
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
import org.springframework.util.CollectionUtils;

@Singleton
public class E2EDefaultDigester implements DataDigester<RpcBased> {

    private static final Logger logger = LoggerFactory.getLogger(E2EDefaultDigester.class);

    @Inject
    private MysqlSupport mysqlSupport;

    @Inject
    private AppConfigUtil appConfigUtil;

    @Inject
    private E2EMetricsResultListener e2EMetricsResultListener;

    @Inject
    private E2ETraceMetricsResultListener e2eTraceMetricsResultListener;

    @Inject
    private EagleLoader eagleLoader;

    @Inject
    @DefaultValue("sto-%,sas-prod,es-api-base-prod,automation-biz-prod,galaxy")
    @Named("/pradar/config/rt/traceMetricsConfig")
    private Remote<String> traceMetricsConfig;

    @Inject
    @DefaultValue("")
    @Named("/pradar/config/rt/traceMetricsTenantConfig")
    private Remote<String> traceMetricsTenantConfig;

    private final E2ENodeCache e2eNodeCache = new E2ENodeCache();

    private final transient AtomicBoolean isRunning = new AtomicBoolean(false);

    private DefaultAggregator e2eAssertMetricsAggregator;
    private DefaultAggregator e2eTraceMetricsAggregator;

    private final Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(1, TimeUnit.HOURS).build();

    // update copy from io.shulie.surge.data.deploy.pradar.digester.TraceMetricsDiggester.digest
    @Override
    public void digest(DigestContext<RpcBased> context) {
        if (isRunning.compareAndSet(false, true)) {
            try {
                e2eAssertMetricsAggregator = new DefaultAggregator(5, 60);
                e2eAssertMetricsAggregator.addListener(PradarRtConstant.E2E_METRICS_ID_TRACE, e2EMetricsResultListener);
                e2eAssertMetricsAggregator.start();

                e2eTraceMetricsAggregator = new DefaultAggregator(5, 60);
                e2eTraceMetricsAggregator.addListener(PradarRtConstant.METRICS_ID_TRACE, e2eTraceMetricsResultListener);
                e2eTraceMetricsAggregator.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        RpcBased rpcBased = context.getContent();
        //客户端rpc日志不计算指标,只计算服务端日志,和链路拓扑图保持一致
        if (PradarLogType.LOG_TYPE_FLOW_ENGINE == rpcBased.getLogType()
                || (PradarLogType.LOG_TYPE_RPC_CLIENT == rpcBased.getLogType() && MiddlewareType.TYPE_RPC == rpcBased.getRpcType())) {
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

        /** 指标计算不参考业务活动的边**/
        String appName = rpcBased.getAppName();
        if (!eagleLoader.contains(edgeId)) {
            /** 指标计算不参考业务活动的边**/
            String appNameConfig = traceMetricsConfig.get();
            String tenantConfig = traceMetricsTenantConfig.get();
            //不在业务活动拓扑图中的边,如果需要提供服务监控接口性能查询,走应用配置和租户配置查询(客户端日志不计算)
            if (rpcBased.getLogType() == PradarLogType.LOG_TYPE_RPC_CLIENT
                    || !(checkAppName(appNameConfig, appName)
                    || checkTenant(tenantConfig, rpcBased.getUserAppKey(), rpcBased.getEnvCode()))) {

                //重复的边ID只打印一次
                if (StringUtils.isBlank(cache.getIfPresent(edgeId))) {
                    cache.put(edgeId, edgeId);
                    logger.warn("edgeId is not match:{},edgeTags is {}", edgeId, eagleTags);
                }
                return;
            }
            rpcBased.setEntranceId(""); //如果边不在真实业务活动中,把所有入口流量汇总一起算指标
            edgeId = rpcBasedParser.edgeId("", rpcBased);
            eagleTags = rpcBasedParser.edgeTags("", rpcBased);
        }
        // 是否压测流量
        String clusterTest = String.valueOf(rpcBased.isClusterTest());
        String parsedAppName = StringUtils.defaultString(rpcBasedParser.appNameParse(rpcBased), "");
        String parsedServiceName = StringUtils.defaultString(rpcBasedParser.serviceParse(rpcBased), "");
        String parsedMethod = StringUtils.defaultString(rpcBasedParser.methodParse(rpcBased), "");
        String rpcType = rpcBased.getRpcType() + "";
        String nodeId = getNodeId(parsedAppName, parsedServiceName, parsedMethod, rpcType);
        String userAppKey = rpcBased.getUserAppKey();
        String envCode = rpcBased.getEnvCode();
        //获取每个应用的采样率
        int simpling = 1;
        //对于调试流量,agent不采样,采样率默认为1
        if (!rpcBased.getFlags().isDebugTest()) {
            simpling = appConfigUtil.getAppSamplingByAppName(userAppKey, envCode, rpcBased.getAppName(), clusterTest);
        }
        // 断言列表
        Map<String, Rule> nodeAssertListMap = Maps.newHashMap();
        Map<String, Map<String, Rule>> e2eAssertConfig = e2eNodeCache.getE2eAssertConfig();
        if (!CollectionUtils.isEmpty(e2eAssertConfig)) {
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
        long timeStamp = rpcBased.getLogTime();
        AggregateSlot<Metric, CallStat> e2eAssertMetrics = e2eAssertMetricsAggregator.getSlotByTimestamp(timeStamp);
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

            // 写入断言指标
            String assertClusterTest = rpcBased.isClusterTest() ? "1" : "0";
            for (String exceptionType : exceptionTypeList) {
                String[] tags = new String[]{nodeId, parsedAppName, parsedServiceName, parsedMethod, rpcType,
                        assertClusterTest, exceptionType};
                CallStat callStat = new CallStat(
                        simpling, 0, simpling * rpcBased.getCost(), simpling, simpling);
                e2eAssertMetrics.addToSlot(Metric.of(PradarRtConstant.E2E_ASSERT_METRICS_ID_TRACE, tags,
                        "", new String[]{}), callStat);
            }

            AggregateSlot<Metric, CallStat> e2eTraceMetricsSlot = e2eTraceMetricsAggregator.getSlotByTimestamp(timeStamp);
            //汇总分组的tag标签,包含生成边的一些关键指标
            ArrayList<String> tags = Lists.newArrayList();
            tags.add(edgeId);
            tags.add(clusterTest);
            for (Map.Entry<String, Object> entry : eagleTags.entrySet()) {
                tags.add(Objects.toString(entry.getValue()));
            }

            String sqlStatement;
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

            E2eTraceMetrics traceMetrics = E2eTraceMetrics.convert(rpcBased, simpling, exceptionTypeList);
            // 冗余字段信息
            String traceId = traceMetrics.getTraceId();
            // 总次数/成功次数/totalRt/错误次数/hitCount/totalTps/总次数(不计算采样率)/e2e成功次数/e2e失败次数/最大耗时
            CallStat callStat = new CallStat(traceId, sqlStatement,
                    traceMetrics.getTotalCount(), traceMetrics.getSuccessCount(), traceMetrics.getTotalRt(),
                    traceMetrics.getFailureCount(), traceMetrics.getHitCount(), traceMetrics.getQps().longValue(),
                    1, traceMetrics.getE2eSuccessCount(), traceMetrics.getE2eErrorCount(), traceMetrics.getMaxRt());
            e2eTraceMetricsSlot.addToSlot(Metric.of(PradarRtConstant.METRICS_ID_TRACE, tags.toArray(new String[0]), "", new String[]{}), callStat);
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
        if (e2eAssertMetricsAggregator != null) {
            e2eAssertMetricsAggregator.stop();
        }
        if (e2eTraceMetricsAggregator != null) {
            e2eTraceMetricsAggregator.stop();
        }
    }

    private String getNodeId(String parsedAppName, String parsedServiceName, String parsedMethod, String rpcType) {
        return Md5Utils.md5(parsedAppName + "|" + parsedServiceName + "|" + parsedMethod + "|" + rpcType);
    }

    public void init() {
        e2eNodeCache.autoRefresh(mysqlSupport);
        RuleFactory.INSTANCE.regsiterVariant(new Class[]{RpcBased.class}, new String[]{"node"});
        logger.info("e2eNodeCache:{}", e2eNodeCache);
        eagleLoader.init();

    }

    private boolean checkAppName(String appNameConfig, String appName) {
        if (StringUtils.isNotBlank(appNameConfig)) {
            String[] appNameArr = appNameConfig.split(",");
            for (String s : appNameArr) {
                //如果含有百分号,启用前缀匹配
                if (s.contains("%")) {
                    //前缀匹配
                    if (StringUtils.isNotBlank(s.split("%")[0]) && appName.startsWith(s.split("%")[0])) {
                        return true;
                    }
                    //后缀匹配
                    if (s.startsWith("%") && appName.endsWith(s.split("%")[1])) {
                        return true;
                    }
                } else {
                    //否则采用等值匹配
                    if (appName.equals(s)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkTenant(String config, String tenantAppKey, String envCode) {
        if (StringUtils.isNotBlank(config) && TenantEnvConstants.ENV_PROD.equalsIgnoreCase(envCode)) {
            String[] tenantArr = config.split(",");
            for (String s : tenantArr) {
                if (tenantAppKey.equals(s)) {
                    return true;
                }
            }
        }
        return false;
    }
}
