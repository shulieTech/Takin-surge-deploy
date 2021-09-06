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
import io.shulie.surge.data.common.aggregation.AggregateSlot;
import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.deploy.pradar.agg.AppRelationTraceMetricsAggarator;
import io.shulie.surge.data.deploy.pradar.common.AppConfigUtil;
import io.shulie.surge.data.deploy.pradar.common.PradarRtConstant;
import io.shulie.surge.data.deploy.pradar.common.TraceMetrics;
import io.shulie.surge.data.deploy.pradar.parser.RpcBasedParser;
import io.shulie.surge.data.deploy.pradar.parser.RpcBasedParserFactory;
import io.shulie.surge.data.runtime.common.remote.DefaultValue;
import io.shulie.surge.data.runtime.common.remote.Remote;
import io.shulie.surge.data.runtime.digest.DataDigester;
import io.shulie.surge.data.runtime.digest.DigestContext;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * @Author: xingchen
 * @ClassName: TraceMetricsDiggester
 * @Package: io.shulie.surge.data.deploy.pradar.digester
 * @Date: 2020/12/313:24
 * @Description:
 */
@Singleton
public class AppRelationTraceMetricsDiggester implements DataDigester<RpcBased> {
    private static Logger logger = LoggerFactory.getLogger(AppRelationTraceMetricsDiggester.class);
    @Inject
    private AppRelationTraceMetricsAggarator appRelationTraceMetricsAggarator;

    @Inject
    private AppConfigUtil appConfigUtil;

    @Inject
    @DefaultValue("true")
    @Named("/pradar/config/rt/appRelationTraceMetricsDisable")
    private Remote<Boolean> appRelationTraceMetricsDisable;

    /**
     * 处理trace日志计算metrics
     *
     * @param context
     */
    @Override
    public void digest(DigestContext<RpcBased> context) {
        if (appRelationTraceMetricsDisable.get()) {
            return;
        }
        RpcBased rpcBased = context.getContent();
        if (rpcBased == null) {
            logger.warn("parse rpcBased is null " + context.getContent());
            return;
        }
        RpcBasedParser rpcBasedParser = RpcBasedParserFactory.getInstance(rpcBased.getLogType(), rpcBased.getRpcType());
        if (rpcBasedParser == null) {
            return;
        }
        // 拼接唯一值
        String fromAppName = rpcBasedParser.serverAppNameParse(rpcBased);
        String toAppName = rpcBasedParser.appNameParse(rpcBased);
        if (fromAppName == null || toAppName == null) {
            return;
        }
        if (fromAppName.equals(toAppName)) {
            return;
        }
        Map<String, Object> fromAppTags = rpcBasedParser.fromAppTags("", rpcBased);
        Map<String, Object> toAppTags = rpcBasedParser.toAppTags("", rpcBased);
        String fromAppType = ObjectUtils.toString(fromAppTags.get("middlewareName"));
        String toAppType = ObjectUtils.toString(toAppTags.get("middlewareName"));
        String[] tags = new String[]{fromAppName, toAppName, fromAppType, toAppType};
        long timeStamp = rpcBased.getLogTime();
        AggregateSlot<Metric, CallStat> slot = appRelationTraceMetricsAggarator.getSlotByTimestamp(timeStamp);
        // 总次数/成功次数/totalRt/错误次数/totalQps
        long successCount = TraceMetrics.isSuccess(rpcBased) ? 1 : 0;
        long failureCount = 1 - successCount;
        Integer simpling = appConfigUtil.getAppSamplingByAppName(rpcBased.getAppName());
        CallStat callStat = new CallStat(
                simpling * 1L, simpling * successCount, simpling * rpcBased.getCost(),
                simpling * failureCount, simpling);
        slot.addToSlot(Metric.of(PradarRtConstant.APP_RELATION_METRICS_ID_TRACE, tags, "", new String[]{}), callStat);
    }

    @Override
    public int threadCount() {
        return 1;
    }

    @Override
    public void stop() throws Exception {
        if (appRelationTraceMetricsAggarator != null) {
            appRelationTraceMetricsAggarator.stop();
        }
    }
}
