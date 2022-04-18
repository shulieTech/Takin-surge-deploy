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

package io.shulie.surge.data.sink.influxdb;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


/**
 * 返回单例的 {@link InfluxDBSupportProvider} 对象
 *
 * @author pamirs
 */
@Singleton
public class InfluxDBSupportProvider implements Provider<InfluxDBSupport> {
    private static final Logger logger = Logger.getLogger(InfluxDBSupportProvider.class);

    private InfluxDBSupport singleton;

    @Inject
    public InfluxDBSupportProvider(@Named("config.influxdb.url") String url,
                                   @Named("config.influxdb.username") String username,
                                   @Named("config.influxdb.password") String password,
                                   @Named("config.influxdb.metircs.duration") String metircsDuration,
                                   @Named("config.influxdb.monitor.duration") String monitorDuration,
                                   @Named("config.influxdb.engine.duration") String engineDuration,
                                   @Named("config.influxdb.database.metircs") String metricsDataBase,
                                   @Named("config.influxdb.database.monitor") String monitorDataBase,
                                   @Named("config.influxdb.database.engine") String engineDataBase) {

        try {
            this.singleton = new DefaultInfluxDBSupport(url, username, password);

            /**
             * 默认创建两个库
             */
            if (StringUtils.isNotBlank(monitorDataBase) || StringUtils.isNotBlank(metricsDataBase) || StringUtils.isNotBlank(engineDataBase)) {
                this.singleton.createDatabase(monitorDataBase);
                this.singleton.createDatabase(metricsDataBase);
                this.singleton.createDatabase(engineDataBase);
                this.singleton.createRetentionPolicy(monitorDataBase, monitorDataBase, monitorDuration, 1);
                this.singleton.createRetentionPolicy(metricsDataBase, metricsDataBase, metircsDuration, 1);
                this.singleton.createRetentionPolicy(engineDataBase, engineDataBase, engineDuration, 1);
            }
        } catch (Exception e) {
            logger.warn("InfluxDBServiceProvider init fail.url :{}" + url, e);
            throw e;
        }
    }

    @Override
    public InfluxDBSupport get() {
        return this.singleton;
    }
}
