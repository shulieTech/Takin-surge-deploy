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

import com.google.common.cache.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.pamirs.pradar.log.parser.constant.TenantConstants;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.deploy.pradar.common.PradarUtils;
import io.shulie.surge.data.deploy.pradar.parser.PradarLogType;
import io.shulie.surge.data.runtime.common.remote.DefaultValue;
import io.shulie.surge.data.runtime.common.remote.Remote;
import io.shulie.surge.data.runtime.common.utils.ApiProcessor;
import io.shulie.surge.data.runtime.digest.DataDigester;
import io.shulie.surge.data.runtime.digest.DigestContext;
import io.shulie.surge.data.sink.clickhouse.ClickHouseShardSupport;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import io.shulie.surge.deploy.pradar.common.CommonStat;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 日志原文按插入顺序直接存储-写入ClickHouse
 *
 * @author pamirs
 */
@Singleton
public class LogDigester implements DataDigester<RpcBased> {
    private static final Logger logger = LoggerFactory.getLogger(LogDigester.class);
    @Inject
    private ClickHouseShardSupport clickHouseShardSupport;

    @Inject
    private MysqlSupport mysqlSupport;

    /**
     * clickhouse和mysql切换
     */
    private String dataSourceType;

    private boolean isUseCk;

    @Inject
    @DefaultValue("false")
    @Named("/pradar/config/rt/clickhouseDisable")
    private Remote<Boolean> clickhouseDisable;

    @Inject
    @DefaultValue("1")
    @Named("/pradar/config/rt/clickhouseSampling")
    private Remote<Integer> clickhouseSampling;

    //同时最多1000个报告,2分钟后没有数据写入则过期
    private static Cache<String, Long> taskIds = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(2, TimeUnit.MINUTES).removalListener(new RemovalListener<String, Long>() {
        @Override
        public void onRemoval(RemovalNotification<String, Long> removalNotification) {
            if (removalNotification.getCause().equals(RemovalCause.EXPIRED)) {
                //logger.info("[{}] pressure test is finished.Total requestCount is [{}],{}.", removalNotification.getKey(), removalNotification.getValue(), removalNotification.getCause());
            }
        }
    }).build();


    private static final Cache<String, Byte> pressureTraceIds = CacheBuilder.newBuilder().maximumSize(1000000).expireAfterWrite(1, TimeUnit.MINUTES).build();

    private transient AtomicBoolean isRunning = new AtomicBoolean(false);

    private LogTraceArgBuilder argBuilder = new LogTraceArgBuilder();

    private String sql = "";
    private String engineSql = "";

    public void init() {
        if (!isRunning.compareAndSet(false, true)) {
            return;
        }
        String tableName = "t_trace_all";
        String engineTable = "t_trace_pressure";
        if (CommonStat.isUseCk(this.dataSourceType)) {
            tableName = clickHouseShardSupport.isCluster() ? "t_trace" : "t_trace_all";
            engineTable = clickHouseShardSupport.isCluster() ? "t_pressure" : "t_trace_pressure";
            isUseCk = true;
        }
        sql = "insert into " + tableName + " (" + argBuilder.getCols() + ") values(" + argBuilder.getParam() + ") ";
        engineSql = "insert into " + engineTable + " (" + argBuilder.getCols() + ") values(" + argBuilder.getParam() + ") ";
    }

    @Override
    public void digest(DigestContext<RpcBased> context) {
        if (clickhouseDisable.get()) {
            return;
        }

        RpcBased rpcBased = context.getContent();
        if (rpcBased == null) {
            return;
        }

        if (!PradarUtils.isTraceSampleAccepted(rpcBased, clickhouseSampling.get())) {
            return;
        }

        //如果是压测引擎日志,统计每个压测报告实际上报条数
        if (rpcBased.getLogType() == PradarLogType.LOG_TYPE_FLOW_ENGINE) {
            Long count = taskIds.getIfPresent(rpcBased.getTaskId());
            taskIds.put(rpcBased.getTaskId(), count != null ? ++count : 1);
        }

        try {
            rpcBased = processRpcBased(context, rpcBased);
            Object[] args = argBuilder.buildArg(rpcBased);
            if (isUseCk) {
                //对引擎trace数据进行去重
                if (rpcBased.getLogType() == PradarLogType.LOG_TYPE_FLOW_ENGINE && rpcBased.getTraceId() != null) {
                    Byte ifPresent = pressureTraceIds.getIfPresent(rpcBased.getTraceId());
                    if (ifPresent != null) {
                        return;
                    }
                    pressureTraceIds.put(rpcBased.getTraceId(), (byte) 1);
                }
                clickHouseShardSupport.addBatch(rpcBased.getLogType() == PradarLogType.LOG_TYPE_FLOW_ENGINE ? engineSql : sql, rpcBased.getTraceId(), args);
            } else {
                mysqlSupport.update(sql, args);
            }
        } catch (Throwable e) {
            logger.warn("fail to write clickhouse, log: " + rpcBased.getLog() + ", error:" + ExceptionUtils.getStackTrace(e));
        }
    }

    private RpcBased processRpcBased(DigestContext<RpcBased> context, RpcBased rpcBased) {
        rpcBased.setDataLogTime(context.getProcessTime());
        Map<String, Object> header = context.getHeader();
        if (header.containsKey("uploadTime")) {
            rpcBased.setUploadTime((Long) header.get("uploadTime"));
        }
        rpcBased.setReceiveHttpTime((Long) header.get("receiveHttpTime"));
        //对于1.6以及之前的老版本探针,没有租户相关字段,根据应用名称获取租户配置,没有设默认值
        if (StringUtils.isBlank(rpcBased.getUserAppKey()) || TenantConstants.DEFAULT_USER_APP_KEY.equals(rpcBased.getUserAppKey())) {
            rpcBased.setUserAppKey(ApiProcessor.getTenantConfigByAppName(rpcBased.getAppName()).get("tenantAppKey"));
        }
        if (StringUtils.isBlank(rpcBased.getEnvCode())) {
            rpcBased.setEnvCode(ApiProcessor.getTenantConfigByAppName(rpcBased.getAppName()).get("envCode"));
        }
        return rpcBased;
    }


    @Override
    public int threadCount() {
        return 2;
    }

    @Override
    public void stop() {
        try {
            this.clickHouseShardSupport.stop();
        } catch (Throwable e) {
            logger.error("clickhouse stop fail");
        }
    }

    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType;
        if (CommonStat.isUseCk(this.dataSourceType)) {
            isUseCk = true;
        }
    }

    public Remote<Boolean> getClickhouseDisable() {
        return clickhouseDisable;
    }

    public Remote<Integer> getClickhouseSampling() {
        return clickhouseSampling;
    }
}
