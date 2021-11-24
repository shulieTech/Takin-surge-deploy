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

package io.shulie.surge.data.deploy.pradar.link.processor;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.shulie.surge.config.clickhouse.ClickhouseTemplateHolder;
import io.shulie.surge.config.clickhouse.ClickhouseTemplateManager;
import io.shulie.surge.data.deploy.pradar.link.model.TenantAppEntity;
import io.shulie.surge.data.common.utils.DateUtils;
import io.shulie.surge.data.common.utils.Pair;
import io.shulie.surge.data.deploy.pradar.link.TaskManager;
import io.shulie.surge.data.deploy.pradar.link.constants.SqlConstants;
import io.shulie.surge.data.deploy.pradar.link.model.LinkEntranceModel;
import io.shulie.surge.data.deploy.pradar.link.util.StringUtil;
import io.shulie.surge.data.deploy.pradar.parser.MiddlewareType;
import io.shulie.surge.data.deploy.pradar.parser.PradarLogType;
import io.shulie.surge.data.runtime.common.DataOperations;
import io.shulie.surge.data.runtime.common.remote.DefaultValue;
import io.shulie.surge.data.runtime.common.remote.Remote;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 入口处理器
 *
 * @author vincent
 */
public class EntranceProcessor extends AbstractProcessor {
    private static final Logger logger = LoggerFactory.getLogger(EntranceProcessor.class);
    //远程调用/白名单保存表
    private static final String LINK_ENTRANCE_NAME = "t_amdb_pradar_link_entrance";

    @Inject
    private ClickhouseTemplateManager clickhouseTemplateManager;

    @Inject
    private MysqlSupport mysqlSupport;

    @Inject
    private TaskManager<String, TenantAppEntity> taskManager;

    /**
     * 是否开启入口功能扫描
     */
    @Inject
    @DefaultValue("true")
    @Named("/pradar/config/rt/entranceProcessDisable")
    private Remote<Boolean> entranceProcessDisable;

    /**
     * 间隔时间,默认两分钟
     */
    @Inject
    @DefaultValue("120")
    @Named("/pradar/config/rt/entranceProcess/delayTime")
    private Remote<Long> intervalTime;

    String linkEntranceInsertSql = "";
    String linkEntranceDeleteSql = "";

    private static final int DEFAULT_DELAY_TIME = 2;

    public void init(String dataSourceType) {
        //设置数据源
        this.setDataSourceType(dataSourceType);
        linkEntranceInsertSql = "INSERT INTO " + LINK_ENTRANCE_NAME + LinkEntranceModel.getCols() + " VALUES "
                + LinkEntranceModel.getParamCols() + LinkEntranceModel.getOnDuplicateCols();
        linkEntranceDeleteSql = "DELETE FROM " + LINK_ENTRANCE_NAME + " WHERE GMT_MODIFY<=?";
    }

    private Pair<String, String> getStartAndEndTime() {
        long now = System.currentTimeMillis();
        String startTime = DateFormatUtils.format(now - DEFAULT_DELAY_TIME * 60 * 1000, "yyyy-MM-dd HH:mm:ss");
        String endTime = DateFormatUtils.format(now - 5000, "yyyy-MM-dd HH:mm:ss");
        return new Pair<>(startTime, endTime);
    }

    /**
     * 解析任务不按照任务编号去分配
     * 该方法未有地方使用到,暂无需关注
     *
     * @throws IOException
     */
    public void noShare() {
        if (!entranceProcessDisable.get()) {
            return;
        }
        if (!isHandler(intervalTime.get())) {
            return;
        }
        Pair<String, String> timePair = getStartAndEndTime();
        List<TenantAppEntity> activeAppNames = queryActiveAppNames(timePair);
        if (!CollectionUtils.isEmpty(activeAppNames)) {
            activeAppNames.forEach(appEntity -> saveEntrance(appEntity, timePair));
        }
    }


    /**
     * 解析任务按照任务编号去分配
     * 该方法是在测试工程中使用的,业务代码未使用到,暂无需关注
     *
     * @throws IOException
     */
    @Override
    public void share(int taskId) {
        if (!entranceProcessDisable.get()) {
            return;
        }
        if (!isHandler(intervalTime.get())) {
            return;
        }
        if (taskId == -1) {
            return;
        }
        Pair<String, String> timePair = getStartAndEndTime();
        List<TenantAppEntity> activeAppNames = queryActiveAppNames(timePair);
        if (CollectionUtils.isEmpty(activeAppNames)) {
            activeAppNames.forEach(appEntity -> {
                if (appEntity.getAppName().hashCode() % taskId == 0) {
                    saveEntrance(appEntity, timePair);
                }
            });
        }
    }

