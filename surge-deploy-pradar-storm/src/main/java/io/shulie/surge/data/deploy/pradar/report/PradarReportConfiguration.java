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

import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import io.shulie.surge.data.deploy.pradar.common.DataBootstrapEnhancer;
import io.shulie.surge.data.runtime.common.DataBootstrap;
import io.shulie.surge.data.runtime.common.DataRuntime;
import io.shulie.surge.data.sink.clickhouse.ClickHouseModule;
import io.shulie.surge.data.sink.mysql.MysqlModule;
import io.shulie.surge.data.sink.redis.singleton.SingletonRedisModule;
import org.apache.commons.lang3.StringUtils;

public class PradarReportConfiguration {

    private int serverPort;
    private Map<String, String> netMap;
    private Map<String, String> hostNameMap;

    public PradarReportConfiguration() {

    }

    public PradarReportConfiguration(String netMapStr, String hostNameMapStr) {
        if (StringUtils.isNotBlank(netMapStr)) {
            this.netMap = JSON.parseObject(netMapStr, new TypeReference<Map<String, String>>() {});
        }
        if (StringUtils.isNotBlank(hostNameMapStr)) {
            this.hostNameMap = JSON.parseObject(hostNameMapStr, new TypeReference<Map<String, String>>() {});
        }
    }

    /**
     * 初始化initDataRuntime
     */
    public DataRuntime initDataRuntime() {
        DataBootstrap bootstrap = DataBootstrap.create("deploy.properties");
        this.serverPort = Integer.parseInt(bootstrap.getProperties().getProperty("report.remoting.server.port"));
        DataBootstrapEnhancer.enhancer(bootstrap);
        bootstrap.install(new ClickHouseModule(), new SingletonRedisModule(), new MysqlModule());
        return bootstrap.startRuntime();
    }

    public int getServerPort() {
        return serverPort;
    }

    public Map<String, String> getNetMap() {
        return netMap;
    }

    public Map<String, String> getHostNameMap() {
        return hostNameMap;
    }
}
