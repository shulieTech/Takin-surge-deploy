package io.shulie.surge.data.deploy.pradar;

import io.shulie.surge.data.common.aggregation.Aggregation;
import io.shulie.surge.data.deploy.pradar.agg.AggregationReceiver;
import io.shulie.surge.data.deploy.pradar.config.PradarAggregationConfiguration;
import io.shulie.surge.data.deploy.pradar.config.PradarModule;
import io.shulie.surge.data.runtime.common.DataBootstrap;
import io.shulie.surge.data.runtime.common.remote.impl.RemoteNacosModule;
import io.shulie.surge.data.runtime.module.NacosClientModule;
import io.shulie.surge.data.sink.influxdb.InfluxDBModule;
import io.shulie.surge.data.sink.mysql.MysqlModule;

import java.util.Map;
import java.util.Objects;

/**
 * @author vincent
 * @date 2022/11/15 11:10
 **/
public class PradarKafkaAggregationConfiguration extends PradarAggregationConfiguration {

    public static final String METRICS_TOPIC = "agg-metrics";
    private static final String TRACE_REDUCE_TOPIC = "trace-reduce-metrics";
    private static final String E2E_TOPIC = "e2e-metrics";
    public static final String APP_RELATION_TOPIC = "agg-app-relation-metrics";
    private String bootstrap;

    /**
     * 初始化
     *
     * @param args
     */
    @Override
    public void initArgs(Map<String, ?> args) {
        super.initArgs(args);
        bootstrap = Objects.toString(args.get("bootstrap"));
    }


    /**
     * 装载module
     *
     * @param bootstrap
     */
    @Override

    public void install(DataBootstrap bootstrap) {
        bootstrap.install(new PradarModule(0), new InfluxDBModule(), new MysqlModule(), new NacosClientModule(), new RemoteNacosModule());
    }

    /**
     * 通用指标接收器
     *
     * @param aggregation
     * @return
     */
    @Override
    protected AggregationReceiver metricsReceiver(Aggregation aggregation) {
        KafkaAggregationReceiver kafkaAggregationReceiver = new KafkaAggregationReceiver(METRICS_TOPIC, bootstrap);
        kafkaAggregationReceiver.init(aggregation);
        return kafkaAggregationReceiver;
    }

    /**
     * e2e巡检指标接收器
     *
     * @param aggregation
     * @return
     */
    @Override
    protected AggregationReceiver e2eReceiver(Aggregation aggregation) {
        KafkaAggregationReceiver kafkaAggregationReceiver = new KafkaAggregationReceiver(E2E_TOPIC, bootstrap);
        kafkaAggregationReceiver.init(aggregation);
        return kafkaAggregationReceiver;
    }

    /**
     * 链路指标接收器
     *
     * @param aggregation
     * @return
     */
    @Override
    protected AggregationReceiver traceMetricsReceiver(Aggregation aggregation) {
        KafkaAggregationReceiver kafkaAggregationReceiver = new KafkaAggregationReceiver(TRACE_REDUCE_TOPIC, bootstrap);
        kafkaAggregationReceiver.init(aggregation);
        return kafkaAggregationReceiver;
    }

    /**
     * 应用关系指标接收器
     *
     * @param aggregation
     * @return
     */
    @Override
    protected AggregationReceiver appRelationMetricsReceiver(Aggregation aggregation) {
        KafkaAggregationReceiver kafkaAggregationReceiver = new KafkaAggregationReceiver(APP_RELATION_TOPIC, bootstrap);
        kafkaAggregationReceiver.init(aggregation);
        return kafkaAggregationReceiver;
    }
}
