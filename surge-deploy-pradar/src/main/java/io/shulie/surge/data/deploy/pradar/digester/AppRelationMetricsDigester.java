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

import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.common.aggregation.AggregateSlot;
import io.shulie.surge.data.common.aggregation.DefaultAggregator;
import io.shulie.surge.data.common.aggregation.Scheduler;
import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.deploy.pradar.common.AppConfigUtil;
import io.shulie.surge.data.deploy.pradar.common.PradarRtConstant;
import io.shulie.surge.data.deploy.pradar.listener.AppRelationMetricsResultListener;
import io.shulie.surge.data.deploy.pradar.parser.RpcBasedParser;
import io.shulie.surge.data.deploy.pradar.parser.RpcBasedParserFactory;
import io.shulie.surge.data.runtime.digest.DataDigester;
import io.shulie.surge.data.runtime.digest.DigestContext;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.shulie.surge.data.deploy.pradar.common.PradarRtConstant.AGG_TRACE_SECONDS_INTERVAL;
import static io.shulie.surge.data.deploy.pradar.common.PradarRtConstant.AGG_TRACE_SECONDS_LOWER_LIMIT;

/**
 * @author caoyanfei@shulie.io
 * @description 应用服务中间件、应用关系、应用关系指标处理
 */
@Singleton
public class AppRelationMetricsDigester implements DataDigester<RpcBased> {

    private static Logger logger = LoggerFactory.getLogger(AppRelationMetricsDigester.class);

    @Inject
    private AppRelationMetricsResultListener appRelationMetricsResultListener;

    @Inject
    private AppConfigUtil appConfigUtil;

    private transient AtomicBoolean isRunning = new AtomicBoolean(false);

    private DefaultAggregator defaultAggregator;

    private Scheduler scheduler = new Scheduler(1);

    @Override
    public void digest(DigestContext<RpcBased> context) {
        if (!isRunning.get()) {
            if (isRunning.compareAndSet(false, true)) {
                try {
                    defaultAggregator = new DefaultAggregator(AGG_TRACE_SECONDS_INTERVAL, AGG_TRACE_SECONDS_LOWER_LIMIT, scheduler);
                    defaultAggregator.start();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        RpcBased rpcBased = context.getContent();
        if (rpcBased == null) {
            logger.warn("parse rpcBased is null " + context.getContent());
            return;
        }
        RpcBasedParser rpcBasedParser = RpcBasedParserFactory.getInstance(rpcBased.getLogType(), rpcBased.getRpcType());
        // 拼接唯一值
        String fromAppName = rpcBasedParser.serverAppNameParse(rpcBased);
        String toAppName = rpcBasedParser.appNameParse(rpcBased);
        if (fromAppName == null || toAppName == null) {
            return;
        }
        Map<String, Object> fromAppTags = rpcBasedParser.fromAppTags("", rpcBased);
        Map<String, Object> toAppTags = rpcBasedParser.toAppTags("", rpcBased);
        String fromAppType = ObjectUtils.toString(fromAppTags.get("middlewareName"));
        String toAppType = ObjectUtils.toString(toAppTags.get("middlewareName"));
        String[] tags = new String[]{fromAppName, toAppName, fromAppType, toAppType};

        long timeStamp = rpcBased.getLogTime();
        AggregateSlot<Metric, CallStat> slot = defaultAggregator.getSlotByTimestamp(timeStamp);
        // 总次数/成功次数/totalRt/错误次数/totalQps
        long successCount = rpcBased.isOk() ? 1 : 0;
        long failureCount = rpcBased.isOk() ? 0 : 1;
        Integer simpling = appConfigUtil.getAppSamplingByAppName(rpcBased.getAppName());
        CallStat callStat = new CallStat(
                simpling * 1L, simpling * successCount, simpling * rpcBased.getCost(),
                simpling * failureCount, simpling);
        slot.addToSlot(Metric.of(PradarRtConstant.APP_RELATION_METRICS_ID_TRACE, tags, "", new String[]{}), callStat);
        defaultAggregator.addListener(PradarRtConstant.APP_RELATION_METRICS_ID_TRACE, appRelationMetricsResultListener);
    }

    @Override
    public int threadCount() {
        return 1;
    }

    @Override
    public void stop() throws Exception {
    }
}






