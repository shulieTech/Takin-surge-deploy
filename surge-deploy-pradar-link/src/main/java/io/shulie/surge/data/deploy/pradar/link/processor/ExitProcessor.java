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


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.shulie.surge.config.clickhouse.ClickhouseTemplateHolder;
import io.shulie.surge.config.clickhouse.ClickhouseTemplateManager;
import io.shulie.surge.config.common.model.TenantAppEntity;
import io.shulie.surge.data.common.utils.Pair;
import io.shulie.surge.data.deploy.pradar.link.TaskManager;
import io.shulie.surge.data.deploy.pradar.link.constants.SqlConstants;
import io.shulie.surge.data.deploy.pradar.link.model.LinkEntranceModel;
import io.shulie.surge.data.deploy.pradar.link.util.StringUtil;
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
 * 出口(远程调用)梳理
 *
 * @author sunshiyu
 */
public class ExitProcessor extends AbstractProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ExitProcessor.class);

    //远程调用/白名单保存表
    private static final String LINK_ENTRANCE_NAME = "t_amdb_pradar_link_entrance";

    @Inject
    private ClickhouseTemplateManager clickhouseTemplateManager;

    @Inject
    private MysqlSupport mysqlSupport;

    @Inject
    private TaskManager<String, TenantAppEntity> taskManager;

    /**
     * 是否开启出口(远程调用)功能扫描
     */
    @Inject
    @DefaultValue("true")
    @Named("/pradar/config/rt/exitProcessDisable")
    private Remote<Boolean> exitProcessDisable;

    /**
     * 间隔时间,默认两分钟
     */
    @Inject
    @DefaultValue("120")
    @Named("/pradar/config/rt/exitProcess/delayTime")
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
        if (!exitProcessDisable.get()) {
            return;
        }
        if (!isHandler(intervalTime.get())) {
            return;
        }
        Pair<String, String> timePair = getStartAndEndTime();
        List<TenantAppEntity> appEntities = queryActiveAppNames(timePair);
        if (!CollectionUtils.isEmpty(appEntities)) {
            appEntities.forEach(appEntity -> saveExit(appEntity, timePair));
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
        if (!exitProcessDisable.get()) {
            return;
        }
        if (!isHandler(intervalTime.get())) {
            return;
        }
        if (taskId == -1) {
            return;
        }
        Pair<String, String> timePair = getStartAndEndTime();
        List<TenantAppEntity> appEntities = queryActiveAppNames(timePair);
        if (!CollectionUtils.isEmpty(appEntities)) {
            appEntities.forEach(entity -> {
                if (entity.getAppName().hashCode() % taskId == 0) {
                    saveExit(entity, timePair);
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
        if (!exitProcessDisable.get()) {
            return;
        }
        if (!isHandler(intervalTime.get())) {
            return;
        }
        Pair<String, String> timePair = getStartAndEndTime();
        List<TenantAppEntity> appEntities = queryActiveAppNames(timePair);
        if (!CollectionUtils.isEmpty(appEntities)) {
            appEntities.forEach(appEntity -> saveExit(appEntity, timePair));
        }
    }

    @Override
    public void init() {
    }

    /**
     * 保存链路出口(远程调用)/出口信息
     *
     * @param taskIds
     * @param currentId
     * @throws IOException
     */
    @Override
    public void share(List<String> taskIds, String currentId) {
        if (!exitProcessDisable.get()) {
            return;
        }
        if (!isHandler(intervalTime.get())) {
            return;
        }
        Pair<String, String> timePair = getStartAndEndTime();
        List<TenantAppEntity> appEntities = queryActiveAppNames(timePair);
        if (!CollectionUtils.isEmpty(appEntities)) {
            Map<String, List<TenantAppEntity>> idMap = taskManager.allotOfAverage(taskIds, appEntities);
            List<TenantAppEntity> avgList = idMap.get(currentId);
            if (!CollectionUtils.isEmpty(avgList)) {
                avgList.forEach(entity -> saveExit(entity, timePair));
            }
        }
    }

    private static final int SERVICE_LENGTH_FIELD = 256;

    /**
     * 保存链路出口(远程调用)/出口信息
     * @param appEntity：租户应用实体
     * @param timePair 时间段
     */
    public void saveExit(TenantAppEntity appEntity, Pair<String, String> timePair) {
        //压测引擎的不处理
        String appName = appEntity.getAppName();
        if (appName.contains("pressure-engine")) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("saveExit:{},startTime,endTime:{}", appName, timePair);
        }
        try {
            List<Map<String, Object>> exitMapList = queryExit(appEntity, timePair);
            if (CollectionUtils.isEmpty(exitMapList)) {
                return;
            }
            String userAppKey = appEntity.getUserAppKey();
            String envCode = appEntity.getEnvCode();
            //对于serviceName和methodName超过256的出口(远程调用),采取截断方式
            exitMapList = exitMapList.stream().map(exitMap -> {
                exitMap.put("userAppKey", userAppKey);
                exitMap.put("envCode", envCode);

                String oriServiceName = StringUtil.formatString(exitMap.get("serviceName"));
                String oriMethodName = StringUtil.formatString(exitMap.get("methodName"));
                if (oriServiceName.length() > SERVICE_LENGTH_FIELD) {
                    logger.warn("detect illegal service:{}", exitMap);
                    exitMap.put("serviceName", oriServiceName.substring(0, SERVICE_LENGTH_FIELD));
                }
                if (oriMethodName.length() > SERVICE_LENGTH_FIELD) {
                    logger.warn("detect illegal method:{}", exitMap);
                    exitMap.put("methodName", oriMethodName.substring(0, SERVICE_LENGTH_FIELD));
                }
                return exitMap;
            }).collect(Collectors.toList());

            List<LinkEntranceModel> linkEntranceModels = exitMapList.stream().map(
                            LinkEntranceModel::parseFromDataMap)
                    .collect(Collectors.toList()).stream().collect(Collectors.collectingAndThen(Collectors.toCollection(()
                            -> new TreeSet<>(Comparator.comparing(LinkEntranceModel::getEntranceId))), ArrayList::new));
            mysqlSupport.batchUpdate(linkEntranceInsertSql, new ArrayList<>(
                    linkEntranceModels.stream().map(LinkEntranceModel::getValues).collect(Collectors.toSet())));
            logger.info("{} saveExit is ok,size: {}", appName, exitMapList.size());
        } catch (Throwable e) {
            logger.error("saveExit error!" + ExceptionUtils.getStackTrace(e));
            //ignore
        }
    }


    /**
     * @param appEntity  租户应用实体
     * @param timePair 时间段
     * @return 出口数据
     */
    public List<Map<String, Object>> queryExit(TenantAppEntity appEntity, Pair<String, String> timePair) {
        List<Map<String, Object>> result = Lists.newArrayList();
        List<Map<String, Object>> exitList;
        Map<String, Map<String, Object>> clientMap = Maps.newHashMap();
        Map<String, Map<String, Object>> serverMap = Maps.newHashMap();
        StringBuilder exitSql = new StringBuilder();
        String userAppKey = appEntity.getUserAppKey();
        String envCode = appEntity.getEnvCode();
        ClickhouseTemplateHolder holder = clickhouseTemplateManager.getTemplateHolder(userAppKey, envCode, false);
        DataOperations template = holder.getTemplate();
        String tableName = holder.getTableName();
        try {
            if (this.isUseCk()) {
                buildQueryCkSql(appEntity, tableName, timePair, exitSql);
            } else {
                buildQueryMysqlSql(appEntity, tableName, timePair, exitSql);
            }
            exitList = template.queryForList(exitSql.toString());
            logger.info("queryExit:{}", exitSql);

            //利用HTTP出口(远程调用)反向生成HTTP出口
            exitList = exitList.stream().filter(exit -> {
                //parsedMiddlewareName改名为middlewareName
                exit.put("middlewareName", StringUtil.formatString(exit.get("parsedMiddlewareName")));
                exit.remove("parsedMiddlewareName");

                //过滤所有非默认白名单(flagMessage为空)的出口和所有上游应用名称是目前客户端应用的服务端出口(远程调用)
                if ("server".equals(exit.get("linkType"))) {
                    String key = StringUtil.formatString(exit.get("serviceName")) + "#" + StringUtil.formatString(exit.get("methodName"));
                    serverMap.put(key, exit);
                    return false;
                }
                if ("client".equals(exit.get("linkType"))) {
                    String key = StringUtil.formatString(exit.get("serviceName")) + "#" + StringUtil.formatString(exit.get("methodName")) + "#" + StringUtil.formatString(exit.get("middlewareDetail"));
                    //设置为远程调用
                    exit.put("linkType", "1");
                    //对于远程调用,上游应用名称不需要,给空
                    exit.put("upAppName", "");
                    clientMap.put(key, exit);
                    return false;
                }
                return true;
            }).collect(Collectors.toList());

            //保存dubbo/feign/grpc等rpc类型的远程调用以及默认白名单远程调用
            if (!exitList.isEmpty()) {
                result.addAll(exitList);
            }

            clientMap.forEach((key, value) -> {
                //遍历所有出口,匹配入口
                if (serverMap.containsKey(key.substring(0, key.lastIndexOf("#")))) {
                    //如果有对应的服务端,用服务端日志的parsedServiceName替换客户端日志
                    value.put("serviceName", serverMap.get(key.substring(0, key.lastIndexOf("#"))).get("defaultWhiteInfo"));
                    //更新下游应用为服务端应用
                    value.put("downAppName", serverMap.get(key.substring(0, key.lastIndexOf("#"))).get("appName"));
                } else {
                    logger.info("external service invoke detect:{}", value);
                }
                //使用defaultWhiteInfo临时保存了parsedServiceName,为了不影响结果,还需要置为空
                value.put("defaultWhiteInfo", "");
                result.add(value);
            });

            return result;
        } catch (Throwable e) {
            logger.error("query queryEntrance error " + ExceptionUtils.getStackTrace(e));
        }
        return new ArrayList<>(0);
    }

    private void buildQueryCkSql(TenantAppEntity appEntity, String tableName, Pair<String, String> timePair, StringBuilder exitSql) {
        String appName = appEntity.getAppName();
        String userAppKey = appEntity.getUserAppKey();
        String envCode = appEntity.getEnvCode();
        //查询DUBBO,FEIGN以及GRPC的出口数据
        exitSql.append(String.format(SqlConstants.QUERY_EXIT_SQL, tableName) + "startDate between '" + timePair.getFirst() + "' and '" + timePair.getSecond() + "' and appName='" + appName + "' and parsedServiceName != '' and logType=2 and parsedMiddlewareName in ('DUBBO','FEIGN','GRPC') and userAppKey = '" + userAppKey + "' and envCode = '" + envCode + "' limit 100 ").append(SqlConstants.UNION_ALL);
        //查询默认白名单(flagMessage不为空)的HTTP出口数据
        exitSql.append(String.format(SqlConstants.QUERY_DEFAULT_WHITE_SQL, tableName) + "startDate between '" + timePair.getFirst() + "' and '" + timePair.getSecond() + "' and appName='" + appName + "' and parsedServiceName != '' and logType=2 and parsedMiddlewareName = 'HTTP' and flagMessage != '' and userAppKey = '" + userAppKey + "' and envCode = '" + envCode + "' limit 100 ").append(SqlConstants.UNION_ALL);
        //查询所有非默认白名单(flagMessage为空)的出口(2分钟前到现在往前5s)和所有上游应用名称是目前客户端应用的服务端出口(远程调用)
        //可能存在客户端日志已经产生了,但是服务端日志还没有产生或者写入ck,此时会把这种有服务端的日志标识成第三方服务
        exitSql.append(String.format(SqlConstants.QUERY_ALL_SQL, tableName) + " (( startDate between '" + timePair.getFirst() + "' and '" + timePair.getSecond() + "' and appName='" + appName + "' and logType=2 and flagMessage='') or (startDate >= '" + timePair.getFirst() + "' and upAppName='" + appName + "' and logType=3 )) and parsedServiceName != '' and parsedMiddlewareName = 'HTTP' and userAppKey = '" + userAppKey + "' and envCode = '" + envCode + "' ");
    }

    private void buildQueryMysqlSql(TenantAppEntity appEntity, String tableName, Pair<String, String> timePair, StringBuilder exitSql) {
        /**
         * mysql的UNION ALL必须把语句用括号包裹
         */
        String appName = appEntity.getAppName();
        String userAppKey = appEntity.getUserAppKey();
        String envCode = appEntity.getEnvCode();
        //查询DUBBO,FEIGN以及GRPC的出口数据
        exitSql.append(SqlConstants.BRACKETS_LEFT).append(String.format(SqlConstants.QUERY_EXIT_SQL,tableName) + "startDate between '" + timePair.getFirst() + "' and '" + timePair.getSecond() + "' and appName='" + appName + "' and parsedServiceName != '' and logType=2 and parsedMiddlewareName in ('DUBBO','FEIGN','GRPC') and userAppKey = '" + userAppKey + "' and envCode = '" + envCode + "'  limit 100 ").append(SqlConstants.BRACKETS_RIGHT).append(SqlConstants.UNION_ALL);
        //查询默认白名单(flagMessage不为空)的HTTP出口数据
        exitSql.append(SqlConstants.BRACKETS_LEFT).append(String.format(SqlConstants.QUERY_DEFAULT_WHITE_SQL,tableName) + "startDate between '" + timePair.getFirst() + "' and '" + timePair.getSecond() + "' and appName='" + appName + "' and parsedServiceName != '' and logType=2 and parsedMiddlewareName = 'HTTP' and flagMessage != '' and userAppKey = '" + userAppKey + "' and envCode = '" + envCode + "'  limit 100 ").append(SqlConstants.BRACKETS_RIGHT).append(SqlConstants.UNION_ALL);
        //查询所有非默认白名单(flagMessage为空)的出口(2分钟前到现在往前5s)和所有上游应用名称是目前客户端应用的服务端出口(远程调用)
        //可能存在客户端日志已经产生了,但是服务端日志还没有产生或者写入ck,此时会把这种有服务端的日志标识成第三方服务
        exitSql.append(SqlConstants.BRACKETS_LEFT).append(String.format(SqlConstants.QUERY_ALL_SQL,tableName) + " (( startDate between '" + timePair.getFirst() + "' and '" + timePair.getSecond() + "' and appName='" + appName + "' and logType=2 and flagMessage='') or (startDate >= '" + timePair.getFirst() + "' and upAppName='" + appName + "' and logType=3 )) and parsedServiceName != '' and parsedMiddlewareName = 'HTTP' and userAppKey = '" + userAppKey + "' and envCode = '" + envCode + "' ").append(SqlConstants.BRACKETS_RIGHT);
    }

    // 遍历所有的数据源，获取到对应的活动的appName，然后通过appName再去查询对应的trace日志做出口的分析
    private List<TenantAppEntity> queryActiveAppNames(Pair<String, String> timePair) {
        String sqlTemplate = "select appName,userAppKey,envCode from %s where startDate between '" + timePair.getFirst() + "' and '" + timePair.getSecond() + "' and logType=2 group by userAppKey,envCode,appName";
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
                logger.error("exitProcessor query appNames error key=[{}]", key, e);
            }
        });
        return result;
    }
}
