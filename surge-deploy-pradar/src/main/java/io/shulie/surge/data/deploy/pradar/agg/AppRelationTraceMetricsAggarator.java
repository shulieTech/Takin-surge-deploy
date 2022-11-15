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
import io.shulie.surge.data.deploy.pradar.common.PradarRtConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        super(AppRelationTraceMetricsAggarator.class.getSimpleName(), PradarRtConstant.REDUCE_APP_RELATION_TRACE_METRICS_STREAM_ID);
    }
}