    /**
     * 解析任务按照任务编号去分配
     * 该方法是在main方法测试中使用到的,,业务代码未使用到,暂无需关注
     *
     * @throws IOException
     */
    @Override
    public void share() {
        if (!entranceProcessDisable.get()) {
            return;
        }
        if (!isHandler(intervalTime.get())) {
            return;
        }
        Pair<String, String> timePair = getStartAndEndTime();
        List<TenantAppEntity> activeAppNames = queryActiveAppNames(timePair);
        if (!CollectionUtils.isEmpty(activeAppNames)) {
            activeAppNames.forEach(appEntity -> saveEntrance(appEntity, timePair));
        }
    }

    @Override
    public void init() {
    }

    /**
     * 保存链路入口信息
     *
     * @param taskIds
     * @param currentId
     * @throws IOException
     */
    @Override
    public void share(List<String> taskIds, String currentId) {
        if (!entranceProcessDisable.get()) {
            return;
        }
        if (!isHandler(intervalTime.get())) {
            return;
        }
        Pair<String, String> timePair = getStartAndEndTime();
        List<TenantAppEntity> activeAppNames = queryActiveAppNames(timePair);
        if (!CollectionUtils.isEmpty(activeAppNames)) {
            Map<String, List<TenantAppEntity>> idMap = taskManager.allotOfAverage(taskIds, activeAppNames);
            List<TenantAppEntity> avgList = idMap.get(currentId);
            if (!CollectionUtils.isEmpty(avgList)) {
                avgList.forEach(appEntity -> saveEntrance(appEntity, timePair));
            }
        }
    }


    // 遍历所有的数据源，获取到对应的活动的appName，然后通过appName再去查询对应的trace日志做出口的分析
    private List<TenantAppEntity> queryActiveAppNames(Pair<String, String> timePair) {
        // 统计当前时间往前两分钟到当前时间往前5s期间有服务端和入口日志的应用列表
        String sqlTemplate = "select userAppKey,envCode,appName from %s where startDate between '" + timePair.getFirst() + "' and '" + timePair.getSecond() + "' and logType in (1,3) group by userAppKey,envCode,appName";
        Map<String, ClickhouseTemplateHolder> templateMap = clickhouseTemplateManager.getQueryTemplateMap();
        if (templateMap.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<TenantAppEntity> result = new ArrayList<>();
        templateMap.forEach((key, value) -> {
            DataOperations template = value.getTemplate();
            String tableName = value.getTableName();
            try {
                List<TenantAppEntity> appEntities = template.query(String.format(sqlTemplate, tableName), new BeanPropertyRowMapper<>(TenantAppEntity.class));
                if (!CollectionUtils.isEmpty(appEntities)) {
                    result.addAll(appEntities);
                }
            } catch (Exception e) {
                logger.error("entranceProcessor query appNames error key=[{}]", key, e);
            }
        });
        return result;
    }

    public static final int SERVICE_LENGTH_FIELD = 256;

    /**
     * 保存链路入口信息
     * @param appEntity 租户应用实体
     * @param timePair 时间段
     */
    public void saveEntrance(TenantAppEntity appEntity, Pair<String, String> timePair) {
        String appName = appEntity.getAppName();
        //压测引擎的不处理
        if (appName.contains("pressure-engine")) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("saveEntrance:{},startTime,endTime:{}", appName, timePair);
        }
        try {
            List<Map<String, Object>> entranceMapList = queryEntrance(appEntity, timePair);
            if (CollectionUtils.isEmpty(entranceMapList)) {
                return;
            }
            String userAppKey = appEntity.getUserAppKey();
            String envCode = appEntity.getEnvCode();
            //对于serviceName和methodName超过256的入口,采取截断方式
            entranceMapList = entranceMapList.stream().map(entranceMap -> {
                //parsedMiddlewareName改名为middlewareName
                entranceMap.put("middlewareName", StringUtil.formatString(entranceMap.get("parsedMiddlewareName")));
                entranceMap.remove("parsedMiddlewareName");
                entranceMap.put("userAppKey", userAppKey);
                entranceMap.put("envCode", envCode);

                String oriServiceName = StringUtil.formatString(entranceMap.get("serviceName"));
                String oriMethodName = StringUtil.formatString(entranceMap.get("methodName"));
                if (oriServiceName.length() > SERVICE_LENGTH_FIELD) {
                    logger.warn("detect illegal service:{}", entranceMap);
                    entranceMap.put("serviceName", oriServiceName.substring(0, SERVICE_LENGTH_FIELD));
                }
                if (oriMethodName.length() > SERVICE_LENGTH_FIELD) {
                    logger.warn("detect illegal method:{}", entranceMap);
                    entranceMap.put("methodName", oriMethodName.substring(0, SERVICE_LENGTH_FIELD));
                }
                return entranceMap;
            }).collect(Collectors.toList());

            List<LinkEntranceModel> linkEntranceModels = entranceMapList.stream().map(
                            LinkEntranceModel::parseFromDataMap)
                    .collect(Collectors.toList()).stream().collect(Collectors.collectingAndThen(Collectors.toCollection(()
                            -> new TreeSet<>(Comparator.comparing(LinkEntranceModel::getEntranceId))), ArrayList::new));
            mysqlSupport.batchUpdate(linkEntranceInsertSql, new ArrayList<>(
                    linkEntranceModels.stream().map(LinkEntranceModel::getValues).collect(Collectors.toSet())));
            logger.info("{} saveEntrance is ok,size: {}", appName, entranceMapList.size());
        } catch (Throwable e) {
            logger.error("saveEntrance error!" + ExceptionUtils.getStackTrace(e));
            //ignore
        }
    }


