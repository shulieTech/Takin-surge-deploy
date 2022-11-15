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

package io.shulie.surge.data.deploy.pradar.config;

import com.google.common.collect.Sets;
import com.google.inject.Injector;
import io.shulie.surge.data.common.aggregation.Aggregation;
import io.shulie.surge.data.common.aggregation.Scheduler;
import io.shulie.surge.data.deploy.pradar.agg.*;
import io.shulie.surge.data.deploy.pradar.common.PradarRtConstant;
import io.shulie.surge.data.deploy.pradar.model.AmdbAppModel;
import io.shulie.surge.data.deploy.pradar.model.AmdbAppRelationModel;
import io.shulie.surge.data.runtime.common.DataBootstrap;
import io.shulie.surge.data.runtime.common.DataRuntime;
import io.shulie.surge.data.sink.influxdb.InfluxDBModule;
import io.shulie.surge.data.sink.influxdb.InfluxDBSupport;
import io.shulie.surge.data.sink.mysql.MysqlModule;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * 指标聚合配置
 *
 * @author vincent
 * @date 2022/11/14 17:46
 **/
public class PradarAggregationConfiguration extends AbstractPradarConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(PradarAggregationConfiguration.class);
    private static final long serialVersionUID = -659934809378009864L;

    private static final String AMDB_APP = "t_amdb_app";
    private static final String AMDB_APP_RELATION = "t_amdb_app_relation";
    private String appInsertSql = "";
    private String appRelationInsertSql = "";

    private InfluxDBSupport influxDbSupport;
    private MysqlSupport mysqlSupport;

    private String metricsDataBase = "pradar";

    protected AggregationReceiver metricsReceiver;
    protected AggregationReceiver e2eReceiver;
    protected AggregationReceiver traceMetricsReceiver;
    protected AggregationReceiver appRelationMetricsReceiver;
    private Set<String> receivers;

    /**
     * 初始化
     *
     * @param args
     */
    @Override
    public void initArgs(Map<String, ?> args) {
        appInsertSql = "INSERT IGNORE INTO " + AMDB_APP + AmdbAppModel.getCols() + " VALUES " + AmdbAppModel.getParamCols();
        appRelationInsertSql = "INSERT IGNORE INTO " + AMDB_APP_RELATION + AmdbAppRelationModel.getCols() + " VALUES " + AmdbAppRelationModel.getParamCols();
        receivers = Sets.newHashSet(StringUtils.split(Objects.toString(args.get("receivers")), ","));
    }

    /**
     * 装载module
     *
     * @param bootstrap
     */
    @Override
    public void install(DataBootstrap bootstrap) {
        bootstrap.install(
                new PradarModule(0),
                new InfluxDBModule(),
                new MysqlModule());
    }

    /**
     * 运行时启动后初始化
     *
     * @param dataRuntime
     */
    @Override
    public void doAfterInit(DataRuntime dataRuntime) {
        try {
            Injector injector = dataRuntime.getInstance(Injector.class);
            influxDbSupport = injector.getInstance(InfluxDBSupport.class);
            mysqlSupport = injector.getInstance(MysqlSupport.class);
            Scheduler scheduler = new Scheduler(receivers.size());
            if (receivers.contains("metrics")) {
                Aggregation metricsAggregation = initMetricsAggregation(scheduler);
                metricsReceiver = metricsReceiver(metricsAggregation);
            }
            if (receivers.contains("e2e")) {
                Aggregation e2eAggregation = initE2ETraceAggregation(scheduler);
                e2eReceiver = e2eReceiver(e2eAggregation);
            }
            if (receivers.contains("trace")) {
                Aggregation traceMetricsAggregation = initTraceMetricsAggregation(scheduler);
                traceMetricsReceiver = traceMetricsReceiver(traceMetricsAggregation);
            }
            if (receivers.contains("appRelation")) {
                Aggregation appRelationAggregation = initAppRelationAggregation(scheduler);
                appRelationMetricsReceiver = appRelationMetricsReceiver(appRelationAggregation);
            }
        } catch (Throwable e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw e;
        }
        logger.info("PradarReduceBolt started...");
    }

    /**
     * 通用指标接收器
     *
     * @param aggregation
     * @return
     */
    protected AggregationReceiver metricsReceiver(Aggregation aggregation) {
        AggregationReceiver aggregationReceiver = new DefaultAggregationReceiver();
        aggregationReceiver.init(aggregation);
        return aggregationReceiver;
    }

    /**
     * e2e巡检指标接收器
     *
     * @param aggregation
     * @return
     */
    protected AggregationReceiver e2eReceiver(Aggregation aggregation) {
        AggregationReceiver aggregationReceiver = new DefaultAggregationReceiver();
        aggregationReceiver.init(aggregation);
        return aggregationReceiver;
    }

    /**
     * 链路指标接收器
     *
     * @param aggregation
     * @return
     */
    protected AggregationReceiver traceMetricsReceiver(Aggregation aggregation) {
        AggregationReceiver aggregationReceiver = new DefaultAggregationReceiver();
        aggregationReceiver.init(aggregation);
        return aggregationReceiver;
    }


    /**
     * 应用关系指标接收器
     *
     * @param aggregation
     * @return
     */
    protected AggregationReceiver appRelationMetricsReceiver(Aggregation aggregation) {
        AggregationReceiver aggregationReceiver = new DefaultAggregationReceiver();
        aggregationReceiver.init(aggregation);
        return aggregationReceiver;
    }


    /**
     * 初始化E2E巡检任务
     *
     * @param scheduler
     */
    private Aggregation initE2ETraceAggregation(Scheduler scheduler) {
        scheduler = new Scheduler(1);
        Aggregation aggregation = new Aggregation(PradarRtConstant.REDUCE_TRACE_SECONDS_INTERVAL,
                PradarRtConstant.REDUCE_TRACE_SECONDS_LOWER_LIMIT);
        aggregation.start(scheduler, new E2EMetricsCommitAction(influxDbSupport, metricsDataBase));
        return aggregation;
    }

    /**
     * 初始化Trace指标聚合
     *
     * @param scheduler
     */
    private Aggregation initTraceMetricsAggregation(Scheduler scheduler) {
        Aggregation aggregation = new Aggregation(PradarRtConstant.REDUCE_TRACE_SECONDS_INTERVAL,
                PradarRtConstant.REDUCE_TRACE_SECONDS_LOWER_LIMIT);
        aggregation.start(scheduler, new TraceMetricsCommitAction(influxDbSupport));
        return aggregation;

    }

    /**
     * 初始化Trace指标聚合
     *
     * @param scheduler
     */
    private Aggregation initAppRelationAggregation(Scheduler scheduler) {
        Aggregation aggregation = new Aggregation(PradarRtConstant.REDUCE_TRACE_SECONDS_INTERVAL, PradarRtConstant.REDUCE_TRACE_SECONDS_LOWER_LIMIT);
        aggregation.start(scheduler, new AppRelationTraceCommitAction(this.appInsertSql,
                this.appRelationInsertSql, influxDbSupport, mysqlSupport));
        return aggregation;

    }

    /**
     * 初始化Trace指标聚合
     *
     * @param scheduler
     */
    private Aggregation initMetricsAggregation(Scheduler scheduler) {
        Aggregation aggregation = new Aggregation(PradarRtConstant.REDUCE_TRACE_SECONDS_INTERVAL,
                PradarRtConstant.REDUCE_TRACE_SECONDS_LOWER_LIMIT);
        aggregation.start(scheduler, new MetricsCommitAction(influxDbSupport));
        return aggregation;
    }

    public AggregationReceiver getMetricsReceiver() {
        return metricsReceiver;
    }

    public AggregationReceiver getE2eReceiver() {
        return e2eReceiver;
    }

    public AggregationReceiver getTraceMetricsReceiver() {
        return traceMetricsReceiver;
    }

    public AggregationReceiver getAppRelationMetricsReceiver() {
        return appRelationMetricsReceiver;
    }

    /**
     * 停止运行。如果已经停止，则应该不会有任何效果。
     * 建议实现使用同步方式执行。
     */
    @Override
    public void stop() throws Exception {
        if (null != metricsReceiver) {
            metricsReceiver.stop();
        }
        if (null != e2eReceiver) {
            e2eReceiver.stop();
        }

        if (null != e2eReceiver) {
            e2eReceiver.stop();
        }

        if (null != traceMetricsReceiver) {
            traceMetricsReceiver.stop();
        }

        if (null != appRelationMetricsReceiver) {
            appRelationMetricsReceiver.stop();
        }

    }
}
