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

package io.shulie.surge.data.deploy.pradar.agg;

import com.google.inject.Singleton;
import io.shulie.surge.data.common.utils.Pair;
import io.shulie.surge.data.deploy.pradar.PradarTraceReduceBolt;
import io.shulie.surge.data.common.aggregation.AggregateSlot;
import io.shulie.surge.data.common.aggregation.Aggregation;
import io.shulie.surge.data.common.aggregation.Aggregator;
import io.shulie.surge.data.common.aggregation.Scheduler;
import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.common.utils.FormatUtils;
import io.shulie.surge.data.deploy.pradar.common.PradarRtConstant;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.shulie.surge.data.common.utils.CommonUtils.divide;

/**
 * @Author: xingchen
 * @ClassName: TraceMetricsAggarator
 * @Package: io.shulie.surge.data.runtime.agg
 * @Date: 2020/11/3010:18
 * @Description:
 */
@Singleton
public class TraceMetricsAggarator implements Aggregator {
    private static Logger logger = LoggerFactory.getLogger(TraceMetricsAggarator.class);
    private final Aggregation<Metric, CallStat> aggregation = new Aggregation<>(
            PradarRtConstant.AGG_TRACE_SECONDS_INTERVAL,
            PradarRtConstant.AGG_TRACE_SECONDS_LOWER_LIMIT);

    /**
     * init
     *
     * @param scheduler
     * @param collector
     * @param topologyContext
     */
    public void init(Scheduler scheduler,
                     final SpoutOutputCollector collector,
                     final TopologyContext topologyContext) {
        aggregation.start(scheduler, new Aggregation.CommitAction<Metric, CallStat>() {
            @Override
            public void commit(long slotKey, AggregateSlot<Metric, CallStat> slot) {
                int size = slot.size();
                if (size > 0) {
                    Map<Metric, CallStat> map = slot.toMap();
                    // 每个 reducer 负责处理一组 Pair<Metric, CallStat> 的汇总任务 Job
                    // 直接 emit 出去由 Storm 进行分配，会导致消息丢失率很高，所以打包发送。
                    List<Integer> reducerIds = topologyContext.getComponentTasks(PradarTraceReduceBolt.class.getSimpleName());
                    final int reducerCount = reducerIds.size();
                    List<Pair<Metric, CallStat>>[] jobs = new List[reducerCount];
                    int jobSize = (int) divide(size, reducerCount - 1); // 估值
                    for (int i = 0; i < reducerCount; ++i) {
                        jobs[i] = new ArrayList<>(jobSize);
                    }
                    // 将汇总任务按照 reducer 的数量哈希分配
                    // 哈希策略必须全局一致，使同一个 metric 落在同一个 reducer 上面
                    for (Map.Entry<Metric, CallStat> entry : map.entrySet()) {
                        int jobId = Math.abs(entry.getKey().hashCode()) % reducerCount;
                        List<Pair<Metric, CallStat>> job = jobs[jobId];
                        job.add(new Pair<>(entry.getKey(), entry.getValue()));
                    }
                    // 将 Job 直接发送给 reducer
                    final String slotKeyTime = FormatUtils.toSecondTimeString(slotKey * 1000);
                    for (int i = 0; i < reducerCount; ++i) {
                        int reducerId = reducerIds.get(i).intValue();
                        List<Pair<Metric, CallStat>> job = jobs[i];
                        if (!job.isEmpty()) {
                            collector.emitDirect(reducerId, PradarRtConstant.REDUCE_TRACE_METRICS_STREAM_ID, new Values(slotKey * 1000, job));
                        }
                    }
                    //logger.info("emit " + slotKeyTime + " to " + reducerIds.size() + " reducers, size=" + size);
                }
            }
        });
    }

    /**
     * ----
     */
    @Override
    public int size() {
        return 0;
    }

    @Override
    public int getInterval() {
        return 0;
    }

    @Override
    public int getLowerLimit() {
        return 0;
    }

    @Override
    public long getLowerBound() {
        return 0;
    }

    /**
     * 槽位到时间戳，slotKey 到 timestamp 的逆转换，已经丢失精度
     *
     * @param key
     * @return
     */
    @Override
    public long slotKeyToTimestamp(long key) {
        return aggregation.slotKeyToTimestamp(key);
    }

    /**
     * 时间戳到槽位
     *
     * @param timestamp
     * @return
     */
    @Override
    public long timestampToSlotKey(long timestamp) {
        return aggregation.timestampToSlotKey(timestamp);
    }

    /**
     * 获取时间戳的槽位
     *
     * @param timestamp
     * @return
     */
    @Override
    public AggregateSlot<Metric, CallStat> getSlotByTimestamp(long timestamp) {
        return aggregation.getSlotByTimestamp(timestamp);
    }

    /**
     * 获取时间戳的槽位
     *
     * @param timestamp
     * @return
     */
    @Override
    public AggregateSlot<Metric, CallStat> getSlotByTimestamp(String timestamp) {
        return aggregation.getSlotByTimestamp(NumberUtils.toLong(timestamp));
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {

    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
