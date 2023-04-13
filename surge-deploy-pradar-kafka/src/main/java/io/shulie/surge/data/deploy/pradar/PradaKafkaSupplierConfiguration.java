package io.shulie.surge.data.deploy.pradar;

import com.google.inject.Injector;
import io.shulie.surge.data.common.aggregation.Scheduler;
import io.shulie.surge.data.deploy.pradar.common.AffinityUtil;
import io.shulie.surge.data.deploy.pradar.config.PradarModule;
import io.shulie.surge.data.deploy.pradar.config.PradarSupplierConfiguration;
import io.shulie.surge.data.runtime.common.DataBootstrap;
import io.shulie.surge.data.runtime.common.DataRuntime;
import io.shulie.surge.data.runtime.common.remote.impl.RemoteNacosModule;
import io.shulie.surge.data.runtime.module.NacosClientModule;
import io.shulie.surge.data.sink.clickhouse.ClickHouseModule;
import io.shulie.surge.data.sink.clickhouse.ClickHouseShardModule;
import io.shulie.surge.data.sink.influxdb.InfluxDBModule;
import io.shulie.surge.data.sink.mysql.MysqlModule;
import io.shulie.surge.data.suppliers.kafka.KafkaModule;
import io.shulie.surge.data.suppliers.kafka.KafkaSupplier;
import io.shulie.surge.data.suppliers.kafka.KafkaSupplierSpec;
import net.openhft.affinity.AffinityLock;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author vincent
 * @date 2022/11/15 15:58
 **/
public class PradaKafkaSupplierConfiguration extends PradarSupplierConfiguration {
    private static final long serialVersionUID = -5439773941738130747L;

    private static final Logger logger = LoggerFactory.getLogger(PradaKafkaSupplierConfiguration.class);

    private static final String TRACE_TOPIC = "stress-test-agent-trace";
    private static final String PRESURCE_ENGINE_TRACE_TOPIC = "stress-test-pressure-engine-trace-log";
    private static final String BASE_TOPIC = "stress-test-agent-monitor";
    private static final String AGENT_LOG_TOPIC = "stress-test-agent-log";

    private static final String TRACE_REDUCE_TOPIC = "stress-test-trace-reduce-metrics";

    private static final String E2E_TOPIC = "stress-test-e2e-metrics";


    private String bootstrap;
    private String kafkaAuthFlag;
    private String securityProtocol;
    private String saslMechanism;
    private String saslJaasConfig;

    /**
     * 参数初始化
     *
     * @param args
     */
    @Override
    public void initArgs(Map<String, ?> args) {
        super.initArgs(args);
    }

    /**
     * 装载module
     *
     * @param bootstrap
     */
    @Override
    public void install(DataBootstrap bootstrap) {
        this.bootstrap = bootstrap.getProperties().getProperty("kafka.sdk.bootstrap", "192.168.1.92:9092");
        kafkaAuthFlag = bootstrap.getProperties().getProperty("kafka.auth.flag", "false");
        securityProtocol = bootstrap.getProperties().getProperty("security.protocol", "SASL_PLAINTEXT");
        saslMechanism = bootstrap.getProperties().getProperty("sasl.mechanism", "PLAIN");
        saslJaasConfig = bootstrap.getProperties().getProperty("sasl.jaas.config", "");
        bootstrap.install(
                new PradarModule(0),
                new KafkaModule(),
                new InfluxDBModule(),
                new ClickHouseModule(),
                new ClickHouseShardModule(),
                new MysqlModule(), new NacosClientModule(), new RemoteNacosModule());
    }

    /**
     * 运行时启动后初始化
     *
     * @param dataRuntime
     */
    @Override
    public void doAfterInit(DataRuntime dataRuntime) throws Exception {
        Injector injector = dataRuntime.getInstance(Injector.class);
        injector.injectMembers(this);
        KafkaSupplier kafkaTraceSupplier = buildTraceSupplier(dataRuntime, true);
        KafkaSupplier kafkaPressureEngineTraceSupplier = buildPressureEngineTraceSupplier(dataRuntime, true);
        KafkaSupplier kafkaBaseSupplier = buildBaseSupplier(dataRuntime, true);
        KafkaSupplier kafkaAgentLogSupplier = buildAgentLogSupplier(dataRuntime, true);


        /**
         * 初始化metrics聚合任务。此处注入和diggest同一个对象
         */
        if (!generalVersion) {
            buildTraceMetricsAggarator();
            buildE2eTraceMetricsAggarator();
        }
        // 初始化边缓存
        eagleLoader.init();
        ruleLoader.init();

        kafkaTraceSupplier.start();
        kafkaBaseSupplier.start();
        kafkaPressureEngineTraceSupplier.start();
        kafkaAgentLogSupplier.start();

        AffinityLock affinityLock = null;
        if (affinityLockEnabled) {
            affinityLock = AffinityUtil.acquireLock(NumberUtils.toInt(taskId));
            logger.info("当前Topology TaskId={},当前进程={},绑定的cpu Id={}", taskId, getProcessID(), affinityLock.cpuId());
        }
    }


