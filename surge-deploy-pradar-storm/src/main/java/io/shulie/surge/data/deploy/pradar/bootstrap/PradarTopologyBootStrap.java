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
import io.shulie.surge.data.runtime.common.DataRuntime;
import io.shulie.surge.deploy.pradar.common.CommonStat;
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
        //读取启动命令参数
        Map<String, String> inputMap = Maps.newHashMap();
        ParamUtil.parseInputParam(inputMap, args);
        //移除无关参数
        inputMap.remove(ParamUtil.WORKERS);
        //默认指定数据源是MYSQL
        inputMap.put(ParamUtil.DATA_SOURCE_TYPE, CommonStat.MYSQL);

        PradarStormSupplierConfiguration pradarStormSupplierConfiguration =
                new PradarStormSupplierConfiguration(
                        inputMap.get(ParamUtil.NET),
                        inputMap.get(ParamUtil.HOSTNAME),
                        inputMap.get(ParamUtil.REGISTERZK),
                        inputMap.getOrDefault(ParamUtil.CORE_SIZE, "0"),
                        inputMap.get(ParamUtil.DATA_SOURCE_TYPE),
                        inputMap.get(ParamUtil.PORTS));
        try {
            DataRuntime dataRuntime = pradarStormSupplierConfiguration.initDataRuntime();
            pradarStormSupplierConfiguration.buildSupplier(dataRuntime, false).start();
        } catch (Throwable e) {
            throw new RuntimeException("fail to start PradarLog", e);
        }
        logger.info("PradarLog starte successfull...");

        inputMap.remove(ParamUtil.REGISTERZK);

        //初始化配置
        PradarLinkConfiguration pradarLinkConfiguration = new PradarLinkConfiguration(inputMap.get(ParamUtil.DATA_SOURCE_TYPE));
        try {
            //启动
            pradarLinkConfiguration.init();
        } catch (Throwable e) {
            throw new RuntimeException("fail to start PradarLinkSpout", e);
        }
        logger.info("PradarLink start successful...");
    }
}
