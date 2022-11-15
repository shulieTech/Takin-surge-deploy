package io.shulie.surge.data.deploy.pradar;

import io.shulie.surge.data.common.aggregation.Aggregation;
import io.shulie.surge.data.deploy.pradar.agg.AggregationReceiver;
import io.shulie.surge.data.deploy.pradar.config.PradarAggregationConfiguration;

import java.util.Map;
import java.util.Objects;

/**
 * @author vincent
 * @date 2022/11/15 11:10
 **/
public class PradarKafkaAggregationConfiguration extends PradarAggregationConfiguration {

    public static final String METRICS_TOPIC = "agg-metrics";
    public static final String E2E_TOPIC = "agg-e2e-metrics";
    public static final String TRACE_TOPIC = "agg-trace-metrics";
    public static final String APP_RELATION_TOPIC = "agg-app-relation-metrics";
    private String bootstraps;

    /**
     * 初始化
     *
     * @param args
     */
    @Override
    public void initArgs(Map<String, ?> args) {
        super.initArgs(args);
        bootstraps = Objects.toString(args.get("bootstraps"));
    }

    /**
     * 通用指标接收器
     *
     * @param aggregation
     * @return
     */
    @Override
    protected AggregationReceiver metricsReceiver(Aggregation aggregation) {
        KafkaAggregationReceiver kafkaAggregationReceiver = new KafkaAggregationReceiver(METRICS_TOPIC, bootstraps);
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
        KafkaAggregationReceiver kafkaAggregationReceiver = new KafkaAggregationReceiver(E2E_TOPIC, bootstraps);
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
        KafkaAggregationReceiver kafkaAggregationReceiver = new KafkaAggregationReceiver(TRACE_TOPIC, bootstraps);
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
        KafkaAggregationReceiver kafkaAggregationReceiver = new KafkaAggregationReceiver(APP_RELATION_TOPIC, bootstraps);
        kafkaAggregationReceiver.init(aggregation);
        return kafkaAggregationReceiver;
    }
}