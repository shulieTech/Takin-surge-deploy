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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.common.aggregation.AggregateSlot;
import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.deploy.pradar.agg.TraceMetricsAggarator;
import io.shulie.surge.data.deploy.pradar.common.AppConfigUtil;
import io.shulie.surge.data.deploy.pradar.common.EagleLoader;
import io.shulie.surge.data.deploy.pradar.common.PradarRtConstant;
import io.shulie.surge.data.deploy.pradar.common.TraceMetrics;
import io.shulie.surge.data.deploy.pradar.parser.RpcBasedParser;
import io.shulie.surge.data.deploy.pradar.parser.RpcBasedParserFactory;
import io.shulie.surge.data.runtime.common.remote.DefaultValue;
import io.shulie.surge.data.runtime.common.remote.Remote;
import io.shulie.surge.data.runtime.digest.DataDigester;
import io.shulie.surge.data.runtime.digest.DigestContext;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

/**
 * trace日志-metrics计算
 *
 * @Author: xingchen
 * @ClassName: TraceMetricsDiggester
 * @Package: io.shulie.surge.data.deploy.pradar.digester
 * @Date: 2020/12/313:24
 * @Description:
 */
@Singleton
public class TraceMetricsDiggester implements DataDigester<RpcBased> {
    private static Logger logger = LoggerFactory.getLogger(TraceMetricsDiggester.class);
    @Inject
    private TraceMetricsAggarator traceMetricsAggarator;

    @Inject
    private AppConfigUtil appConfigUtil;

    @Inject
    private EagleLoader eagleLoader;

    @Inject
    @DefaultValue("true")
    @Named("/pradar/config/rt/traceMetricsDisable")
    private Remote<Boolean> traceMetricsDisable;

    /**
     * 处理trace日志计算metrics
     *
     * @param context
     */
    @Override
    public void digest(DigestContext<RpcBased> context) {
        if (traceMetricsDisable.get()) {
            return;
        }
        RpcBased rpcBased = context.getContent();
        RpcBasedParser rpcBasedParser = RpcBasedParserFactory.getInstance(rpcBased.getLogType(), rpcBased.getRpcType());
        if (rpcBasedParser == null) {
            return;
        }
        // 生成唯一边Id ,同步zk集合，判断此流量是否要统计
        String edgeId = rpcBasedParser.edgeId("", rpcBased);
        if (!eagleLoader.contains(edgeId)) {
            return;
        }
        Long traceTime = rpcBased.getLogTime();
        Map<String, Object> eagleTags = rpcBasedParser.edgeTags("", rpcBased);
        ArrayList<String> tags = Lists.newArrayList();
        tags.add(edgeId);
        tags.add(String.valueOf(rpcBased.isClusterTest()));
        for (Map.Entry<String, Object> entry : eagleTags.entrySet()) {
            tags.add(Objects.toString(entry.getValue()));
        }

        int sampling = appConfigUtil.getAppSamplingByAppName(rpcBased.getAppName());
        TraceMetrics traceMetrics = TraceMetrics.convert(rpcBased, sampling);
        AggregateSlot<Metric, CallStat> slot = traceMetricsAggarator.getSlotByTimestamp(traceTime);
        // 冗余字段信息
        String traceId = rpcBased.getTraceId();
        // 总次数/成功次数/totalRt/错误次数/hitCount/totalQps/totalTps/total
        CallStat callStat = new CallStat(traceId,
                traceMetrics.getTotalCount(), traceMetrics.getSuccessCount(), traceMetrics.getTotalRt(),
                traceMetrics.getFailureCount(), traceMetrics.getHitCount(), traceMetrics.getQps().longValue(),
                traceMetrics.getQps().longValue(), 1);
        slot.addToSlot(Metric.of(PradarRtConstant.METRICS_ID_TRACE, tags.toArray(new String[tags.size()]), "", new String[]{}), callStat);
    }

    @Override
    public int threadCount() {
        return 1;
    }

    @Override
    public void stop() {
        try {
            if (traceMetricsAggarator != null) {
                traceMetricsAggarator.stop();
            }
        } catch (Throwable e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
