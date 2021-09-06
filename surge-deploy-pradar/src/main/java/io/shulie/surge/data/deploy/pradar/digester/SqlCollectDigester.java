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

package io.shulie.surge.data.deploy.pradar.digester;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.deploy.pradar.common.HttpAsyncPoster;
import io.shulie.surge.data.deploy.pradar.common.HttpUtil;
import io.shulie.surge.data.deploy.pradar.common.PradarUtils;
import io.shulie.surge.data.deploy.pradar.parser.MiddlewareType;
import io.shulie.surge.data.runtime.common.remote.DefaultValue;
import io.shulie.surge.data.runtime.common.remote.Remote;
import io.shulie.surge.data.runtime.digest.DataDigester;
import io.shulie.surge.data.runtime.digest.DigestContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志SQL-批量发送至指定Url
 *
 * @author pamirs
 */
@Singleton
public class SqlCollectDigester implements DataDigester<RpcBased> {

    private static final Logger logger = LoggerFactory.getLogger(SqlCollectDigester.class);

    @Inject
    @DefaultValue("true")
    @Named("/pradar/config/rt/sqlCollectDisable")
    private Remote<Boolean> sqlCollectDisable;

    @Inject
    @DefaultValue("1")
    @Named("/pradar/config/rt/sqlCollectSampling")
    private Remote<Integer> sqlCollectSampling;

    /**
     * SQL解析服务URL
     */
    @Inject
    @Named("agent.sql.analysis.url")
    private String sqlAnalysisUrl;

    List<String> batches = Lists.newArrayList();

    HttpAsyncPoster httpAsyncPoster;

    @Override
    public void digest(DigestContext<RpcBased> context) {
        if (sqlCollectDisable.get()) {
            return;
        }
        if (StringUtils.isBlank(sqlAnalysisUrl)) {
            return;
        }
        RpcBased rpcBased = context.getContent();
        try {
            if (rpcBased == null || rpcBased.getRpcType() != MiddlewareType.TYPE_DB) {
                return;
            }
            if (!PradarUtils.isTraceSampleAccepted(context.getContent().getTraceId(), sqlCollectSampling.get())) {
                return;
            }
            if (StringUtils.isBlank(rpcBased.getCallbackMsg())) {
                return;
            }
            if (httpAsyncPoster == null) {
                httpAsyncPoster = new HttpAsyncPoster("SqlCollectDigester", 10, sqlAnalysisUrl, 3000);
            }
            String appName = rpcBased.getAppName();
            String dbType = rpcBased.getMiddlewareName();
            String dbHost = rpcBased.getRemoteIp();
            String dbPort = rpcBased.getPort();
            String dbName = rpcBased.getServiceName();
            String sql = rpcBased.getCallbackMsg();
            long sqlCost = rpcBased.getCost();
            long sqlStart = rpcBased.getStartTime();
            String msg = appName + "|" + dbType + "|" + dbHost + "|" + dbPort + "|" + dbName + "|" + sqlCost + "|"
                + sqlStart + "|" + sql;
            batches.add(msg);
            if (batches.size() >= 100) {
                httpAsyncPoster.sendPost(batches);
                batches = Lists.newArrayList();
            }
        } catch (Throwable e) {
            logger.warn("SqlCollectDigester fail to send sql, error:" + e.getMessage());
        }
    }

    @Override
    public int threadCount() {
        return 1;
    }

    @Override
    public void stop() throws Exception {

    }
}