    /**
     * 读取所有的应用入口
     * 对于rpcType是webServer或者是job类型,trace表里只有入口日志和服务器端日志,还有压测引擎产生的日志,无客户端日志
     * 对于logType是trace日志或者server日志,rpcType只有webServer、rpc、MQ类型
     * 综上,两种条件取并集,确实能涵盖服务类型是http,dubbo,rocketMQ,kafka,elasticjob几种
     *
     * @param appEntity 租户应用实体
     * @param timePair 时间段
     */
    public List<Map<String, Object>> queryEntrance(TenantAppEntity appEntity, Pair<String, String> timePair) {
        StringBuilder entranceSql = new StringBuilder();
        ClickhouseTemplateHolder holder = clickhouseTemplateManager.getTemplateHolder(appEntity.getUserAppKey(), appEntity.getEnvCode(), false);
        DataOperations template = holder.getTemplate();
        try {
            buildQueryCkSql(appEntity, holder.getTableName(), timePair, entranceSql);
            String executeSql = entranceSql.toString();
            logger.info("queryEntrance:{}", executeSql);
            return template.queryForList(executeSql);
        } catch (Throwable e) {
            logger.error("query queryEntrance error " + ExceptionUtils.getStackTrace(e));
        }
        return new ArrayList<>(0);
    }

    private void buildQueryCkSql(TenantAppEntity appEntity, String tableName, Pair<String, String> timePair, StringBuilder entranceSql) {
        //查询入口数据(HTTP/DUBBO/JOB)
        String appName = appEntity.getAppName();
        String userAppKey = appEntity.getUserAppKey();
        String envCode = appEntity.getEnvCode();
        entranceSql.append(String.format(SqlConstants.QUERY_ENTRANCE_SQL, tableName) + "startDate between '" + timePair.getFirst() + "' and '" + timePair.getSecond() + "' and appName='" + appName + "' and parsedServiceName != '' and " + "(rpcType in ('" + MiddlewareType.TYPE_WEB_SERVER + "','" + MiddlewareType.TYPE_JOB + "') or logType in ('" + PradarLogType.LOG_TYPE_RPC_SERVER + "','" + PradarLogType.LOG_TYPE_TRACE + "'))  and userAppKey = '" + userAppKey + "' and envCode = '" + envCode + "'  limit 100 ");
    }

    /**
     * 失效数据
     *
     * @throws IOException
     */
    public void shareExpire(int expirtTime) {
        expireData(DateUtils.addDays(new Date(), -expirtTime));
    }

    /**
     * 失效过期数据
     *
     * @param expireDate
     * @return
     */
    void expireData(Date expireDate) {
        try {
            mysqlSupport.update(linkEntranceDeleteSql, new Object[]{expireDate});
        } catch (Throwable e) {
            logger.error("query appNames error " + ExceptionUtils.getStackTrace(e));
        }
    }
}
