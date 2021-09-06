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
import io.shulie.surge.data.common.utils.DateUtils;
import io.shulie.surge.data.deploy.pradar.link.TaskManager;
import io.shulie.surge.data.deploy.pradar.link.model.LinkEntranceModel;
import io.shulie.surge.data.deploy.pradar.link.util.StringUtil;
import io.shulie.surge.data.deploy.pradar.parser.MiddlewareType;
import io.shulie.surge.data.deploy.pradar.parser.PradarLogType;
import io.shulie.surge.data.runtime.common.remote.DefaultValue;
import io.shulie.surge.data.runtime.common.remote.Remote;
import io.shulie.surge.data.sink.clickhouse.ClickHouseSupport;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 入口/出口处理器
 *
 * @author vincent
 */
public class EntranceProcessor extends AbstractProcessor {
    private static final Logger logger = LoggerFactory.getLogger(EntranceProcessor.class);

    //入口/出口表
    private static final String LINK_ENTRANCE_NAME = "t_amdb_pradar_link_entrance";

    @Inject
    private ClickHouseSupport clickHouseSupport;

    @Inject
    private MysqlSupport mysqlSupport;

    @Inject
    private TaskManager<String, String> taskManager;

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

    private static int DEFAULT_DELAY_TIME = 2;

    public void init(String dataSourceType) {
        //设置数据源
        this.setDataSourceType(dataSourceType);
        linkEntranceInsertSql = "INSERT INTO " + LINK_ENTRANCE_NAME + LinkEntranceModel.getCols() + " VALUES "
                + LinkEntranceModel.getParamCols() + LinkEntranceModel.getOnDuplicateCols();
        linkEntranceDeleteSql = "DELETE FROM " + LINK_ENTRANCE_NAME + " WHERE GMT_MODIFY<=?";
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
        Date queryTime = new Date(System.currentTimeMillis() - DEFAULT_DELAY_TIME * 60 * 1000);
        List<Map<String, Object>> appNames = queryAppNames(queryTime);
        if (CollectionUtils.isNotEmpty(appNames)) {
            appNames.stream().forEach(appNameMap -> {
                String appName = String.valueOf(appNameMap.get("appName"));
                saveLinkInfo(appName, queryTime);
            });
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
        Date queryTime = new Date(System.currentTimeMillis() - DEFAULT_DELAY_TIME * 60 * 1000);
        List<Map<String, Object>> appNames = queryAppNames(queryTime);
        if (CollectionUtils.isNotEmpty(appNames)) {
            appNames.stream().forEach(appNameMap -> {
                String appName = String.valueOf(appNameMap.get("appName"));
                if (appName.hashCode() % taskId == 0) {
                    saveLinkInfo(appName, queryTime);
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
        Date queryTime = new Date(System.currentTimeMillis() - DEFAULT_DELAY_TIME * 60 * 1000);
        List<Map<String, Object>> appNames = queryAppNames(queryTime);
        if (CollectionUtils.isNotEmpty(appNames)) {
            appNames.stream().forEach(appNameMap -> {
                String appName = String.valueOf(appNameMap.get("appName"));
                saveLinkInfo(appName, queryTime);
            });
        }
    }

    @Override
    public void init() {
    }

    /**
     * 保存链路入口/出口信息
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
        Date queryTime = new Date(System.currentTimeMillis() - DEFAULT_DELAY_TIME * 60 * 1000);
        List<Map<String, Object>> appNames = queryAppNames(queryTime);
        if (CollectionUtils.isNotEmpty(appNames)) {
            Map<String, List<Map<String, Object>>> nameMap = appNames.stream().collect(
                    Collectors.groupingBy(name -> String.valueOf(name.get("appName"))));
            List<String> nameList = new ArrayList<>(nameMap.keySet());
            Map<String, List<String>> idMap = taskManager.allotOfAverage(taskIds, nameList);
            List<String> avgList = idMap.get(currentId);
            if (CollectionUtils.isNotEmpty(avgList)) {
                avgList.stream().forEach(avg -> {
                    Optional<Map<String, Object>> appMap = nameMap.get(avg).stream().findFirst();
                    if (appMap.isPresent()) {
                        saveLinkInfo(String.valueOf(appMap.get().get("appName")), queryTime);
                    }
                });
            }
        }
    }

    /**
     * 读取所有的应用名
     *
     * @param queryTime
     * @return
     */
    public List<Map<String, Object>> queryAppNames(Date queryTime) {
        try {
            String appNameSql = "select appName from t_trace_all where startDate >= '" + DateFormatUtils.format(
                    queryTime, "yyyy-MM-dd HH:mm:ss") + "' group by appName";
            if (isUseCk()) {
                return clickHouseSupport.queryForList(appNameSql);
            } else {
                return mysqlSupport.queryForList(appNameSql);
            }
        } catch (Throwable e) {
            logger.error("query appNames error " + ExceptionUtils.getStackTrace(e));
        }
        return Collections.EMPTY_LIST;
    }

    private static final int SERVICE_LENGTH_FIELD = 256;

    /**
     * 保存链路入口/出口信息
     */
    public void saveLinkInfo(String appName, Date queryTime) {
        logger.info("saveLinkInfo:{},queryTime:{}", appName, queryTime);
        try {
            // 按应用名去解析服务信息
            List<Map<String, Object>> entranceMapList = queryEntrance(appName, queryTime);
            if (CollectionUtils.isEmpty(entranceMapList)) {
                return;
            }
            //对于serviceName和methodName超过256的入口,采取截断方式
            entranceMapList = entranceMapList.stream().map(entranceMap -> {
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
            logger.info("saveLinkEntrance is ok {}", appName);
        } catch (Throwable e) {
            logger.error("Save to pradar_link_entrance error!" + ExceptionUtils.getStackTrace(e));
            //ignore
        }
    }


    /**
     * 读取所有的应用入口和出口
     * 对于rpcType是webServer或者是job类型,trace表里只有入口日志和服务器端日志,还有压测引擎产生的日志,无客户端日志
     * 对于logType是trace日志或者server日志,rpcType只有webServer、rpc、MQ类型
     * 综上,两种条件取并集,确实能涵盖服务类型是http,dubbo,rocketMQ,kafka,elasticjob几种
     * 针对6.4需求
     *
     * @param queryTime
     * @return
     */
    public List<Map<String, Object>> queryEntrance(String appName, Date queryTime) {

        try {
            //此处的limit 200可能存在问题,暂时还没想到该怎么处理这个
            //2021-05-24 如果serviceName为空,则不写入入口表,因为是无效入口
            //入口日志无需保存upAppName,只有服务端日志需要保存upAppName
            if (this.isUseCk()) {
                String entranceSql =
                        "select distinct parsedServiceName as serviceName,appName,rpcType,parsedMethod as methodName,"
                                + "parsedMiddlewareName as middlewareName,parsedExtend as extend,'0' as linkType,case when logType = '3' then upAppName else '' end as upAppName from t_trace_all "
                                + "where startDate >= '" + DateFormatUtils.format(queryTime, "yyyy-MM-dd HH:mm:ss")
                                + "'  and appName='" + appName + "' and serviceName != '' and " +
                                "(rpcType in ('" + MiddlewareType.TYPE_WEB_SERVER + "','" +
                                MiddlewareType.TYPE_JOB + "') or logType in ('" + PradarLogType.LOG_TYPE_RPC_SERVER + "','"
                                + PradarLogType.LOG_TYPE_TRACE + "')) limit 100 ";
                entranceSql += "union all ";
                //出口日志无需关注upAppName,给空即可
                //and parsedMiddlewareName in ('DUBBO','HTTP','FEIGN') 去除中间件类型过滤,开放所有类型出口
                entranceSql += "select distinct parsedServiceName as serviceName,appName,rpcType,parsedMethod as methodName,"
                        + "parsedMiddlewareName as middlewareName,parsedExtend as extend,'1' as linkType,'' as upAppName from t_trace_all "
                        + "where startDate >= '" + DateFormatUtils.format(queryTime, "yyyy-MM-dd HH:mm:ss")
                        + "'  and appName='" + appName + "' and serviceName != '' and logType = '2'  limit 100";
                logger.info("queryEntrance:{}", entranceSql);
                return clickHouseSupport.queryForList(entranceSql);
            } else {
                String entranceSql =
                        "(select distinct parsedServiceName as serviceName,appName,rpcType,parsedMethod as methodName,"
                                + "parsedMiddlewareName as middlewareName,parsedExtend as extend,'0' as linkType,case when logType = '3' then upAppName else '' end as upAppName from t_trace_all "
                                + "where startDate >= '" + DateFormatUtils.format(queryTime, "yyyy-MM-dd HH:mm:ss")
                                + "'  and appName='" + appName + "' and serviceName != '' and " +
                                "(rpcType in ('" + MiddlewareType.TYPE_WEB_SERVER + "','" +
                                MiddlewareType.TYPE_JOB + "') or logType in ('" + PradarLogType.LOG_TYPE_RPC_SERVER + "','"
                                + PradarLogType.LOG_TYPE_TRACE + "')) limit 100) ";
                entranceSql += "union all ";
                //出口日志无需关注upAppName,给空即可
                //and parsedMiddlewareName in ('DUBBO','HTTP','FEIGN') 去除中间件类型过滤,开放所有类型出口
                entranceSql += "(select distinct parsedServiceName as serviceName,appName,rpcType,parsedMethod as methodName,"
                        + "parsedMiddlewareName as middlewareName,parsedExtend as extend,'1' as linkType,'' as upAppName from t_trace_all "
                        + "where startDate >= '" + DateFormatUtils.format(queryTime, "yyyy-MM-dd HH:mm:ss")
                        + "'  and appName='" + appName + "' and serviceName != '' and logType = '2'  limit 100)";
                logger.info("queryEntrance:{}", entranceSql);
                return mysqlSupport.queryForList(entranceSql);
            }
        } catch (Throwable e) {
            logger.error("query queryEntrance error " + ExceptionUtils.getStackTrace(e));
        }
        return Collections.EMPTY_LIST;
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
