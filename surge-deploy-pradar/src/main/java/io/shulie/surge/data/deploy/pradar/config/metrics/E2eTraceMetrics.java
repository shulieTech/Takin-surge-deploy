package io.shulie.surge.data.deploy.pradar.config.metrics;

import java.math.BigDecimal;
import java.util.List;

import com.pamirs.pradar.log.parser.metrics.MetricsBased;
import com.pamirs.pradar.log.parser.trace.RpcBased;

// copy from io.shulie.surge.data.deploy.pradar.common.TraceMetrics
public class E2eTraceMetrics extends MetricsBased {
    /**
     * 入口标识
     */
    private String entryIndex;
    /**
     * 入口应用
     */
    private String traceAppName;
    /**
     * e2e成功次数
     */
    private long e2eSuccessCount;
    /**
     * e2e失败次数
     */
    private long e2eErrorCount;
    /**
     * 最大rt
     */
    private long maxRt;

    public static E2eTraceMetrics convert(RpcBased rpcBased, int sampling, List<String> exceptionTypeList) {
        final E2eTraceMetrics traceMetrics = new E2eTraceMetrics();
        // 设置默认值
        traceMetrics.setAppName(rpcBased.getAppName());
        traceMetrics.setTraceAppName(rpcBased.getTraceAppName());
        traceMetrics.setTimestamp(rpcBased.getLogTime());
        traceMetrics.setClusterTest(rpcBased.isClusterTest());
        traceMetrics.setTraceId(rpcBased.getTraceId());
        traceMetrics.setTotalRt(rpcBased.getCost() * sampling);
        traceMetrics.setMaxRt(rpcBased.getCost());
        traceMetrics.setQps(BigDecimal.ONE.multiply(BigDecimal.valueOf(sampling)));
        traceMetrics.setHitCount(0L);
        traceMetrics.setTotalCount(sampling);
        // 判断是否成功
        if (isSuccess(rpcBased)) {
            traceMetrics.setSuccessCount(sampling);
            traceMetrics.setFailureCount(0L);
        } else {
            traceMetrics.setSuccessCount(0L);
            traceMetrics.setFailureCount(sampling);
        }
        //由于e2e指标计算逻辑中将命中断言的数据认定为失败
        traceMetrics.setE2eSuccessCount(exceptionTypeList.size() > 0 ? 0 : sampling);
        traceMetrics.setE2eErrorCount(exceptionTypeList.size() > 0 ? sampling : 0);
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

    public long getE2eSuccessCount() {
        return e2eSuccessCount;
    }

    public void setE2eSuccessCount(long e2eSuccessCount) {
        this.e2eSuccessCount = e2eSuccessCount;
    }

    public long getE2eErrorCount() {
        return e2eErrorCount;
    }

    public void setE2eErrorCount(long e2eErrorCount) {
        this.e2eErrorCount = e2eErrorCount;
    }

    public long getMaxRt() {
        return maxRt;
    }

    public void setMaxRt(long maxRt) {
        this.maxRt = maxRt;
    }

    public static boolean isSuccess(RpcBased rpcBased) {
        return "".equals(rpcBased.getResultCode()) || "00".equals(rpcBased.getResultCode())
            || "200".equals(rpcBased.getResultCode());
    }
}