package io.shulie.surge.data.deploy.pradar.agg;

import io.shulie.surge.data.common.aggregation.Aggregation;
import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.common.utils.Pair;

import java.util.List;

/**
 * 聚合接收器
 *
 * @author vincent
 * @date 2022/11/14 20:22
 **/
public interface AggregationReceiver {

    /**
     * 初始化
     *
     * @param aggregation
     */
    void init(Aggregation aggregation);

    /**
     * 处理一次操作
     *
     * @param slotKey
     * @param job
     */
    void execute(Long slotKey, List<Pair<Metric, CallStat>> job);

}
