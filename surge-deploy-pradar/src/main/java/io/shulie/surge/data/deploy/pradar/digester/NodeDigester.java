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
import io.shulie.surge.data.common.aggregation.AggregateSlot;
import io.shulie.surge.data.common.aggregation.DefaultAggregator;
import io.shulie.surge.data.common.aggregation.Scheduler;
import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.deploy.pradar.listener.NodeResultListener;
import io.shulie.surge.data.runtime.common.remote.DefaultValue;
import io.shulie.surge.data.runtime.common.remote.Remote;
import io.shulie.surge.data.runtime.digest.DataDigester;
import io.shulie.surge.data.runtime.digest.DigestContext;
import io.shulie.surge.data.suppliers.grpc.remoting.node.NodeInfo;
import io.shulie.surge.data.suppliers.grpc.remoting.node.ThreadStat;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: xingchen
 * @ClassName: MetricsDigester
 * @Package: io.shulie.surge.data.deploy.pradar.digester
 * @Date: 2020/11/1614:40
 * @Description:
 */
@Singleton
public class NodeDigester implements DataDigester<NodeInfo> {
    private static Logger logger = LoggerFactory.getLogger(NodeDigester.class);

    private static final String METRICS_ID = "app_node_info";

    @Inject
    @DefaultValue("false")
    @Named("/pradar/config/rt/nodeMetricsDisable")
    private Remote<Boolean> nodeMetricsDisable;

    @Inject
    private NodeResultListener nodeResultListener;

    private transient AtomicBoolean isRunning = new AtomicBoolean(false);

    private DefaultAggregator defaultAggregator;

    private Scheduler scheduler = new Scheduler(1);

    @Override
    public void digest(DigestContext<NodeInfo> context) {
        if (nodeMetricsDisable.get()) {
            return;
        }
        if (isRunning.compareAndSet(false, true)) {
            try {
                defaultAggregator = new DefaultAggregator(5, 60, scheduler);
                defaultAggregator.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        NodeInfo nodeInfo = context.getContent();
        if (nodeInfo == null) {
            logger.warn("parse GcBased is null " + context.getContent());
            return;
        }
        // 拼接唯一值
        String[] tags = new String[]{
                nodeInfo.getResource().getAppName(),
                nodeInfo.getResource().getIp(),
                nodeInfo.getResource().getHost(),
                nodeInfo.getResource().getAgentId(),
                nodeInfo.getResource().getTenantCode(),
                nodeInfo.getResource().getEnv(),
        };
        long timeStamp = nodeInfo.getCollectionTime();
        AggregateSlot<Metric, CallStat> slot = defaultAggregator.getSlotByTimestamp(timeStamp);

        // 堆使用/eden 使用/survivor使用/old 区使用/metaspace 使用/codeCache 使用/nonHeap 使用/cpu 使用率/younggc次数/younggc 耗时
        // fullGc 次数/fullGc耗时/内存使用/ 新建线程数/运行中线程数/blocked 线程数/等待线程数/timed 等待线程数/terminated 线程数
        long newCount = 0, runnableCount = 0, blockedCount = 0, waitingCount = 0, timedWaitingCount = 0, terminatedCount = 0;
        for (ThreadStat threadStat : nodeInfo.getThread().getThreadInfo()) {
            if (StringUtils.equalsIgnoreCase(Thread.State.NEW.name(), threadStat.getState())) {
                newCount++;
            } else if (StringUtils.equalsIgnoreCase(Thread.State.RUNNABLE.name(), threadStat.getState())) {
                runnableCount++;
            } else if (StringUtils.equalsIgnoreCase(Thread.State.BLOCKED.name(), threadStat.getState())) {
                blockedCount++;
            } else if (StringUtils.equalsIgnoreCase(Thread.State.WAITING.name(), threadStat.getState())) {
                waitingCount++;
            } else if (StringUtils.equalsIgnoreCase(Thread.State.TIMED_WAITING.name(), threadStat.getState())) {
                timedWaitingCount++;
            } else if (StringUtils.equalsIgnoreCase(Thread.State.TERMINATED.name(), threadStat.getState())) {
                terminatedCount++;
            }
        }

        CallStat callStat = new CallStat(
                nodeInfo.getJvmInfo().getHeapUsed(),
                nodeInfo.getJvmInfo().getEdenUsed(),
                nodeInfo.getJvmInfo().getSurvivorUsed(),
                nodeInfo.getJvmInfo().getOldUsed(),
                nodeInfo.getJvmInfo().getMetaSpaceUsed(),
                nodeInfo.getJvmInfo().getCodeCacheUsed(),
                nodeInfo.getJvmInfo().getNonHeapUsed(),

                nodeInfo.getCpu().getCoresPercent().longValue() * 100,

                nodeInfo.getGcInfo().getYoungGcCount().longValue(),
                nodeInfo.getGcInfo().getYoungGcCost().longValue(),
                nodeInfo.getGcInfo().getFullGcCount().longValue(),
                nodeInfo.getGcInfo().getFullGcCost().longValue(),

                nodeInfo.getMemory().getMemoryUsed(),

                newCount,
                runnableCount,
                blockedCount,
                waitingCount,
                timedWaitingCount,
                terminatedCount
        );

        slot.addToSlot(Metric.of(METRICS_ID, tags, "", new String[]{}), callStat);
        defaultAggregator.addListener(METRICS_ID, nodeResultListener);
    }

    @Override
    public int threadCount() {
        return 1;
    }

    @Override
    public void stop() throws Exception {
        defaultAggregator.stop();
    }
}
