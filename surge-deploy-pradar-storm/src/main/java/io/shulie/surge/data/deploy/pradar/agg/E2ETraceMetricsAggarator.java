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
import io.shulie.surge.data.deploy.pradar.E2ETraceReduceBolt;
import io.shulie.surge.data.deploy.pradar.common.PradarRtConstant;

@Singleton
public class E2ETraceMetricsAggarator extends BaseTraceMetricsAggarator {

    public E2ETraceMetricsAggarator(){
        super(E2ETraceReduceBolt.class.getSimpleName(), PradarRtConstant.REDUCE_E2E_TRACE_METRICS_STREAM_ID);
    }
}