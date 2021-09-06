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

package io.shulie.surge.data.deploy.pradar.common;

public class PradarRtConstant {
    public static final String REDUCE_STREAM_ID = "pradar-reduce";

    /**
     * Pradar Stream ID
     */
    public static final String REDUCE_METRICS_STREAM_ID = "pradar-reduce-metrics";

    // trace记录metrics
    public static final String REDUCE_TRACE_METRICS_STREAM_ID = "pradar-trace-reduce-metrics";

    // trace记录app关系metrics
    public static final String REDUCE_APP_RELATION_TRACE_METRICS_STREAM_ID = "pradar-app-relation-trace-reduce-metrics";

    // e2e 节点指标统计
    public static final String REDUCE_E2E_TRACE_METRICS_STREAM_ID = "e2e-trace-reduce-metrics";

    /**
     * trace记录metrics
     */
    public static final String METRICS_ID_TRACE = "trace_metrics";

    /**
     * trace记录App关系metrics
     */
    public static final String APP_RELATION_METRICS_ID_TRACE = "trace_app_metrics";

    /**
     * trace记录App关系metrics
     */
    public static final String E2E_METRICS_ID_TRACE = "trace_e2e_metrics";
    /**
     * trace记录App关系metrics
     */
    public static final String E2E_ASSERT_METRICS_ID_TRACE = "trace_e2e_assert_metrics";

    /**
     * trace_metrics数据汇总时间间隔，单位：秒
     */
    public static final int AGG_TRACE_SECONDS_INTERVAL = 1;

    /**
     * trace_metrics实时数据汇总的延时，单位：秒
     */
    public static final int AGG_TRACE_SECONDS_LOWER_LIMIT = 30;

    /**
     * 实时数据汇总的间隔，单位：秒
     */
    public static final int REDUCE_TRACE_SECONDS_INTERVAL = 5;

    /**
     * 实时数据汇总的延时，单位：秒
     */
    public static final int REDUCE_TRACE_SECONDS_LOWER_LIMIT = 90;
}
