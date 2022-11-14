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

import com.google.common.collect.Maps;
import io.shulie.surge.data.deploy.pradar.PradarStormSupplierConfiguration;
import io.shulie.surge.data.deploy.pradar.common.ParamUtil;
import io.shulie.surge.data.deploy.pradar.config.PradarLinkConfiguration;
import io.shulie.surge.data.deploy.pradar.config.PradarModule;
import io.shulie.surge.data.runtime.common.DataBootstrap;
import io.shulie.surge.data.runtime.common.DataRuntime;
import io.shulie.surge.data.sink.clickhouse.ClickHouseModule;
import io.shulie.surge.data.sink.clickhouse.ClickHouseShardModule;
import io.shulie.surge.data.sink.influxdb.InfluxDBModule;
import io.shulie.surge.data.sink.mysql.MysqlModule;
import io.shulie.surge.data.suppliers.nettyremoting.NettyRemotingModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author sunshiyu
 * @description 不依赖storm, 支持java -cp方式启动
 * @datetime 2021-08-04 6:59 下午
 */
public class PradarTopologyBootStrap {
    private final static Logger logger = LoggerFactory.getLogger(PradarTopologyBootStrap.class);

    public static void main(String[] args) {

        DataBootstrap bootstrap = DataBootstrap.create("deploy.properties");

        //TODO 运行时初始化

        //TODO 初始化处理任务
        //读取启动命令参数
        Map<String, String> inputMap = Maps.newHashMap();
        ParamUtil.parseInputParam(inputMap, args);
        //移除无关参数
        inputMap.remove(ParamUtil.WORKERS);
        PradarStormSupplierConfiguration pradarStormSupplierConfiguration =
                new PradarStormSupplierConfiguration(
                        inputMap.get(ParamUtil.NET),
                        inputMap.get(ParamUtil.HOSTNAME),
                        inputMap.get(ParamUtil.REGISTERZK),
                        inputMap.getOrDefault(ParamUtil.CORE_SIZE, "0"),
                        inputMap.get(ParamUtil.DATA_SOURCE_TYPE),
                        inputMap.get(ParamUtil.PORTS),
                        inputMap.get(ParamUtil.HOST),
                        inputMap.get(ParamUtil.WORK));
        try {
            bootstrap.install(
                    new PradarModule(0),
                    new NettyRemotingModule(),
                    new InfluxDBModule(),
                    new ClickHouseModule(),
                    new ClickHouseShardModule(),
                    new MysqlModule());
        } catch (Throwable e) {
            throw new RuntimeException("fail to start PradarLog", e);
        }
        inputMap.remove(ParamUtil.REGISTERZK);

        logger.info("PradarLog start successfull...");

        //TODO 预聚合任务


        //TODO 定时任务任务
        //初始化配置
        PradarLinkConfiguration pradarLinkConfiguration = new PradarLinkConfiguration(inputMap.get(ParamUtil.DATA_SOURCE_TYPE));
        try {
            //启动
            pradarLinkConfiguration.init();
        } catch (Throwable e) {
            throw new RuntimeException("fail to start PradarLink", e);
        }
        logger.info("PradarLink start successful...");


        //TODO 运行时初始化
        DataRuntime dataRuntime = bootstrap.startRuntime();

        //TODO

        try {
            pradarStormSupplierConfiguration.buildSupplier(dataRuntime, false).start();
        } catch (Exception e) {
            logger.error("Init supplier.", e);
        }


    }
}