    /**
     * trace metrics aggarator
     */
    protected void buildTraceMetricsAggarator() {
        traceMetricsAggarator.init(new Scheduler(1), new KafkaOutputCollector(bootstrap, TRACE_REDUCE_TOPIC,
                kafkaAuthFlag, securityProtocol, saslMechanism, saslJaasConfig));
    }

    /**
     * trace metrics aggarator
     */
    protected void buildE2eTraceMetricsAggarator() {
        e2eTraceMetricsAggarator.init(new Scheduler(1), new KafkaOutputCollector(bootstrap, E2E_TOPIC,
                kafkaAuthFlag, securityProtocol, saslMechanism, saslJaasConfig));
    }

    /**
     * 创建订阅器
     *
     * @param dataRuntime
     * @param isDistributed
     * @throws Exception
     */
    protected KafkaSupplier buildTraceSupplier(DataRuntime dataRuntime, Boolean isDistributed) throws Exception {
        try {
            KafkaSupplierSpec kafkaSupplierSpec = new KafkaSupplierSpec(bootstrap, TRACE_TOPIC, kafkaAuthFlag,
                    securityProtocol, saslMechanism, saslJaasConfig);
            KafkaSupplier kafkaSupplier = dataRuntime.createGenericInstance(kafkaSupplierSpec);
            kafkaSupplier.setQueue(buildTraceProcessor(dataRuntime, isDistributed, TRACE_TOPIC));
            return kafkaSupplier;
        } catch (Throwable e) {
            logger.error("KafkaSupplier fail " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("KafkaSupplier fail");
        }
    }

    /**
     * 创建订阅器
     *
     * @param dataRuntime
     * @param isDistributed
     * @throws Exception
     */
    protected KafkaSupplier buildPressureEngineTraceSupplier(DataRuntime dataRuntime, Boolean isDistributed) throws Exception {
        try {
            KafkaSupplierSpec kafkaSupplierSpec = new KafkaSupplierSpec(bootstrap, PRESURCE_ENGINE_TRACE_TOPIC,
                    kafkaAuthFlag, securityProtocol, saslMechanism, saslJaasConfig);
            KafkaSupplier kafkaSupplier = dataRuntime.createGenericInstance(kafkaSupplierSpec);
            kafkaSupplier.setQueue(buildTraceProcessor(dataRuntime, isDistributed, PRESURCE_ENGINE_TRACE_TOPIC));
            return kafkaSupplier;
        } catch (Throwable e) {
            logger.error("KafkaSupplier fail " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("KafkaSupplier fail");
        }
    }

    /**
     * 创建订阅器
     *
     * @param dataRuntime
     * @param isDistributed
     * @throws Exception
     */
    protected KafkaSupplier buildBaseSupplier(DataRuntime dataRuntime, Boolean isDistributed) throws Exception {
        try {
            KafkaSupplierSpec kafkaSupplierSpec = new KafkaSupplierSpec(bootstrap, BASE_TOPIC, kafkaAuthFlag,
                    securityProtocol, saslMechanism, saslJaasConfig);
            KafkaSupplier kafkaSupplier = dataRuntime.createGenericInstance(kafkaSupplierSpec);
            kafkaSupplier.setQueue(buildMonitorProcessor(dataRuntime));
            return kafkaSupplier;
        } catch (Throwable e) {
            logger.error("netty fail " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("netty fail");
        }
    }

    /**
     * 创建订阅器
     *
     * @param dataRuntime
     * @param isDistributed
     * @throws Exception
     */
    protected KafkaSupplier buildAgentLogSupplier(DataRuntime dataRuntime, Boolean isDistributed) throws Exception {
        try {
            KafkaSupplierSpec kafkaSupplierSpec = new KafkaSupplierSpec(bootstrap, AGENT_LOG_TOPIC, kafkaAuthFlag,
                    securityProtocol, saslMechanism, saslJaasConfig);
            KafkaSupplier kafkaSupplier = dataRuntime.createGenericInstance(kafkaSupplierSpec);
            kafkaSupplier.setQueue(buildAgentLogProcessor(dataRuntime));
            return kafkaSupplier;
        } catch (Throwable e) {
            logger.error("netty fail " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("netty fail");
        }
    }

}
