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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import io.shulie.surge.data.deploy.pradar.parser.PradarLogType;
import io.shulie.surge.data.runtime.common.DataRuntime;
import io.shulie.surge.data.sink.clickhouse.ClickHouseSupport;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import io.shulie.surge.data.sink.redis.RedisSupport;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import static io.shulie.surge.data.common.utils.CommonUtils.divide;
import static io.shulie.surge.data.deploy.pradar.report.PradarReportTaskTopology.ACTIVITY_SERVICE_METRICS_STREAM_ID;
import static io.shulie.surge.data.deploy.pradar.report.PradarReportTaskTopology.ACTIVITY_SERVICE_STREAM_ID;
import static io.shulie.surge.data.deploy.pradar.report.ReportActivityBolt.REDIS_REPORT_PREFIX;

/**
 * 负责统计压测报告中各业务活动的接口总数据(非5s梯度)
 */
public class ReportActivityServiceBolt extends BaseBasicBolt {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportActivityServiceBolt.class);

    /**
     * 查询指定压测报告中对应业务活动中各服务接口的请求数
     */
    private static final String SERVICE_STATISTICS_SQL
        = "select appName, serviceName, methodName, rpcType, sum(toInt32(sampling)) reqCnt, "
        + " min(cost) minCost, max(cost) maxCost, sum(cost) sumCost "
        + " from (select appName, parsedServiceName serviceName, parsedMethod methodName, "
        + " rpcType, traceId, samplingInterval sampling, min(cost) cost"
        + " from t_trace_pressure_%s where traceId in (select DISTINCT traceId from t_trace_pressure_%s "
        + " where appName = '%s' AND parsedServiceName = '%s' and parsedMethod = '%s' and rpcType = '%s') "
        + " AND logType in ('" + PradarLogType.LOG_TYPE_TRACE + "', '" + PradarLogType.LOG_TYPE_RPC_SERVER + "') "
        + " AND rpcType in ('0', '1') group by appName, serviceName, methodName, rpcType, traceId, sampling) "
        + " group by appName, serviceName, methodName, rpcType";

    private ClickHouseSupport clickHouseSupport;

    private MysqlSupport mysqlSupport;

    private TopologyContext context;

    private RedisSupport redisSupport;

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        DataRuntime dataRuntime = new PradarReportConfiguration().initDataRuntime();
        this.clickHouseSupport = dataRuntime.getInstance(ClickHouseSupport.class);
        this.mysqlSupport = dataRuntime.getInstance(MysqlSupport.class);
        this.redisSupport = dataRuntime.getInstance(RedisSupport.class);
        this.context = context;
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        if (!ACTIVITY_SERVICE_STREAM_ID.equals(input.getSourceStreamId())) {
            return;
        }
        List<ReportActivityEntrance> activities = (List<ReportActivityEntrance>)input.getValueByField("activities");
        activities.forEach(activity -> {
            List<ReportServiceMetricsModel> services = clickHouseSupport.queryForList(formatSql(activity),
                ReportServiceMetricsModel.class);
            List<ReportServiceEntrance> serviceEntrances = new ArrayList<>(services.size());
            saveServiceMetrics(activity, services);
            services.forEach(service -> serviceEntrances.add(convert(service)));
            // 每个业务活动有多少接口
            redisSupport.hmset(REDIS_REPORT_PREFIX + activity.getReportId(), activity.hashKey(), services.size());
            LOGGER.info("压测报告[{}]分析业务活动接口任务，size=[{}]", activity.getReportId(), serviceEntrances.size());
            emit(serviceEntrances, collector);
        });
    }

    private void emit(List<ReportServiceEntrance> serviceInterfaces, BasicOutputCollector collector) {
        int size = serviceInterfaces.size();
        List<Integer> tasks = context.getComponentTasks(ReportServiceMetricsBolt.class.getSimpleName());
        int taskCount = tasks.size();
        int jobSize = (int)divide(size, taskCount - 1); // 估值
        jobSize = Math.max(jobSize, 1);
        List<ReportServiceEntrance>[] jobs = new List[taskCount];
        for (int i = 0; i < jobs.length; i++) {
            jobs[i] = new ArrayList<>(jobSize);
        }
        serviceInterfaces.forEach(service -> {
            int hash = service.hashKey().hashCode() & Integer.MAX_VALUE;
            int jobId = hash % taskCount;
            jobs[jobId].add(service);
        });
        for (int i = 0; i < taskCount; i++) {
            List<ReportServiceEntrance> activities = jobs[i];
            if (!CollectionUtils.isEmpty(activities)) {
                collector.emitDirect(tasks.get(i), ACTIVITY_SERVICE_METRICS_STREAM_ID, new Values(activities));
            }
        }
    }

    private void saveServiceMetrics(ReportActivityEntrance entrance, List<ReportServiceMetricsModel> services) {
        if (!CollectionUtils.isEmpty(services)) {
            String appName = entrance.getAppName();
            String serviceName = entrance.getServiceName();
            String methodName = entrance.getMethodName();
            String rpcType = entrance.getRpcType();
            BigDecimal avgCost = entrance.getAvgCost();
            String reportId = entrance.getReportId();
            List<Object[]> params = services.stream().map(service -> {
                service.setEntranceAppName(appName);
                service.setEntranceServiceName(serviceName);
                service.setEntranceMethodName(methodName);
                service.setEntranceRpcType(rpcType);
                service.setServiceAvgCost(avgCost);
                service.setReportId(reportId);
                return service.getValues();
            }).collect(Collectors.toList());
            Lists.partition(params, 300).forEach(
                param -> mysqlSupport.updateBatch(ReportServiceMetricsModel.INSERT_SQL, param));
        }
    }

    private ReportServiceEntrance convert(ReportServiceMetricsModel service) {
        ReportServiceEntrance entrance = new ReportServiceEntrance();
        BeanUtils.copyProperties(service, entrance);
        return entrance;
    }

    private String formatSql(ReportActivityEntrance entrance) {
        String reportId = entrance.getReportId();
        return String.format(SERVICE_STATISTICS_SQL, reportId, reportId, entrance.getAppName(),
            entrance.getServiceName(), entrance.getMethodName(), entrance.getRpcType());
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream(ACTIVITY_SERVICE_METRICS_STREAM_ID, new Fields("activities"));
    }
}
