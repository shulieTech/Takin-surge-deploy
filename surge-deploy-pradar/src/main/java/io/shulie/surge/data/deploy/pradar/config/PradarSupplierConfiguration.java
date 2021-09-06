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

package io.shulie.surge.data.deploy.pradar.config;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.pamirs.pradar.log.parser.DataType;
import io.shulie.surge.data.deploy.pradar.common.DataBootstrapEnhancer;
import io.shulie.surge.data.deploy.pradar.common.ParamUtil;
import io.shulie.surge.data.deploy.pradar.digester.BaseDataDigester;
import io.shulie.surge.data.deploy.pradar.digester.LogDigester;
import io.shulie.surge.data.deploy.pradar.digester.MetricsDigester;
import io.shulie.surge.data.runtime.common.DataBootstrap;
import io.shulie.surge.data.runtime.common.DataRuntime;
import io.shulie.surge.data.runtime.digest.DataDigester;
import io.shulie.surge.data.runtime.processor.DataQueue;
import io.shulie.surge.data.runtime.processor.ProcessorConfigSpec;
import io.shulie.surge.data.sink.clickhouse.ClickHouseModule;
import io.shulie.surge.data.sink.clickhouse.ClickHouseShardModule;
import io.shulie.surge.data.sink.influxdb.InfluxDBModule;
import io.shulie.surge.data.sink.mysql.MysqlModule;
import io.shulie.surge.data.suppliers.nettyremoting.NettyRemotingModule;
import io.shulie.surge.data.suppliers.nettyremoting.NettyRemotingSupplier;
import io.shulie.surge.data.suppliers.nettyremoting.NettyRemotingSupplierSpec;
import io.shulie.surge.deploy.pradar.common.CommonStat;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * pradar supplier初始化
 */
public class PradarSupplierConfiguration {
    private static Logger logger = LoggerFactory.getLogger(PradarSupplierConfiguration.class);

    private Integer workPort;
    private String dataSourceType;
    private Map<String, String> netMap;
    private Map<String, String> hostNameMap;
    private Map<String, String> serverPortsMap = Maps.newHashMap();
    private boolean registerZk;
    private boolean generalVersion;

    private int coreSize = 0;

    public PradarSupplierConfiguration() {
    }

    public PradarSupplierConfiguration(Integer workPort) {
        this.workPort = workPort;
    }

    public PradarSupplierConfiguration(String netMapStr, String dataSourceType) {
        if (null != netMapStr && StringUtils.isNotBlank(netMapStr)) {
            this.netMap = JSON.parseObject(netMapStr, Map.class);
        }
        this.dataSourceType = dataSourceType;
    }


    public PradarSupplierConfiguration(Integer workPort, Object netMapStr) {
        this.workPort = workPort;
        if (null != netMapStr && StringUtils.isNotBlank(String.valueOf(netMapStr))) {
            this.netMap = JSON.parseObject(String.valueOf(netMapStr), Map.class);
        }
    }

    public PradarSupplierConfiguration(Integer workPort,
                                       Object netMapStr,
                                       Object hostNameMapStr,
                                       Object registerZk,
                                       Object coreSize,
                                       Object dataSourceType,
                                       Object serverPortsMapStr,
                                       Object generalVersion) {
        this.workPort = workPort;
        if (null != netMapStr && StringUtils.isNotBlank(String.valueOf(netMapStr))) {
            this.netMap = JSON.parseObject(String.valueOf(netMapStr), Map.class);
        }
        if (null != hostNameMapStr && StringUtils.isNotBlank(String.valueOf(hostNameMapStr))) {
            this.hostNameMap = JSON.parseObject(String.valueOf(hostNameMapStr), Map.class);
        }
        if (null != serverPortsMapStr && StringUtils.isNotBlank(String.valueOf(serverPortsMapStr))) {
            this.serverPortsMap = JSON.parseObject(String.valueOf(serverPortsMapStr), Map.class);
        }
        this.registerZk = CommonStat.TRUE.equals(String.valueOf(registerZk)) ? true : false;
        this.generalVersion = CommonStat.TRUE.equals(String.valueOf(generalVersion)) ? true : false;
        this.coreSize = Integer.valueOf(Objects.toString(coreSize));
        this.dataSourceType = Objects.toString(dataSourceType);
    }

    /**
     * 初始化initDataRuntime
     *
     * @throws Exception
     */
    public DataRuntime initDataRuntime() {
        DataBootstrap bootstrap = DataBootstrap.create("deploy.properties", "pradar");
        DataBootstrapEnhancer.enhancer(bootstrap);
        bootstrap.install(
                new PradarModule(workPort),
                new NettyRemotingModule(),
                new InfluxDBModule(),
                new ClickHouseModule(),
                new ClickHouseShardModule(),
                new MysqlModule());
        DataRuntime dataRuntime = bootstrap.startRuntime();
        return dataRuntime;
    }

    /**
     * 初始化
     *
     * @throws Exception
     */
    public void init() throws Exception {
        buildSupplier(initDataRuntime()).start();
    }

