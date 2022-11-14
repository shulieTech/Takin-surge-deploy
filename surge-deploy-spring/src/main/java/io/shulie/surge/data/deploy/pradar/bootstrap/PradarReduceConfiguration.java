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

package io.shulie.surge.data.deploy.pradar.bootstrap;

import com.google.inject.Injector;
import io.shulie.surge.data.common.aggregation.Aggregation;
import io.shulie.surge.data.common.aggregation.Scheduler;
import io.shulie.surge.data.deploy.pradar.E2ETraceReduceBolt;
import io.shulie.surge.data.deploy.pradar.PradarAppRelationTraceReduceBolt;
import io.shulie.surge.data.deploy.pradar.PradarTraceReduceBolt;
import io.shulie.surge.data.deploy.pradar.common.PradarRtConstant;
import io.shulie.surge.data.deploy.pradar.config.PradarModule;
import io.shulie.surge.data.deploy.pradar.model.AmdbAppModel;
import io.shulie.surge.data.deploy.pradar.model.AmdbAppRelationModel;
import io.shulie.surge.data.runtime.common.DataBootstrap;
import io.shulie.surge.data.runtime.common.DataRuntime;
import io.shulie.surge.data.sink.influxdb.InfluxDBModule;
import io.shulie.surge.data.sink.influxdb.InfluxDBSupport;
import io.shulie.surge.data.sink.mysql.MysqlModule;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * @author vincent
 * @date 2022/11/14 17:46
 **/
public class PradarReduceConfiguration extends AbstractPradarConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(PradarReduceConfiguration.class);
    private static final long serialVersionUID = -659934809378009864L;

    private static final String AMDB_APP = "t_amdb_app";
    private static final String AMDB_APP_RELATION = "t_amdb_app_relation";
    private String appInsertSql = "";
    private String appRelationInsertSql = "";

    private InfluxDBSupport influxDbSupport;
    private MysqlSupport mysqlSupport;
    private Scheduler scheduler;

    private String metricsDataBase = "pradar";


    /**
     * 初始化
     *
     * @param args
     */
    @Override
    public void initArgs(Map<String, Object> args) {
        appInsertSql = "INSERT IGNORE INTO " + AMDB_APP + AmdbAppModel.getCols() + " VALUES " + AmdbAppModel.getParamCols();
        appRelationInsertSql = "INSERT IGNORE INTO " + AMDB_APP_RELATION + AmdbAppRelationModel.getCols() + " VALUES " + AmdbAppRelationModel.getParamCols();
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
    public void doAfterInit(DataRuntime dataRuntime) throws Exception {
        try {
            Injector injector = dataRuntime.getInstance(Injector.class);
            influxDbSupport = injector.getInstance(InfluxDBSupport.class);
            mysqlSupport = injector.getInstance(MysqlSupport.class);
            scheduler = new Scheduler(1);
        } catch (Throwable e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw e;
        }
        logger.info("PradarReduceBolt started...");
    }

    /**
     * 初始化E2E巡检任务
     *
     * @param scheduler
     */
    private void initE2ETraceAggregation(Scheduler scheduler) {
        scheduler = new Scheduler(1);
        Aggregation aggregation = new Aggregation(PradarRtConstant.REDUCE_TRACE_SECONDS_INTERVAL,
                PradarRtConstant.REDUCE_TRACE_SECONDS_LOWER_LIMIT);
        aggregation.start(scheduler, new E2ETraceReduceBolt.TraceMetricsCommitAction(influxDbSupport, metricsDataBase));
    }

    /**
     * 初始化Trace指标聚合
     *
     * @param scheduler
     */
    private void initTraceAggregation(Scheduler scheduler) {
        Aggregation aggregation = new Aggregation(PradarRtConstant.REDUCE_TRACE_SECONDS_INTERVAL,
                PradarRtConstant.REDUCE_TRACE_SECONDS_LOWER_LIMIT);
        aggregation.start(scheduler, new PradarTraceReduceBolt.TraceMetricsCommitAction(influxDbSupport));
    }

    /**
     * 初始化Trace指标聚合
     *
     * @param scheduler
     */
    private void initAppRelationAggregation(Scheduler scheduler) {
        Aggregation aggregation = new Aggregation(PradarRtConstant.REDUCE_TRACE_SECONDS_INTERVAL, PradarRtConstant.REDUCE_TRACE_SECONDS_LOWER_LIMIT);
        aggregation.start(scheduler, new PradarAppRelationTraceReduceBolt.TraceMetricsCommitAction(this.appInsertSql,
                this.appRelationInsertSql, influxDbSupport, mysqlSupport));
    }


}
