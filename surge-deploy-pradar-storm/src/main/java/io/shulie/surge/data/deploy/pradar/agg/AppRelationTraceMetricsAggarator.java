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
import io.shulie.surge.data.common.aggregation.AggregateSlot;
import io.shulie.surge.data.common.aggregation.Aggregation;
import io.shulie.surge.data.common.aggregation.Aggregator;
import io.shulie.surge.data.common.aggregation.Scheduler;
import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.common.utils.FormatUtils;
import io.shulie.surge.data.common.utils.Pair;
import io.shulie.surge.data.deploy.pradar.PradarAppRelationTraceReduceBolt;
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
import static io.shulie.surge.data.deploy.pradar.common.PradarRtConstant.AGG_TRACE_SECONDS_INTERVAL;
import static io.shulie.surge.data.deploy.pradar.common.PradarRtConstant.AGG_TRACE_SECONDS_LOWER_LIMIT;

/**
 * @Author: xingchen
 * @ClassName: TraceMetricsAggarator
 * @Package: io.shulie.surge.data.runtime.agg
 * @Date: 2020/11/3010:18
 * @Description:
 */
@Singleton
public class AppRelationTraceMetricsAggarator extends BaseTraceMetricsAggarator {
    private static Logger logger = LoggerFactory.getLogger(AppRelationTraceMetricsAggarator.class);

    public AppRelationTraceMetricsAggarator(){
        super(PradarAppRelationTraceReduceBolt.class.getSimpleName(),PradarRtConstant.REDUCE_APP_RELATION_TRACE_METRICS_STREAM_ID);
    }
}