    /**
     * trace 日志 构建基础的消费digester ,可使用在单节点启动和storm集群中
     *
     * @param dataRuntime
     * @return
     */
    public DataDigester[] buildTraceLogProcess(DataRuntime dataRuntime) {
        LogDigester logDigester = dataRuntime.getInstance(LogDigester.class);
        logDigester.setDataSourceType(this.dataSourceType);
        return new DataDigester[]{logDigester};
    }


    /**
     * 基础cpu、load处理
     *
     * @param dataRuntime
     * @return
     */
    public DataDigester[] buildMonitorProcess(DataRuntime dataRuntime) {
        BaseDataDigester baseDataDigester = dataRuntime.getInstance(BaseDataDigester.class);
        return new DataDigester[]{baseDataDigester};
    }

    /**
     * 单节点的metrics计算
     *
     * @param dataRuntime
     * @return
     */
    public DataDigester[] buildMetricsProcess(DataRuntime dataRuntime) {
        MetricsDigester metricsDigester = dataRuntime.getInstance(MetricsDigester.class);
        return new DataDigester[]{metricsDigester};
    }

    /**
     * 创建订阅器
     *
     * @param dataRuntime
     * @return
     * @throws Exception
     */
    public NettyRemotingSupplier buildSupplier(DataRuntime dataRuntime) {
        try {
            NettyRemotingSupplierSpec nettyRemotingSupplierSpec = new NettyRemotingSupplierSpec();
            nettyRemotingSupplierSpec.setNetMap(netMap);
            nettyRemotingSupplierSpec.setHostNameMap(hostNameMap);
            nettyRemotingSupplierSpec.setRegisterZk(true);
            NettyRemotingSupplier nettyRemotingSupplier = dataRuntime.createGenericInstance(nettyRemotingSupplierSpec);

            /**
             * storm消费trace日志
             */
            ProcessorConfigSpec<PradarProcessor> traceLogProcessorConfigSpec = new PradarProcessorConfigSpec();
            traceLogProcessorConfigSpec.setName("trace-log");
            traceLogProcessorConfigSpec.setDigesters(ArrayUtils.addAll(buildTraceLogProcess(dataRuntime)));
            traceLogProcessorConfigSpec.setExecuteSize(coreSize);
            PradarProcessor traceLogProcessor = dataRuntime.createGenericInstance(traceLogProcessorConfigSpec);

            /**
             * storm消费monitor日志
             */
            ProcessorConfigSpec<PradarProcessor> baseProcessorConfigSpec = new PradarProcessorConfigSpec();
            baseProcessorConfigSpec.setName("base");
            baseProcessorConfigSpec.setDigesters(buildMonitorProcess(dataRuntime));
            baseProcessorConfigSpec.setExecuteSize(coreSize);
            PradarProcessor baseProcessor = dataRuntime.createGenericInstance(baseProcessorConfigSpec);

            Map<String, DataQueue> queueMap = Maps.newHashMap();
            queueMap.put(String.valueOf(DataType.TRACE_LOG), traceLogProcessor);
            queueMap.put(String.valueOf(DataType.MONITOR_LOG), baseProcessor);
            nettyRemotingSupplier.setQueue(queueMap);
            return nettyRemotingSupplier;
        } catch (Throwable e) {
            logger.error("netty fail " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("netty fail");
        }
    }


    public boolean isRegisterZk() {
        return registerZk;
    }

    public int getCoreSize() {
        return coreSize;
    }

    public void setCoreSize(int coreSize) {
        this.coreSize = coreSize;
    }

    public Integer getWorkPort() {
        return workPort;
    }

    public void setWorkPort(Integer workPort) {
        this.workPort = workPort;
    }

    public Map<String, String> getNetMap() {
        return netMap;
    }

    public void setNetMap(Map<String, String> netMap) {
        this.netMap = netMap;
    }

    public Map<String, String> getHostNameMap() {
        return hostNameMap;
    }

    public void setHostNameMap(Map<String, String> hostNameMap) {
        this.hostNameMap = hostNameMap;
    }

    public void setRegisterZk(boolean registerZk) {
        this.registerZk = registerZk;
    }

    public String getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    public Map<String, String> getServerPortsMap() {
        return serverPortsMap;
    }

    public boolean isGeneralVersion() {
        return generalVersion;
    }

    /**
     * 简单使用 启动日志数据写入, 此处功能默认数据链路数据存储到mysql
     * java -cp xxx.jar io.shulie.surge.data.deploy.pradar.config.PradarSupplierConfiguration
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> inputMap = Maps.newHashMap();
        ParamUtil.parseInputParam(inputMap, args);
        // 默认将数据写入到mysql
        inputMap.put(ParamUtil.DATA_SOURCE_TYPE, CommonStat.MYSQL);

        PradarSupplierConfiguration pradarSupplierConfiguration =
                new PradarSupplierConfiguration(inputMap.get(ParamUtil.NET),
                        inputMap.get(ParamUtil.DATA_SOURCE_TYPE));
        pradarSupplierConfiguration.init();
    }
}
