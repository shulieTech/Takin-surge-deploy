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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.shulie.surge.data.common.utils.CommonUtils;
import io.shulie.surge.data.deploy.pradar.report.ReportTaskServer.ReportTaskServlet;
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
import org.springframework.util.CollectionUtils;

import static io.shulie.surge.data.deploy.pradar.report.PradarReportTaskTopology.ACTIVITY_SERVICE_STREAM_ID;
import static io.shulie.surge.data.deploy.pradar.report.PradarReportTaskTopology.ACTIVITY_STREAM_ID;

/**
 * 负责统计压测报告中业务活动数据
 */
public class ReportActivityBolt extends BaseBasicBolt {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportActivityBolt.class);

    public static final String REDIS_REPORT_PREFIX = "report:analyze:";

    /**
     * 统计sql
     */
    private static final String ACTIVITY_STATISTICS_SQL =
        "select sum(cost) sumCost, max(cost) maxCost, min(cost) minCost, count(1) reqCnt, "
            + " divide(sumCost, reqCnt) avgCost, appName, '%s' reportId, parsedServiceName serviceName, parsedMethod "
            + "methodName, "
            + " rpcType from (select appName, parsedServiceName, parsedMethod, rpcType, min(cost) cost from "
            + "t_trace_pressure_%s "
            + " where logType= '1' group by traceId, appName, parsedServiceName, parsedMethod, rpcType, rpcId) "
            + " group by appName, parsedServiceName, parsedMethod, rpcType";

    private ClickHouseSupport clickHouseSupport;

    private MysqlSupport mysqlSupport;

    private RedisSupport redisSupport;

    private TopologyContext context;

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
        if (!ACTIVITY_STREAM_ID.equals(input.getSourceStreamId())) {
            return;
        }
        String reportId = input.getStringByField("reportId");
        List<ReportActivityModel> activityList = clickHouseSupport.queryForList(
            String.format(ACTIVITY_STATISTICS_SQL, reportId, reportId), ReportActivityModel.class);
        if (!CollectionUtils.isEmpty(activityList)) {
            LOGGER.info("压测报告[{}]分析业务活动任务，size=[{}]", reportId, activityList.size());
            mysqlSupport.updateBatch(ReportActivityModel.INSERT_SQL, new ArrayList<>(
                activityList.stream().map(ReportActivityModel::getValues).collect(Collectors.toSet())
            ));
            emit(activityList, collector);
        } else {
            // 删除处理标识
            redisSupport.del(ReportTaskServlet.redisKey(reportId));
        }
    }

    private void emit(List<ReportActivityModel> activityList, BasicOutputCollector collector) {
        List<Integer> tasks = context.getComponentTasks(ReportActivityServiceBolt.class.getSimpleName());
        int activitySize = activityList.size();
        int taskCount = tasks.size();
        int jobSize = (int)CommonUtils.divide(activitySize, taskCount - 1); // 估值
        jobSize = Math.max(jobSize, 1);
        List<ReportActivityEntrance>[] jobs = new List[taskCount];
        for (int i = 0; i < jobs.length; i++) {
            jobs[i] = new ArrayList<>(jobSize);
        }
        activityList.forEach(activity -> {
            int hash = activity.hashKey().hashCode() & Integer.MAX_VALUE;
            int jobId = hash % taskCount;
            jobs[jobId].add(convert(activity));
        });
        for (int i = 0; i < taskCount; i++) {
            List<ReportActivityEntrance> activities = jobs[i];
            if (!CollectionUtils.isEmpty(activities)) {
                collector.emitDirect(tasks.get(i), ACTIVITY_SERVICE_STREAM_ID, new Values(activities));
            }
        }
    }

    private ReportActivityEntrance convert(ReportActivityModel activity) {
        ReportActivityEntrance entrance = new ReportActivityEntrance(activity.getReportId(), activity.getAppName(),
            activity.getServiceName(), activity.getMethodName(), activity.getRpcType());
        entrance.setAvgCost(activity.getAvgCost());
        return entrance;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream(ACTIVITY_SERVICE_STREAM_ID, new Fields("activities"));
    }
}
