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
    protected AppConfigUtil appConfigUtil;

    @Override
    public void digest(DigestContext<RpcBased> context) {

        RpcBased rpcBased = context.getContent();
        //客户端rpc日志不计算指标,只计算服务端日志,和链路拓扑图保持一致
        if (PradarLogType.LOG_TYPE_FLOW_ENGINE == rpcBased.getLogType() || (PradarLogType.LOG_TYPE_RPC_CLIENT == rpcBased.getLogType())) {
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


        //获取是否压测流量
        String clusterTest = String.valueOf(rpcBased.isClusterTest());
        //todo 验证取采样率是否兼容租户
        String userAppKey = rpcBased.getUserAppKey();
        String envCode = rpcBased.getEnvCode();
        //获取每个应用的采样率
        int sampling = 1;
        //对于调试流量,agent不采样,采样率默认为1
        if (!rpcBased.getFlags().isDebugTest()) {
            sampling = appConfigUtil.getAppSamplingByAppName(userAppKey, envCode, rpcBased.getAppName(), clusterTest);
        }

        //日志时间
        Long traceTime = rpcBased.getLogTime();
        AggregateSlot<Metric, CallStat> slot = traceMetrics2Aggarator.getSlotByTimestamp(traceTime);


        //汇总分组的tag标签,包含生成边的一些关键指标
        ArrayList<String> tags = Lists.newArrayList();

        tags.add(clusterTest);
        tags.add(rpcBased.getAppName());

        tags.add(userAppKey);
        tags.add(envCode);

        //zcc:当前应用的ip
        String hostIp = rpcBased.getHostIp();
        String agentId = rpcBased.getAgentId();
        tags.add(hostIp);
        tags.add(agentId);

        //zcc:从RpcBased中取指标数据,以traceId为纬度
        TraceMetrics traceMetrics = TraceMetrics.convert(rpcBased, sampling, new ArrayList<>());
        // 冗余字段信息
        String traceId = traceMetrics.getTraceId();
        // 总次数/成功次数/totalRt/错误次数/hitCount/totalTps/总次数(不计算采样率)/e2e成功次数/e2e失败次数/最大耗时
        CallStat callStat = new CallStat(traceId,
                traceMetrics.getTotalCount(), traceMetrics.getSuccessCount(), traceMetrics.getTotalRt(),
                traceMetrics.getFailureCount(), traceMetrics.getHitCount(), traceMetrics.getQps().longValue(), 1, traceMetrics.getMaxRt());

        slot.addToSlot(Metric.of(PradarRtConstant.WATERLINE_METRICS_ID_TRACE, tags.toArray(new String[tags.size()]), "", new String[]{}), callStat);
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
}
