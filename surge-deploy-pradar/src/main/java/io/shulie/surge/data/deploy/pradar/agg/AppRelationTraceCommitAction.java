package io.shulie.surge.data.deploy.pradar.agg;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.shulie.surge.data.common.aggregation.AggregateSlot;
import io.shulie.surge.data.common.aggregation.Aggregation;
import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.deploy.pradar.model.AmdbAppModel;
import io.shulie.surge.data.deploy.pradar.model.AmdbAppRelationModel;
import io.shulie.surge.data.sink.influxdb.InfluxDBSupport;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * APP指标计算提交
 */
public class AppRelationTraceCommitAction implements Aggregation.CommitAction<Metric, CallStat> {
    private static Logger logger = LoggerFactory.getLogger(AppRelationTraceCommitAction.class);

    private InfluxDBSupport influxDbSupport;
    private MysqlSupport mysqlSupport;
    private String appInsertSql;
    private String appRelationInsertSql;

    public AppRelationTraceCommitAction(String appInsertSql,
                                    String appRelationInsertSql,
                                    InfluxDBSupport influxDbSupport,
                                    MysqlSupport mysqlSupport) {
        this.appInsertSql = appInsertSql;
        this.appRelationInsertSql = appRelationInsertSql;
        this.influxDbSupport = influxDbSupport;
        this.mysqlSupport = mysqlSupport;
    }

    @Override
    public void commit(long slotKey, AggregateSlot<Metric, CallStat> slot) {
        try {
            Set<AmdbAppModel> appSet = Sets.newHashSet();
            Set<AmdbAppRelationModel> appRelationSet = Sets.newHashSet();

            slot.toMap().entrySet().forEach(metricCallStatEntry -> {
                Metric metric = metricCallStatEntry.getKey();
                CallStat callStat = metricCallStatEntry.getValue();

                String metricsId = metric.getMetricId();
                String[] tags = metric.getPrefixes();

                Map<String, String> influxdbTags = Maps.newHashMap();
                influxdbTags.put("fromAppName", tags[0]);
                influxdbTags.put("toAppName", tags[1]);
                // 总次数/成功次数/totalRt/错误次数/totalQps
                Map<String, Object> fields = Maps.newHashMap();
                fields.put("totalCount", callStat.get(0));
                fields.put("successCount", callStat.get(1));
                fields.put("totalRt", callStat.get(2));
                fields.put("errorCount", callStat.get(3));
                fields.put("totalQps", callStat.get(4));
                // 写入influxDB
                influxDbSupport.write("pradar", metricsId, influxdbTags, fields, slotKey * 1000);

                AmdbAppModel fromApp = new AmdbAppModel(tags[0], tags[2]);
                AmdbAppModel toApp = new AmdbAppModel(tags[1], tags[3]);
                AmdbAppRelationModel amdbAppRelationModel = new AmdbAppRelationModel(tags[0], tags[1]);

                appSet.add(fromApp);
                appSet.add(toApp);
                appRelationSet.add(amdbAppRelationModel);
            });

            mysqlSupport.batchUpdate(appInsertSql, appSet.stream().map(AmdbAppModel::getValues).collect(Collectors.toList()));
            mysqlSupport.batchUpdate(appRelationInsertSql, appRelationSet.stream().map(AmdbAppRelationModel::getValues).collect(Collectors.toList()));
        } catch (Throwable e) {
            logger.error("write fail influxdb " + ExceptionUtils.getStackTrace(e));
        }
    }
}