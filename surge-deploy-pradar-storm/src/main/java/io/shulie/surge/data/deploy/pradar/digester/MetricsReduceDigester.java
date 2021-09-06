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
import com.pamirs.pradar.log.parser.metrics.MetricsBased;
import io.shulie.surge.data.common.aggregation.AggregateSlot;
import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.common.utils.TopicFormatUtils;
import io.shulie.surge.data.deploy.pradar.agg.MetricsAggarator;
import io.shulie.surge.data.deploy.pradar.common.StringUtil;
import io.shulie.surge.data.runtime.common.remote.DefaultValue;
import io.shulie.surge.data.runtime.common.remote.Remote;
import io.shulie.surge.data.runtime.common.utils.ApiProcessor;
import io.shulie.surge.data.runtime.digest.DataDigester;
import io.shulie.surge.data.runtime.digest.DigestContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: xingchen
 * @ClassName: MetricsDigester
 * @Package: io.shulie.surge.data.deploy.pradar.digester
 * @Date: 2020/11/1614:40
 * @Description:
 */
@Singleton
public class MetricsReduceDigester implements DataDigester<MetricsBased> {
    private static Logger logger = LoggerFactory.getLogger(MetricsReduceDigester.class);
    private static final String METRICS_ID = "tro_pradar";

    @Inject
    private MetricsAggarator metricsAggarator;

    @Inject
    @DefaultValue("true")
    @Named("/pradar/config/rt/metricsReduceDisable")
    private Remote<Boolean> metricsReduceDisable;

    @Override
    public void digest(DigestContext<MetricsBased> context) {
        if (metricsReduceDisable.get()) {
            return;
        }
        MetricsBased metricsBased = context.getContent();
        if (metricsBased == null) {
            logger.warn("parse MetricsBased is null " + context.getContent());
            return;
        }
        format(metricsBased);
        // 拼接唯一值
        String[] tags = new String[]{metricsBased.getAppName(), metricsBased.getEvent(), metricsBased.getType(), String.valueOf(metricsBased.isClusterTest()), metricsBased.getCallEvent(), metricsBased.getCallType()
                , String.valueOf(metricsBased.isEntry())};
        long timeStamp = metricsBased.getTimestamp();
        AggregateSlot<Metric, CallStat> slot = metricsAggarator.getSlotByTimestamp(timeStamp);
        // 冗余字段信息
        String traceId = metricsBased.getTraceId();
        // 总次数/成功次数/totalRt/错误次数/hitCount/totalQps/totalTps/total
        CallStat callStat = new CallStat(traceId,
                metricsBased.getTotalCount(), metricsBased.getSuccessCount(), metricsBased.getTotalRt(),
                metricsBased.getFailureCount(), metricsBased.getHitCount(), metricsBased.getQps().longValue(),
                metricsBased.getQps().longValue(), 1);
        slot.addToSlot(Metric.of(METRICS_ID, tags, "", new String[]{}), callStat);
    }

    private static void format(MetricsBased metricsBased) {
        if ("entry-http".equals(metricsBased.getType())) {
            metricsBased.setEvent(ApiProcessor.merge(metricsBased.getAppName(), metricsBased.getEvent(), ""));
        }
        if (metricsBased.isClusterTest()) {
            if ("entry-rocketmq".equals(metricsBased.getType())
                    || "entry-ibmmq".equals(metricsBased.getType())
                    || "entry-kafka".equals(metricsBased.getType())
                    || "entry-rabbitmq".equals(metricsBased.getType())
                    || "entry-activemq".equals(metricsBased.getType())) {
                String event = metricsBased.getEvent();
                metricsBased.setEvent(TopicFormatUtils.replaceAll(event));
            }
            if ("call-rocketmq".equals(metricsBased.getCallType())
                    || "call-ibmmq".equals(metricsBased.getCallType())
                    || "call-rabbitmq".equals(metricsBased.getCallType())
                    || "call-kafka".equals(metricsBased.getCallType())
                    || "call-activemq".equals(metricsBased.getCallType())) {
                String callEvent = metricsBased.getCallEvent();
                metricsBased.setCallEvent(TopicFormatUtils.replaceAll(callEvent));
            }
        }
        if ("entry-rocketmq".equals(metricsBased.getType())) {
            String event = metricsBased.getEvent();
            String tmpEvent = "";
            // xxx#yyy%zzz-->xxx#zzz
            if (event.contains("%")) {
                tmpEvent = event.substring(0, event.indexOf("#") + 1);
            }
            if (event.contains("%")) {
                tmpEvent = tmpEvent + event.substring(event.indexOf("%") + 1);
            }
            if (tmpEvent.contains(":")) {
                tmpEvent = tmpEvent.substring(0, tmpEvent.indexOf(":"));
            }
            metricsBased.setEvent(tmpEvent.trim());
        }
    }

    @Override
    public int threadCount() {
        return 1;
    }

    @Override
    public void stop() throws Exception {
        try {
            if (metricsAggarator != null) {
                metricsAggarator.stop();
            }
        } catch (Throwable e) {
        }
    }

    public static void main(String[] args) {
        MetricsBased metricsBased = new MetricsBased();
        metricsBased.setType("entry-rocketmq");
        metricsBased.setEvent("trace_except_package_info#MQ_INST_1942653734864712_GOhQcxxx%GID_sto_oms_trace_sync:123");
        format(metricsBased);
        System.out.println(metricsBased.getEvent());
    }
}
