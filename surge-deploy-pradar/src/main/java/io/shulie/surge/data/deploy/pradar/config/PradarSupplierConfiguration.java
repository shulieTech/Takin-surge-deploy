package io.shulie.surge.data.deploy.pradar.config;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.pamirs.pradar.log.parser.DataType;
import io.shulie.surge.data.JettySupplier;
import io.shulie.surge.data.JettySupplierSpec;
import io.shulie.surge.data.common.aggregation.Scheduler;
import io.shulie.surge.data.deploy.pradar.agg.E2ETraceMetricsAggarator;
import io.shulie.surge.data.deploy.pradar.agg.TraceMetricsAggarator;
import io.shulie.surge.data.deploy.pradar.collector.OutputCollector;
import io.shulie.surge.data.deploy.pradar.common.AffinityUtil;
import io.shulie.surge.data.deploy.pradar.common.EagleLoader;
import io.shulie.surge.data.deploy.pradar.common.ParamUtil;
import io.shulie.surge.data.deploy.pradar.common.RuleLoader;
import io.shulie.surge.data.deploy.pradar.digester.*;
import io.shulie.surge.data.deploy.pradar.servlet.EngineDataWriteServlet;
import io.shulie.surge.data.deploy.pradar.servlet.HealthCheckServlet;
import io.shulie.surge.data.deploy.pradar.servlet.LogWriteServlet;
import io.shulie.surge.data.runtime.common.DataBootstrap;
import io.shulie.surge.data.runtime.common.DataRuntime;
import io.shulie.surge.data.runtime.common.remote.impl.RemoteZkModule;
import io.shulie.surge.data.runtime.digest.DataDigester;
import io.shulie.surge.data.runtime.module.ZooKeeperClientModule;
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
import net.openhft.affinity.AffinityLock;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Map;
import java.util.Objects;

/**
 * pradar supplier配置
 *
 * @author vincent
 * @date 2022/11/14 17:21
 **/
