/* * Copyright 2021 Shulie Technology, Co.Ltd * Email: shulie@shulie.io * Licensed under the Apache License, Version 2.0 (the "License"); * you may not use this file except in compliance with the License. * You may obtain a copy of the License at * *      http://www.apache.org/licenses/LICENSE-2.0 * * Unless required by applicable law or agreed to in writing, software * distributed under the License is distributed on an "AS IS" BASIS, * See the License for the specific language governing permissions and * limitations under the License. */package io.shulie.surge.data.deploy.pradar.digester;import com.google.common.collect.Maps;import com.google.inject.Inject;import com.google.inject.Singleton;import com.google.inject.name.Named;import com.pamirs.pradar.log.parser.monitor.MonitorBased;import io.shulie.surge.data.common.utils.FormatUtils;import io.shulie.surge.data.runtime.common.remote.DefaultValue;import io.shulie.surge.data.runtime.common.remote.Remote;import io.shulie.surge.data.runtime.digest.DataDigester;import io.shulie.surge.data.runtime.digest.DigestContext;import io.shulie.surge.data.sink.influxdb.InfluxDBSupport;import org.apache.commons.lang3.StringUtils;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import java.util.Map;/** * @author xingchen * @className BaseDataDigester * @package com.pamirs.pradar.digest * @description 基础信息处理digester */@Singletonpublic class BaseDataDigester implements DataDigester<MonitorBased> {    private static final Logger logger = LoggerFactory.getLogger(BaseDataDigester.class);    @Inject    @Named("config.influxdb.measurement")    private String measurement;    @Inject    @Named("config.influxdb.database.monitor")    private String monitorDataBase;    @Inject    @DefaultValue("false")    @Named("/pradar/config/rt/monitroDisable")    private Remote<Boolean> monitroDisable;    @Inject    private InfluxDBSupport influxDbSupport;    @Override    public void digest(DigestContext<MonitorBased> context) {        try {            if (monitroDisable.get()) {                return;            }            MonitorBased monitorBased = context.getContent();            if (monitorBased == null) {                logger.warn("parse monitorBased is null " + context.getContent());                return;            }            Map<String, Object> fields = Maps.newHashMap();            fields.put("app_ip", monitorBased.getHostIp());            fields.put("app_name", monitorBased.getAppName());            fields.put("cpu_rate", monitorBased.getCpuUsage());            fields.put("cpu_load", monitorBased.getCpuLoad1());            fields.put("mem_rate", monitorBased.getMemoryUsage());            fields.put("iowait", monitorBased.getIoWait());            fields.put("net_bandwidth_rate", monitorBased.getNetworkUsage());            fields.put("net_bandwidth", monitorBased.getNetworkSpeed());            fields.put("cpu_cores", monitorBased.getCpuNum());            fields.put("disk", monitorBased.getTotalDisk());            fields.put("memory", monitorBased.getTotalMemory());            fields.put("is_container_flag", monitorBased.getIsContainerFlag());            Map<String, String> tags = Maps.newHashMap();            tags.put("tag_app_ip", monitorBased.getHostIp());            if (StringUtils.isNotBlank(monitorBased.getAgentId())) {                fields.put("agent_id", monitorBased.getAgentId());                tags.put("tag_agent_id", monitorBased.getAgentId());            }            tags.put("tag_app_name", monitorBased.getAppName());            long time = Long.valueOf(monitorBased.getTimestamp()) * 1000;            // 加一个时间字符。便于查看日志            fields.put("log_time", FormatUtils.toDateTimeSecondString(time));            /**             * 解析数据处理             */            influxDbSupport.write(monitorDataBase, measurement, tags, fields, time);        } catch (Throwable e) {            logger.error("fail to write influxdb, log: " + context.getContent() + ", error:" + e.getMessage());        }    }    @Override    public void stop() {        try {            influxDbSupport.stop();        } catch (Throwable e) {            logger.error("close fail");        }    }    @Override    public int threadCount() {        return 1;    }}