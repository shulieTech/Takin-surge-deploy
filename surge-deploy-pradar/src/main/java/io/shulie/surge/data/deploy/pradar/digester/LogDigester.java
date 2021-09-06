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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.deploy.pradar.common.PradarUtils;
import io.shulie.surge.data.deploy.pradar.digester.command.BaseCommand;
import io.shulie.surge.data.deploy.pradar.digester.command.ClickhouseFacade;
import io.shulie.surge.data.deploy.pradar.digester.command.FlagCommand;
import io.shulie.surge.data.deploy.pradar.digester.command.LinkCommand;
import io.shulie.surge.data.runtime.common.remote.DefaultValue;
import io.shulie.surge.data.runtime.common.remote.Remote;
import io.shulie.surge.data.runtime.digest.DataDigester;
import io.shulie.surge.data.runtime.digest.DigestContext;
import io.shulie.surge.data.sink.clickhouse.ClickHouseShardSupport;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import io.shulie.surge.deploy.pradar.common.CommonStat;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
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

    @Inject
    @DefaultValue("false")
    @Named("/pradar/config/rt/clickhouseDisable")
    private Remote<Boolean> clickhouseDisable;

    @Inject
    @DefaultValue("1")
    @Named("/pradar/config/rt/clickhouseSampling")
    private Remote<Integer> clickhouseSampling;

    private transient AtomicBoolean isRunning = new AtomicBoolean(false);

    private ClickhouseFacade clickhouseFacade = ClickhouseFacade.Factory.getInstace();

    private String sql = "";

    public void init() {
        String tableName = "t_trace_all";
        if (CommonStat.isUseCk(this.dataSourceType)) {
            tableName = clickHouseShardSupport.isCluster() ? "t_trace" : "t_trace_all";
        }
        clickhouseFacade.addCommond(new BaseCommand());
        clickhouseFacade.addCommond(new LinkCommand());
        clickhouseFacade.addCommond(new FlagCommand());
        sql = "insert into " + tableName + " (" + clickhouseFacade.getCols() + ") values(" + clickhouseFacade.getParam() + ") ";
    }

    @Override
    public void digest(DigestContext<RpcBased> context) {
        if (clickhouseDisable.get()) {
            return;
        }
        if (isRunning.compareAndSet(false, true)) {
            init();
        }
        RpcBased rpcBased = context.getContent();
        try {
            if (rpcBased == null) {
                return;
            }
            if (!PradarUtils.isTraceSampleAccepted(context.getContent().getTraceId(), clickhouseSampling.get())) {
                return;
            }
            rpcBased.setDataLogTime(context.getProcessTime());
            List<Object[]> batchs = Lists.newArrayList();
            batchs.add(clickhouseFacade.toObjects(clickhouseFacade.invoke(rpcBased)));
            Map<String, List<Object[]>> objMap = Maps.newHashMap();
            objMap.put(rpcBased.getTraceId(), batchs);

            // TODO 此修改支持mysql和clickhouse写入,代码不是很友好，后续剥离出来
            if (CommonStat.isUseCk(dataSourceType)) {
                clickHouseShardSupport.batchUpdate(sql, objMap);
            } else {
                mysqlSupport.batchUpdate(sql, batchs);
            }
        } catch (Throwable e) {
            logger.warn("fail to write clickhouse, log: " + rpcBased.getLog() + ", error:" + ExceptionUtils.getStackTrace(e));
        }
    }


    @Override
    public int threadCount() {
        return 1;
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
    }

    public Remote<Boolean> getClickhouseDisable() {
        return clickhouseDisable;
    }

    public Remote<Integer> getClickhouseSampling() {
        return clickhouseSampling;
    }
}
