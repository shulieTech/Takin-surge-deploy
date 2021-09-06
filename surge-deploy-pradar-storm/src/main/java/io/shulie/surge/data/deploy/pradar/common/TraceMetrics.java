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

import com.pamirs.pradar.log.parser.metrics.MetricsBased;
import com.pamirs.pradar.log.parser.trace.RpcBased;

import java.math.BigDecimal;

/**
 * @Author: xingchen
 * @ClassName: TraceMetrics
 * @Package: io.shulie.surge.data.deploy.pradar.common
 * @Date: 2020/12/313:51
 * @Description:
 */
public class TraceMetrics extends MetricsBased {
    /**
     * 入口标识
     */
    private String entryIndex;
    /**
     * 入口应用
     */
    private String traceAppName;

    /**
     * 通过rpcBase的转换traceMetrics
     *
     * @param rpcBased
     * @return
     */
    public static TraceMetrics convert(RpcBased rpcBased, int sampling) {
        final TraceMetrics traceMetrics = new TraceMetrics();
        // 设置默认值
        traceMetrics.setAppName(rpcBased.getAppName());
        traceMetrics.setTraceAppName(rpcBased.getTraceAppName());
        traceMetrics.setTimestamp(rpcBased.getLogTime());
        traceMetrics.setTotalRt(rpcBased.getCost() * sampling);
        traceMetrics.setTotalCount(1L * sampling);
        traceMetrics.setHitCount(0L);
        traceMetrics.setQps(BigDecimal.ONE.multiply(BigDecimal.valueOf(sampling)));
        traceMetrics.setClusterTest(rpcBased.isClusterTest());
        traceMetrics.setTraceId(rpcBased.getTraceId());
        // 判断是否成功
        if (isSuccess(rpcBased)) {
            traceMetrics.setSuccessCount(1L * sampling);
            traceMetrics.setFailureCount(0L);
        } else {
            traceMetrics.setSuccessCount(0L);
            traceMetrics.setFailureCount(1L * sampling);
        }
        return traceMetrics;
    }

    public String getEntryIndex() {
        return entryIndex;
    }

    public void setEntryIndex(String entryIndex) {
        this.entryIndex = entryIndex;
    }

    public String getTraceAppName() {
        return traceAppName;
    }

    public void setTraceAppName(String traceAppName) {
        this.traceAppName = traceAppName;
    }

    public static boolean isSuccess(RpcBased rpcBased) {
        return "".equals(rpcBased.getResultCode()) || "00".equals(rpcBased.getResultCode()) || "200".equals(rpcBased.getResultCode());
    }
}