public class PradarSupplierConfiguration implements PradarConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(PradarSupplierConfiguration.class);
    protected Map<String, String> netMap;
    protected Map<String, String> hostNameMap;
    protected String host = "";
    protected String work = "";
    protected Map<String, String> serverPortsMap = Maps.newHashMap();
    protected String dataSourceType;
    protected boolean registerZk;
    protected boolean generalVersion;
    protected int coreSize;
    private boolean httpEnabled;
    protected boolean openMqConsumer;
    protected int workerPort;
    protected String taskId;
    protected boolean affinityLockEnabled;
    /**
     * trace指标聚合器
     */
    @Inject
    protected TraceMetricsAggarator traceMetricsAggarator;
    /**
     * E2E指标聚合器
     */
    @Inject
    protected E2ETraceMetricsAggarator e2eTraceMetricsAggarator;
    /**
     * 边读取器
     */
    @Inject
    protected EagleLoader eagleLoader;
    /**
     * 断言规则
     */
    @Inject
    protected RuleLoader ruleLoader;

    /**
     * 输出器
     */
    protected OutputCollector outputCollector;

    /**
     * 参数初始化
     *
     * @param args
     */
    public void initArgs(Map<String, ?> args) {
        //移除无关参数
        args.remove(ParamUtil.WORKERS);
        taskId = Objects.toString(args.get(ParamUtil.TASK_ID));
        String netMapStr = Objects.toString(args.get(ParamUtil.NET), "");
        if (null != netMapStr && StringUtils.isNotBlank(netMapStr)) {
            this.netMap = JSON.parseObject(netMapStr, Map.class);
        }
        String hostNameMapStr = Objects.toString(args.get(ParamUtil.HOSTNAME), "");
        if (null != hostNameMapStr && StringUtils.isNotBlank(hostNameMapStr)) {
            this.hostNameMap = JSON.parseObject(hostNameMapStr, Map.class);
        }
        String serverPortsMapStr = Objects.toString(args.get(ParamUtil.PORTS), "");
        if (null != serverPortsMapStr && StringUtils.isNotBlank(serverPortsMapStr)) {
            this.serverPortsMap = JSON.parseObject(serverPortsMapStr, Map.class);
        }
        String registerZkStr = Objects.toString(args.get(ParamUtil.REGISTERZK), "");
        this.registerZk = CommonStat.TRUE.equals(String.valueOf(registerZkStr)) ? true : false;
        this.coreSize = NumberUtils.toInt(Objects.toString(args.get(ParamUtil.CORE_SIZE), ""), 8);
        this.dataSourceType = Objects.toString(args.get(ParamUtil.DATA_SOURCE_TYPE), "");
        this.openMqConsumer = Objects.isNull(args.get(ParamUtil.MQConsumer)) ? false : true;

        if (null != host) {
            this.host = Objects.toString(args.get(ParamUtil.HOST), "");
        }
        if (null != work) {
            this.work = Objects.toString(args.get(ParamUtil.WORK), "");
        }
        String httpEnabledStr = Objects.toString(args.get(ParamUtil.HTTP), "");

        this.workerPort = NumberUtils.toInt(Objects.toString(args.get("workerPort"), ""));
        this.httpEnabled = CommonStat.TRUE.equals(httpEnabledStr) ? true : false;
        this.registerZk = CommonStat.TRUE.equals(Objects.toString(args.get(ParamUtil.REGISTERZK), "")) ? true : false;
        this.generalVersion = CommonStat.TRUE.equals(Objects.toString(args.get(ParamUtil.GENERAL_VERSION), "")) ? true : false;
        this.affinityLockEnabled = CommonStat.TRUE.equals(Objects.toString(args.get(ParamUtil.AffinityLock), "")) ? true : false;
    }

    /**
     * 装载module
     *
     * @param bootstrap
     */
    @Override
    public void install(DataBootstrap bootstrap) {
        bootstrap.install(new PradarModule(workerPort), new NettyRemotingModule(), new InfluxDBModule(), new ClickHouseModule(), new ClickHouseShardModule(), new MysqlModule(), new ZooKeeperClientModule(), new RemoteZkModule());
    }

    /**
     * 运行时启动后初始化
     *
     * @param dataRuntime
     */
    @Override
    public void doAfterInit(DataRuntime dataRuntime) throws Exception {
        NettyRemotingSupplier nettyRemotingSupplier = buildSupplier(dataRuntime, true);
        Injector injector = dataRuntime.getInstance(Injector.class);
        injector.injectMembers(this);
        /**
         * 初始化metrics聚合任务。此处注入和diggest同一个对象
         */
        if (!generalVersion) {
            traceMetricsAggarator.init(new Scheduler(1), outputCollector);
        }
        e2eTraceMetricsAggarator.init(new Scheduler(1), outputCollector);
        // 初始化边缓存
        eagleLoader.init();
        ruleLoader.init();
        nettyRemotingSupplier.start();

        AffinityLock affinityLock = null;
        if (affinityLockEnabled) {
            affinityLock = AffinityUtil.acquireLock(NumberUtils.toInt(taskId));
            logger.info("当前Topology TaskId={},当前进程={},绑定的cpu Id={}", taskId, getProcessID(), affinityLock.cpuId());
        }

        // 确认是否开始Http服务
        if (httpEnabled) {
            //启动jetty
            JettySupplier jettySupplier = buildJettySupplier(dataRuntime, true);
            jettySupplier.start();
        }

    }

    protected int getProcessID() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        System.out.println(runtimeMXBean.getName());
        return Integer.valueOf(runtimeMXBean.getName().split("@")[0]).intValue();
    }

    /**
     * 设置收集器
     *
     * @param outputCollector
     */
    @Override
    public void collector(OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
    }

    /**
     * 创建订阅器
     *
     * @param dataRuntime
     * @throws Exception
     */
    protected NettyRemotingSupplier buildSupplier(DataRuntime dataRuntime, Boolean isDistributed) throws Exception {
        try {
            NettyRemotingSupplierSpec nettyRemotingSupplierSpec = new NettyRemotingSupplierSpec();
            nettyRemotingSupplierSpec.setNetMap(netMap);
            nettyRemotingSupplierSpec.setHostNameMap(hostNameMap);
            nettyRemotingSupplierSpec.setRegisterZk(registerZk);
            nettyRemotingSupplierSpec.setHost(host);
            nettyRemotingSupplierSpec.setWork(work);
            NettyRemotingSupplier nettyRemotingSupplier = dataRuntime.createGenericInstance(nettyRemotingSupplierSpec);


            Map<String, DataQueue> queueMap = Maps.newHashMap();
            queueMap.put(String.valueOf(DataType.TRACE_LOG), buildTraceProcessor(dataRuntime, isDistributed));
            queueMap.put(String.valueOf(DataType.MONITOR_LOG), buildMonitorProcessor(dataRuntime));
            queueMap.put(String.valueOf(DataType.AGENT_LOG), buildAgentLogProcessor(dataRuntime));

            nettyRemotingSupplier.setQueue(queueMap);
            nettyRemotingSupplier.setInputPortMap(serverPortsMap);
            nettyRemotingSupplier.setWork(work);
            return nettyRemotingSupplier;
        } catch (Throwable e) {
            logger.error("netty fail " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("netty fail");
        }
    }

    /**
     * 创建trace处理器
     *
     * @param dataRuntime
     * @return
     * @throws Exception
     */
    protected PradarProcessor buildTraceProcessor(DataRuntime dataRuntime, boolean isDistributed) throws Exception {
        /**
         * storm消费trace日志
         */
        ProcessorConfigSpec<PradarProcessor> traceLogProcessorConfigSpec = new PradarProcessorConfigSpec();
        traceLogProcessorConfigSpec.setName("trace-log");
        traceLogProcessorConfigSpec.setDigesters(ArrayUtils.addAll(buildTraceLogDigesters(dataRuntime), isDistributed ? buildTraceLogComplexDigester(dataRuntime) : buildE2EProcessByStandaloneDigesters(dataRuntime)));
        traceLogProcessorConfigSpec.setExecuteSize(coreSize);
        return dataRuntime.createGenericInstance(traceLogProcessorConfigSpec);

    }

    /**
     * 创建monitor处理器
     *
     * @param dataRuntime
     * @return
     * @throws Exception
     */
    protected PradarProcessor buildMonitorProcessor(DataRuntime dataRuntime) throws Exception {
        /**
         * storm消费monitor日志
         */
        ProcessorConfigSpec<PradarProcessor> baseProcessorConfigSpec = new PradarProcessorConfigSpec();
        baseProcessorConfigSpec.setName("base");
        baseProcessorConfigSpec.setDigesters(buildMonitorDigesters(dataRuntime));
        baseProcessorConfigSpec.setExecuteSize(coreSize);
        return dataRuntime.createGenericInstance(baseProcessorConfigSpec);
    }

    /**
     * 创建agent日志处理器
     *
     * @param dataRuntime
     * @return
     * @throws Exception
     */
    protected PradarProcessor buildAgentLogProcessor(DataRuntime dataRuntime) throws Exception {
        ProcessorConfigSpec<PradarProcessor> agentProcessorConfigSpec = new PradarProcessorConfigSpec();
        agentProcessorConfigSpec.setName("agent-log");
        agentProcessorConfigSpec.setDigesters(buildAgentDigesters(dataRuntime));
        agentProcessorConfigSpec.setExecuteSize(coreSize);
        return dataRuntime.createGenericInstance(agentProcessorConfigSpec);
    }


    /**
     * 创建jetty订阅器
     *
     * @param dataRuntime
     * @throws Exception
     */
    private JettySupplier buildJettySupplier(DataRuntime dataRuntime, Boolean isDistributed) {
        try {
            JettySupplierSpec jettySupplierSpec = new JettySupplierSpec();
            JettySupplier jettySupplier = dataRuntime.createGenericInstance(jettySupplierSpec);

            /**
             * storm消费trace日志
             */
            ProcessorConfigSpec<PradarProcessor> traceLogProcessorConfigSpec = new PradarProcessorConfigSpec();
            traceLogProcessorConfigSpec.setName("trace-log");
            traceLogProcessorConfigSpec.setDigesters(ArrayUtils.addAll(buildTraceLogDigesters(dataRuntime), isDistributed ? buildTraceLogComplexDigester(dataRuntime) : buildE2EProcessByStandaloneDigesters(dataRuntime)));
            traceLogProcessorConfigSpec.setExecuteSize(coreSize);
            PradarProcessor traceLogProcessor = dataRuntime.createGenericInstance(traceLogProcessorConfigSpec);

            /**
             * storm消费monitor日志
             */
            ProcessorConfigSpec<PradarProcessor> baseProcessorConfigSpec = new PradarProcessorConfigSpec();
            baseProcessorConfigSpec.setName("base");
            baseProcessorConfigSpec.setDigesters(buildMonitorDigesters(dataRuntime));
            baseProcessorConfigSpec.setExecuteSize(coreSize);
            PradarProcessor baseProcessor = dataRuntime.createGenericInstance(baseProcessorConfigSpec);

            /**
             * agent日志
             */
            ProcessorConfigSpec<PradarProcessor> agentProcessorConfigSpec = new PradarProcessorConfigSpec();
            agentProcessorConfigSpec.setName("agent-log");
            agentProcessorConfigSpec.setDigesters(buildAgentDigesters(dataRuntime));
            agentProcessorConfigSpec.setExecuteSize(coreSize);
            PradarProcessor agentProcessor = dataRuntime.createGenericInstance(agentProcessorConfigSpec);


            Map<String, DataQueue> queueMap = Maps.newHashMap();
            queueMap.put(String.valueOf(DataType.TRACE_LOG), traceLogProcessor);
            queueMap.put(String.valueOf(DataType.MONITOR_LOG), baseProcessor);
            queueMap.put(String.valueOf(DataType.AGENT_LOG), agentProcessor);

            jettySupplier.setQueue(queueMap);
            jettySupplier.addServlet("/takin-surge/log/engine/metrics/upload", dataRuntime.getInstance(EngineDataWriteServlet.class));
            jettySupplier.addServlet("/takin-surge/health", dataRuntime.getInstance(HealthCheckServlet.class));
            LogWriteServlet logWriteServlet = dataRuntime.getInstance(LogWriteServlet.class);
            logWriteServlet.setQueueMap(queueMap);
            jettySupplier.addServlet("/takin-surge/log/link/upload", logWriteServlet);
            return jettySupplier;
        } catch (Throwable e) {
            logger.error("jetty fail " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("jetty fail");
        }
    }

    /**
     * trace 日志 构建基础的消费digester ,可使用在单节点启动和storm集群中
     *
     * @param dataRuntime
     * @return
     */
    private DataDigester[] buildTraceLogDigesters(DataRuntime dataRuntime) {
        LogDigester logDigester = dataRuntime.getInstance(LogDigester.class);
        logDigester.setDataSourceType(this.dataSourceType);
        if (openMqConsumer) {
            RocketmqDigester rocketmqDigester = dataRuntime.getInstance(RocketmqDigester.class);
            return new DataDigester[]{logDigester, rocketmqDigester};
        }
        return new DataDigester[]{logDigester};
    }

    /**
     * 基础cpu、load处理
     *
     * @param dataRuntime
     * @return
     */
    private DataDigester[] buildMonitorDigesters(DataRuntime dataRuntime) {
        BaseDataDigester baseDataDigester = dataRuntime.getInstance(BaseDataDigester.class);
        return new DataDigester[]{baseDataDigester};
    }

    /**
     * agent日志处理
     *
     * @param dataRuntime
     * @return
     */
    private DataDigester[] buildAgentDigesters(DataRuntime dataRuntime) {
        DataDigester agentInfoDigester = dataRuntime.getInstance(AgentInfoDigester.class);
        return new DataDigester[]{agentInfoDigester};
    }

    /**
     * 用于分片任务计算
     *
     * @param dataRuntime
     * @return
     */
    private DataDigester[] buildTraceLogComplexDigester(DataRuntime dataRuntime) {
        TraceMetricsDigester traceMetricsDigester = dataRuntime.getInstance(TraceMetricsDigester.class);
        traceMetricsDigester.init();
        return new DataDigester[]{traceMetricsDigester};
    }

    /**
     * 单机模式E2E计算任务
     *
     * @param dataRuntime
     * @return
     */
    private DataDigester[] buildE2EProcessByStandaloneDigesters(DataRuntime dataRuntime) {
        E2EDefaultDigester e2eDefaultDigester = dataRuntime.getInstance(E2EDefaultDigester.class);
        e2eDefaultDigester.init();
        return new DataDigester[]{e2eDefaultDigester};
    }

    /**
     * 停止运行。如果已经停止，则应该不会有任何效果。
     * 建议实现使用同步方式执行。
     */
    @Override
    public void stop() throws Exception {

    }
}
