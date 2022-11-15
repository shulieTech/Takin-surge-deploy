package io.shulie.surge.data.deploy.pradar.agg;

import io.shulie.surge.data.common.aggregation.AggregateSlot;
import io.shulie.surge.data.common.aggregation.Aggregation;
import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.common.utils.FormatUtils;
import io.shulie.surge.data.common.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 抽象聚合接收器
 *
 * @author vincent
 * @date 2022/11/14 20:26
 **/
public class DefaultAggregationReceiver implements AggregationReceiver {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAggregationReceiver.class);

    private Aggregation aggregation;

    /**
     * 初始化
     *
     * @param aggregation
     */
    @Override
    public void init(Aggregation aggregation) {
        this.aggregation = aggregation;
    }

    /**
     * 处理一次操作
     *
     * @param slotKey
     * @param job
     */
    @Override
    public void execute(Long slotKey, List<Pair<Metric, CallStat>> job) {
        AggregateSlot<Metric, CallStat> slot = aggregation.getSlotByTimestamp(slotKey);
        if (slot != null) {
            for (Pair<Metric, CallStat> pair : job) {
                slot.addToSlot(pair.getFirst(), pair.getSecond());
            }
        } else {
            logger.info("no slot for " + slotKey + ": " + FormatUtils.toSecondTimeString(slotKey * 1000));
        }
    }
}
