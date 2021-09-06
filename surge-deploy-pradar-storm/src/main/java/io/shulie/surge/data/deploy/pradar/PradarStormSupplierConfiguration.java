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

package io.shulie.surge.data.deploy.pradar;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.pamirs.pradar.log.parser.DataType;
import io.shulie.surge.data.deploy.pradar.common.DataBootstrapEnhancer;
import io.shulie.surge.data.deploy.pradar.config.PradarModule;
import io.shulie.surge.data.deploy.pradar.config.PradarProcessor;
import io.shulie.surge.data.deploy.pradar.config.PradarProcessorConfigSpec;
import io.shulie.surge.data.deploy.pradar.config.PradarSupplierConfiguration;
import io.shulie.surge.data.deploy.pradar.digester.E2EAssertionDigester;
import io.shulie.surge.data.deploy.pradar.digester.E2EDefaultDigester;
import io.shulie.surge.data.deploy.pradar.digester.MetricsReduceDigester;
import io.shulie.surge.data.deploy.pradar.digester.TraceMetricsDiggester;
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
 *
 * @author vincent
 */

public class PradarStormSupplierConfiguration {
    private static Logger logger = LoggerFactory.getLogger(PradarStormSupplierConfiguration.class);

    private Integer workPort;
    private Map<String, String> netMap;
    private Map<String, String> hostNameMap;
    private Map<String, String> serverPortsMap = Maps.newHashMap();
    private String dataSourceType;
    private boolean registerZk;
    private boolean generalVersion;
    private int coreSize;

    public PradarStormSupplierConfiguration(Map<String, String> netMap, Map<String, String> hostNameMap,
                                            boolean registerZk, int coreSize, String dataSourceType,
                                            Map<String, String> serverPortsMap, boolean generalVersion) {
        this.netMap = netMap;
        this.hostNameMap = hostNameMap;
        this.registerZk = registerZk;
        this.coreSize = coreSize;
        this.dataSourceType = dataSourceType;
        this.serverPortsMap = serverPortsMap;
        this.generalVersion = generalVersion;
    }


    public PradarStormSupplierConfiguration(
            Object netMapStr,
            Object hostNameMapStr,
            Object registerZk,
            Object coreSize,
            Object dataSourceType,
            Object serverPortsMapStr) {
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
        this.coreSize = Integer.valueOf(Objects.toString(coreSize));
        this.dataSourceType = Objects.toString(dataSourceType);
    }

    /**
     * 创建订阅器
     *
     * @param dataRuntime
     * @throws Exception
     */
    public NettyRemotingSupplier buildSupplier(DataRuntime dataRuntime, Boolean isDistributed) {
        try {
            PradarSupplierConfiguration conf = new PradarSupplierConfiguration("", dataSourceType);
            NettyRemotingSupplierSpec nettyRemotingSupplierSpec = new NettyRemotingSupplierSpec();
            nettyRemotingSupplierSpec.setNetMap(netMap);
            nettyRemotingSupplierSpec.setHostNameMap(hostNameMap);
            nettyRemotingSupplierSpec.setRegisterZk(registerZk);
            NettyRemotingSupplier nettyRemotingSupplier = dataRuntime.createGenericInstance(nettyRemotingSupplierSpec);

            /**
             * storm消费trace日志
             */
            ProcessorConfigSpec<PradarProcessor> traceLogProcessorConfigSpec = new PradarProcessorConfigSpec();
            traceLogProcessorConfigSpec.setName("trace-log");
            traceLogProcessorConfigSpec.setDigesters(
                    ArrayUtils.addAll(conf.buildTraceLogProcess(dataRuntime),
                            isDistributed ? buildTraceLogComplexProcess(dataRuntime) : buildE2EProcessByStandadlone(dataRuntime)));
            traceLogProcessorConfigSpec.setExecuteSize(coreSize);
            PradarProcessor traceLogProcessor = dataRuntime.createGenericInstance(traceLogProcessorConfigSpec);

            /**
             * storm消费monitor日志
             */
            ProcessorConfigSpec<PradarProcessor> baseProcessorConfigSpec = new PradarProcessorConfigSpec();
            baseProcessorConfigSpec.setName("base");
            baseProcessorConfigSpec.setDigesters(conf.buildMonitorProcess(dataRuntime));
            baseProcessorConfigSpec.setExecuteSize(coreSize);
            PradarProcessor baseProcessor = dataRuntime.createGenericInstance(baseProcessorConfigSpec);

            Map<String, DataQueue> queueMap = Maps.newHashMap();
            queueMap.put(String.valueOf(DataType.TRACE_LOG), traceLogProcessor);
            queueMap.put(String.valueOf(DataType.MONITOR_LOG), baseProcessor);

            nettyRemotingSupplier.setQueue(queueMap);
            nettyRemotingSupplier.setInputPortMap(serverPortsMap);

            return nettyRemotingSupplier;
        } catch (Throwable e) {
            logger.error("netty fail " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("netty fail");
        }
    }

    /**
     * 用于分片任务计算
     *
     * @param dataRuntime
     * @return
     */
    public DataDigester[] buildTraceLogComplexProcess(DataRuntime dataRuntime) {
        if (!generalVersion) {
            TraceMetricsDiggester traceMetricsDiggester = dataRuntime.getInstance(TraceMetricsDiggester.class);
            E2EAssertionDigester e2eAssertionDigester = dataRuntime.getInstance(E2EAssertionDigester.class);
            e2eAssertionDigester.init();
            return new DataDigester[]{traceMetricsDiggester, e2eAssertionDigester};
        } else {
            E2EAssertionDigester e2eAssertionDigester = dataRuntime.getInstance(E2EAssertionDigester.class);
            e2eAssertionDigester.init();
            return new DataDigester[]{e2eAssertionDigester};
        }
    }

    /**
     * 单机模式E2E计算任务
     *
     * @param dataRuntime
     * @return
     */
    public DataDigester[] buildE2EProcessByStandadlone(DataRuntime dataRuntime) {
        E2EDefaultDigester e2eDefaultDigester = dataRuntime.getInstance(E2EDefaultDigester.class);
        e2eDefaultDigester.init();
        return new DataDigester[]{e2eDefaultDigester};
    }

    /**
     * 用于分片的metrics计算
     *
     * @param dataRuntime
     * @return
     */
    public DataDigester[] buildMetricsProcess(DataRuntime dataRuntime) {
        MetricsReduceDigester metricsReduceDigester = dataRuntime.getInstance(MetricsReduceDigester.class);
        return new DataDigester[]{metricsReduceDigester};
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
}
