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

package io.shulie.surge.data.deploy.pradar.config.metrics;

import com.pamirs.pradar.log.parser.metrics.MetricsBased;
import io.shulie.surge.data.common.utils.TopicFormatUtils;
import io.shulie.surge.data.runtime.common.utils.ApiProcessor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author pamirs
 */
public class FluxInfoVO implements Serializable {
    /**
     * 唯一ID 意味着唯一一条数据
     */
    private String uuid;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 调用类型 http dubbo
     */
    private String type;

    /**
     * 当是DUBBO调用统计时 此字段为dubbo service name
     * 当是HTTP调用时 此字段为应用request lookup path
     */
    private String event;

    /**
     * 压测标识
     */
    private Boolean ptFlag;

    /**
     * 当前统计记录时间戳
     */
    private Long timestamp;

    /**
     * 周期内调用总次数
     */
    private Long totalCount;

    /**
     * 周期内成功总数
     */
    private Long successCount;

    /**
     * 周期内失败总数
     */
    private Long failureCount;

    /**
     * 周期内调用总延迟
     */
    private Long totalRt;

    /**
     * 每秒访问量
     */
    private Double qps;

    /**
     * 周期间隔 单位：秒
     */
    private Integer interval;

    private Double tps;

    /**
     * 缓存命中次数
     */
    private Long hitCount;

    /**
     * 调用类型
     */
    private String callType;

    /**
     * 调用信息 一个接口的操作
     * 当统计类型为DB时 此字段为数据库名称
     * 当统计类型是DUBBO时 此字段为dubbo service name
     * 当统计类型为MQ时 此字段为TOPIC
     * 当统计类型为Cache时 此字段为NULL
     */
    private String callEvent;
    private String entryFlag;
    private Long errorCount;
    private String sendTime;
    /**
     * 本机IP
     */
    private String localAddr;

    private String traceId;
    /**
     * agent id
     */
    protected String agentId;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Boolean getPtFlag() {
        return ptFlag;
    }

    public void setPtFlag(Boolean ptFlag) {
        this.ptFlag = ptFlag;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Long successCount) {
        this.successCount = successCount;
    }

    public Long getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Long failureCount) {
        this.failureCount = failureCount;
    }

    public Long getTotalRt() {
        return totalRt;
    }

    public void setTotalRt(Long totalRt) {
        this.totalRt = totalRt;
    }

    public Double getQps() {
        return qps;
    }

    public void setQps(Double qps) {
        this.qps = qps;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public Double getTps() {
        return tps;
    }

    public void setTps(Double tps) {
        this.tps = tps;
    }

    public Long getHitCount() {
        return hitCount;
    }

    public void setHitCount(Long hitCount) {
        this.hitCount = hitCount;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getCallEvent() {
        return callEvent;
    }

    public void setCallEvent(String callEvent) {
        this.callEvent = callEvent;
    }

    public String getEntryFlag() {
        return entryFlag;
    }

    public void setEntryFlag(String entryFlag) {
        this.entryFlag = entryFlag;
    }

    public Long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Long errorCount) {
        this.errorCount = errorCount;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getLocalAddr() {
        return localAddr;
    }

    public void setLocalAddr(String localAddr) {
        this.localAddr = localAddr;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public static MetricsBased convertMetricsBased(FluxInfoVO flux) {
        formatMetrics(flux);
        final MetricsBased metricsBased = new MetricsBased();
        metricsBased.setUuid(flux.getUuid());
        metricsBased.setAppName(flux.getAppName());
        metricsBased.setClusterTest(flux.getPtFlag());
        metricsBased.setEvent(flux.getEvent());
        metricsBased.setEntry("true".equals(flux.getEntryFlag()));
        metricsBased.setType(flux.getType());
        metricsBased.setCallEvent(flux.getCallEvent());
        metricsBased.setCallType(flux.getCallType());
        metricsBased.setTimestamp(flux.getTimestamp());
        metricsBased.setTotalCount(flux.getTotalCount());
        metricsBased.setSuccessCount(flux.getSuccessCount());
        metricsBased.setFailureCount(flux.getFailureCount());
        metricsBased.setHitCount(flux.getHitCount());
        metricsBased.setTotalRt(flux.getTotalRt());
        metricsBased.setQps(BigDecimal.valueOf(flux.getQps()));
        metricsBased.setInterval(flux.getInterval());
        metricsBased.setAgentId(flux.getAgentId());
        metricsBased.setTraceId(flux.getTraceId());

        return metricsBased;
    }

    public static void formatMetrics(FluxInfoVO fluxInfoVO) {
        if ("entry-ibmmq".equals(fluxInfoVO.getType())) {
            String event = fluxInfoVO.getEvent();
            if (StringUtils.isNotEmpty(event)) {
                if (event.startsWith("topic://")) {
                    event = event.substring(8);
                }
            }
            fluxInfoVO.setEvent(event);
        }
        if ("call-ibmmq".equals(fluxInfoVO.getCallType())) {
            String callEvent = fluxInfoVO.getCallEvent();
            if (StringUtils.isNotEmpty(callEvent)) {
                if (callEvent.startsWith("topic://")) {
                    callEvent = callEvent.substring(8);
                }
            }
            fluxInfoVO.setCallEvent(callEvent);
        }
        if ("entry-http".equals(fluxInfoVO.getType())) {
            String event = fluxInfoVO.getEvent();
            String newEvent = ApiProcessor.merge(fluxInfoVO.getAppName(), event, "");
            fluxInfoVO.setEvent(newEvent);
        }
        if (fluxInfoVO.getPtFlag()) {
            if ("entry-rocketmq".equals(fluxInfoVO.getType())
                    || "entry-ibmmq".equals(fluxInfoVO.getType())
                    || "entry-kafka".equals(fluxInfoVO.getType())
                    || "entry-rabbitmq".equals(fluxInfoVO.getType())
                    || "entry-activemq".equals(fluxInfoVO.getType())) {
                String event = fluxInfoVO.getEvent();
                fluxInfoVO.setEvent(TopicFormatUtils.replaceAll(event));
            }
            if ("call-rocketmq".equals(fluxInfoVO.getCallType())
                    || "call-ibmmq".equals(fluxInfoVO.getCallType())
                    || "call-rabbitmq".equals(fluxInfoVO.getCallType())
                    || "call-kafka".equals(fluxInfoVO.getCallType())
                    || "call-activemq".equals(fluxInfoVO.getCallType())) {
                String callEvent = fluxInfoVO.getCallEvent();
                fluxInfoVO.setCallEvent(TopicFormatUtils.replaceAll(callEvent));
            }
        }
    }
}
