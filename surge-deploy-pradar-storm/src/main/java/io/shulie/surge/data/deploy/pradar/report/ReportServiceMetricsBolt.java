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

package io.shulie.surge.data.deploy.pradar.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import io.shulie.surge.data.deploy.pradar.report.ReportTaskServer.ReportTaskServlet;
import io.shulie.surge.data.runtime.common.DataRuntime;
import io.shulie.surge.data.sink.clickhouse.ClickHouseSupport;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import io.shulie.surge.data.sink.redis.RedisSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.shulie.surge.data.deploy.pradar.report.PradarReportTaskTopology.ACTIVITY_SERVICE_METRICS_STREAM_ID;
import static io.shulie.surge.data.deploy.pradar.report.ReportActivityBolt.REDIS_REPORT_PREFIX;
import static io.shulie.surge.data.deploy.pradar.report.ReportActivityModel.UPDATE_STATE_SQL;
import static io.shulie.surge.data.deploy.pradar.report.ReportServiceMetricsModel.UPDATE_SERVICE_SELF_COST;
import static io.shulie.surge.data.deploy.pradar.report.ReportServiceMetricsWindowsModel.SELF_COST_SQL;

/**
 * 负责统计各业务活动服务接口的每5s数据
 */
public class ReportServiceMetricsBolt extends BaseBasicBolt {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportServiceMetricsBolt.class);

    /**
     * 计算对应业务活动的接口在各时间窗口内的最小耗时、最大耗时、总耗时、调用总次数、rpcId集合
     */
    private static final String SERVICE_STATISTICS_SQL =
        "select appName, parsedServiceName serviceName, parsedMethod methodName, rpcType rpcType, timeWindow, "
            + " sum(toInt32(samplingInterval)) reqCnt, min(cost) minCost, sum(cost) sumCost, max(cost) maxCost, "
            + " count(1) countAfterSimp, arrayStringConcat(groupUniqArray(rpcId), ',') AS rpcIds "
            + " from t_trace_pressure_%s where traceId in ( SELECT distinct traceId from t_trace_pressure_%s "
            + " where appName = '%s' and parsedServiceName = '%s' and parsedMethod = '%s' and rpcType = '%s') "
            + " and appName = '%s' and parsedServiceName = '%s' and parsedMethod = '%s' and rpcType = '%s' "
            + " group by appName, parsedServiceName, parsedMethod, rpcType, timeWindow";

    /**
     * 统计下有节点耗时
     */
    private static final String DOWN_STREAM_STATISTICS_SQL =
        "select sum(cost) downstreamCost, timeWindow from t_trace_pressure_%s where traceId in "
            + " (SELECT distinct traceId from t_trace_pressure_%s where appName = '%s' and parsedServiceName = '%s' "
            + " and parsedMethod = '%s' and rpcType = '%s') and upAppName = '%s' and logType = '2' and async = '0' "
            + " and %s group by timeWindow";

    private ClickHouseSupport clickHouseSupport;

    private MysqlSupport mysqlSupport;

    private RedisSupport redisSupport;

    private ReportTaskNotifier notifier;

    @Override
    public void prepare(Map conf, TopologyContext context) {
        DataRuntime dataRuntime = new PradarReportConfiguration().initDataRuntime();
        this.clickHouseSupport = dataRuntime.getInstance(ClickHouseSupport.class);
        this.mysqlSupport = dataRuntime.getInstance(MysqlSupport.class);
        this.redisSupport = dataRuntime.getInstance(RedisSupport.class);
        this.notifier = dataRuntime.getInstance(ReportTaskNotifier.class);
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        if (!ACTIVITY_SERVICE_METRICS_STREAM_ID.equals(input.getSourceStreamId())) {
            return;
        }
        // 业务活动下接口级别数据
        List<ReportServiceEntrance> activities = (List<ReportServiceEntrance>)input.getValueByField("activities");
        activities.forEach(this::calcServiceMetrics);
    }

    private void calcServiceMetrics(ReportServiceEntrance entrance) {
        List<ReportServiceMetricsWindowsModel> metricsParts = clickHouseSupport.queryForList(formatServiceSql(entrance)
            , ReportServiceMetricsWindowsModel.class);
        LOGGER.info("压测报告[{}]分析业务活动接口窗口任务，size=[{}]", entrance.getReportId(), metricsParts.size());
        Map<Date, ReportServiceMetricsWindowsModel> serviceMap = new HashMap<>(metricsParts.size());
        Set<String> rpcIdSet = new HashSet<>();
        for (ReportServiceMetricsWindowsModel part : metricsParts) {
            serviceMap.putIfAbsent(part.getTimeWindow(), part);
            rpcIdSet.addAll(Arrays.asList(part.getRpcIds().split(",")));
        }
        List<DownstreamTimedCost> downstreamCostList = calcDownstreamCost(entrance, rpcIdSet);
        Map<Date, Long> downstreamMap = new HashMap<>(downstreamCostList.size());
        for (DownstreamTimedCost timedCost : downstreamCostList) {
            downstreamMap.putIfAbsent(timedCost.getTimeWindow(), timedCost.getDownstreamCost());
        }
        String reportId = entrance.getReportId();
        String appName = entrance.getEntranceAppName();
        String serviceName = entrance.getEntranceServiceName();
        String methodName = entrance.getEntranceMethodName();
        String rpcType = entrance.getEntranceRpcType();
        serviceMap.forEach((key, value) -> {
            Long downstreamCost = downstreamMap.get(key);
            value.setReportId(reportId);
            value.setEntranceAppName(appName);
            value.setEntranceServiceName(serviceName);
            value.setEntranceMethodName(methodName);
            value.setEntranceRpcType(rpcType);
            if (downstreamCost == null) {
                downstreamCost = 0L;
            }
            BigDecimal avgCost = new BigDecimal(String.valueOf(value.getSumCost() - downstreamCost))
                .divide(new BigDecimal(String.valueOf(value.getCountAfterSimp())), 4, RoundingMode.HALF_EVEN);
            value.setDownstreamCost(downstreamCost);
            value.setAvgCost(avgCost);
        });
        // 保存数据
        saveServiceMetrics(serviceMap.values());
        afterProcessRedisData(entrance);
    }

    private void afterProcessRedisData(ReportServiceEntrance entrance) {
        String key = REDIS_REPORT_PREFIX + entrance.getReportId();
        String field = entrance.parenHashKey();
        associateUpdateInterface(entrance);
        Long value = redisSupport.hIncrBy(key, field, -1);
        if (value != null && value.compareTo(0L) == 0) {
            redisSupport.hDel(key, field);
            updateActivity(entrance);
            Long size = redisSupport.hSize(key);
            if (size != null && size == 0) {
                redisSupport.del(key, ReportTaskServlet.redisKey(entrance.getReportId()));
                notifyReportCompleted(entrance.getReportId());
            }
        }
    }

    private void associateUpdateInterface(ReportServiceEntrance entrance) {
        updateSelfCost(entrance);
        updateServiceStatus(entrance);
    }

    private void updateActivity(ReportServiceEntrance entrance) {
        Object[] params = {entrance.getReportId(), entrance.getEntranceAppName(), entrance.getEntranceServiceName(),
            entrance.getEntranceMethodName(), entrance.getEntranceRpcType()};
        mysqlSupport.update(UPDATE_STATE_SQL, params);
    }

    // 更新自耗时
    private void updateSelfCost(ReportServiceEntrance entrance) {
        String selfCostSql = String.format(SELF_COST_SQL, entrance.getReportId(), entrance.getAppName(),
            entrance.getServiceName(), entrance.getMethodName(), entrance.getRpcType(), entrance.getEntranceAppName(),
            entrance.getEntranceServiceName(), entrance.getEntranceMethodName(), entrance.getEntranceRpcType());
        BigDecimal avgCost = mysqlSupport.queryForObject(selfCostSql, BigDecimal.class);

        Object[] params = {avgCost, entrance.getReportId(), entrance.getAppName(),
            entrance.getServiceName(), entrance.getMethodName(), entrance.getRpcType(), entrance.getEntranceAppName(),
            entrance.getEntranceServiceName(), entrance.getEntranceMethodName(), entrance.getEntranceRpcType()};
        mysqlSupport.update(UPDATE_SERVICE_SELF_COST, params);
    }

    // 更新接口处理状态
    private void updateServiceStatus(ReportServiceEntrance entrance) {
        Object[] param = new Object[] {entrance.getReportId(), entrance.getAppName(), entrance.getServiceName(),
            entrance.getMethodName(), entrance.getRpcType(), entrance.getEntranceAppName(),
            entrance.getEntranceServiceName(), entrance.getEntranceMethodName(), entrance.getEntranceRpcType()};
        mysqlSupport.updateBatch(ReportServiceMetricsModel.UPDATE_SERVICE_STATUS, Collections.singletonList(param));
    }

    private void notifyReportCompleted(String reportId) {
        notifier.notifyTaskCompleted(reportId);
    }

    private void saveServiceMetrics(Collection<ReportServiceMetricsWindowsModel> serviceMetrics) {
        List<Object[]> params = serviceMetrics.stream().map(ReportServiceMetricsWindowsModel::getValues).collect(
            Collectors.toList());
        Lists.partition(params, 300).forEach(
            param -> mysqlSupport.updateBatch(ReportServiceMetricsWindowsModel.INSERT_SQL, param));
    }

    // 查询接口各时间窗口metrics
    private String formatServiceSql(ReportServiceEntrance entrance) {
        String reportId = entrance.getReportId();
        return String.format(SERVICE_STATISTICS_SQL, reportId, reportId, entrance.getEntranceAppName(),
            entrance.getEntranceServiceName(), entrance.getEntranceMethodName(), entrance.getEntranceRpcType(),
            entrance.getAppName(), entrance.getServiceName(), entrance.getMethodName(), entrance.getRpcType());
    }

    // 查询下游各时间窗口耗时
    private List<DownstreamTimedCost> calcDownstreamCost(ReportServiceEntrance entrance, Set<String> rpcIdSet) {
        String downstreamSql = formatDownstreamSql(entrance, buildRpcIdCondition(rpcIdSet));
        return clickHouseSupport.queryForList(downstreamSql, DownstreamTimedCost.class);
    }

    // 构建下游rpcId的match条件
    private String buildRpcIdCondition(Set<String> rpcIdSet) {
        Set<String> regexes = rpcIdSet.stream().map(this::buildRegexCondition).collect(Collectors.toSet());
        return " multiMatchAny(rpcId, [" + StringUtils.join(regexes, ",") + "]) ";
    }

    // 构建查询下游耗时sql
    private String formatDownstreamSql(ReportServiceEntrance entrance, String rpcIdCondition) {
        String reportId = entrance.getReportId();
        return String.format(DOWN_STREAM_STATISTICS_SQL, reportId, reportId, entrance.getEntranceAppName(),
            entrance.getEntranceServiceName(), entrance.getEntranceMethodName(), entrance.getEntranceRpcType(),
            entrance.getAppName(), rpcIdCondition);
    }

    private String buildRegexCondition(String rpcId) {
        int count = StringUtils.countMatches(rpcId, '.');
        StringBuilder builder = new StringBuilder("'^");
        for (int i = -1; i < count; i++) {
            builder.append("\\d+.");
        }
        builder.append("\\d+$'");
        return builder.toString();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }

    private static class DownstreamTimedCost {
        private Date timeWindow;
        private Long downstreamCost;

        public Date getTimeWindow() {
            return timeWindow;
        }

        public void setTimeWindow(Date timeWindow) {
            this.timeWindow = timeWindow;
        }

        public Long getDownstreamCost() {
            return downstreamCost;
        }

        public void setDownstreamCost(Long downstreamCost) {
            this.downstreamCost = downstreamCost;
        }
    }
}
