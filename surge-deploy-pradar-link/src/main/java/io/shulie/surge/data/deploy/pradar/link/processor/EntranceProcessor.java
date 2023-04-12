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
import io.shulie.surge.data.common.utils.Pair;
import io.shulie.surge.data.deploy.pradar.link.TaskManager;
import io.shulie.surge.data.deploy.pradar.link.constants.SqlConstants;
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
 * 入口处理器
 *
 * @author vincent
 */
public class EntranceProcessor extends AbstractProcessor {
    private static final Logger logger = LoggerFactory.getLogger(EntranceProcessor.class);
    //远程调用/白名单保存表
    private static final String LINK_ENTRANCE_NAME = "t_amdb_pradar_link_entrance";

    @Inject
    private ClickHouseSupport clickHouseSupport;

    @Inject
    private MysqlSupport mysqlSupport;

    @Inject
    private TaskManager<String> taskManager;

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

    /**
     * 远程调用,默认
     */
    @Inject
    @DefaultValue("2")
    @Named("/pradar/config/rt/entranceProcess/expireDays")
    private Remote<String> expireDays;

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

    private Pair<String, String> getStartAndEndTime() {
        long now = System.currentTimeMillis();
        //需要加上间隔时间，目前出口是间隔一分钟
        String startTime = DateFormatUtils.format(
                now - ((intervalTime.get()) * 1000), "yyyy-MM-dd HH:mm:ss");
        String endTime = DateFormatUtils.format(
                now, "yyyy-MM-dd HH:mm:ss");
        return new Pair<>(startTime, endTime);
    }

    /**
     * 解析任务不按照任务编号去分配
     * 该方法未有地方使用到,暂无需关注
     *
     * @throws IOException
     */
    public void noShare() {
        if (entranceProcessDisable.get()) {
            return;
        }
        if (!isHandler(intervalTime.get(), 60)) {
            return;
        }
        Pair<String, String> timePair = getStartAndEndTime();
        List<Map<String, Object>> appNames = queryAppNames(timePair);
        if (CollectionUtils.isNotEmpty(appNames)) {
            appNames.stream().forEach(appNameMap -> {
                String key = appNameMap.get("userAppKey") + "#" + appNameMap.get("envCode") + "#" + appNameMap.get("appName");
                saveEntrance(key, timePair);
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
        if (entranceProcessDisable.get()) {
            return;
        }
        if (!isHandler(intervalTime.get(), 60)) {
            return;
        }
        if (taskId == -1) {
            return;
        }
        Pair<String, String> timePair = getStartAndEndTime();
        List<Map<String, Object>> appNames = queryAppNames(timePair);
        if (CollectionUtils.isNotEmpty(appNames)) {
            appNames.stream().forEach(appNameMap -> {
                String key = appNameMap.get("userAppKey") + "#" + appNameMap.get("envCode") + "#" + appNameMap.get("appName");
                if (key.hashCode() % taskId == 0) {
                    saveEntrance(key, timePair);
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
        if (entranceProcessDisable.get()) {
            return;
        }
        if (!isHandler(intervalTime.get(), 60)) {
            return;
        }
        Pair<String, String> timePair = getStartAndEndTime();
        List<Map<String, Object>> appNames = queryAppNames(timePair);
        if (CollectionUtils.isNotEmpty(appNames)) {
            appNames.stream().forEach(appNameMap -> {
                String key = appNameMap.get("userAppKey") + "#" + appNameMap.get("envCode") + "#" + appNameMap.get("appName");
                saveEntrance(key, timePair);
            });
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
        if (entranceProcessDisable.get()) {
            return;
        }
        if (!isHandler(intervalTime.get(), 60)) {
            return;
        }
        Pair<String, String> timePair = getStartAndEndTime();
        List<Map<String, Object>> appNames = queryAppNames(timePair);
        if (CollectionUtils.isNotEmpty(appNames)) {
            Map<String, List<Map<String, Object>>> nameMap = appNames.stream().collect(
                    Collectors.groupingBy(name -> name.get("userAppKey") + "#" + name.get("envCode") + "#" + name.get("appName")));
            List<String> nameList = new ArrayList<>(nameMap.keySet());
            List<String> avgList = taskManager.allotOfAverage(nameList);
            if (CollectionUtils.isNotEmpty(avgList)) {
                avgList.stream().forEach(avg -> {
                    Optional<Map<String, Object>> appMap = nameMap.get(avg).stream().findFirst();
                    if (appMap.isPresent()) {
                        saveEntrance(appMap.get().get("userAppKey") + "#" + appMap.get().get("envCode") + "#" + appMap.get().get("appName"), timePair);
                    }
                });
            }
        }
    }

    /**
     * 读取所有的应用名
     *
     * @param timePair
     * @return
     */
    public List<Map<String, Object>> queryAppNames(Pair timePair) {
        try {
            //统计当前时间往前两分钟到当前时间往前5s期间有服务端和入口日志的应用列表
            String appNameSql = "select userAppKey,envCode,appName from t_trace_all where startDate between '" + timePair.getFirst() + "' and '" + timePair.getSecond() + "' and logType in (1,3) and clusterTest = '0' group by userAppKey,envCode,appName";
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

    public static final int SERVICE_LENGTH_FIELD = 256;

    /**
     * 保存链路入口信息
     */
    public void saveEntrance(String key, Pair timePair) {
        //压测引擎的不处理
        if (key.contains("pressure-engine")) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("saveEntrance:{},startTime,endTime:{}", key, timePair);
        }
        try {
            List<Map<String, Object>> entranceMapList = queryEntrance(key, timePair);
            if (CollectionUtils.isEmpty(entranceMapList)) {
                return;
            }
            //对于serviceName和methodName超过256的入口,采取截断方式
            entranceMapList = entranceMapList.stream().map(entranceMap -> {
                //parsedMiddlewareName改名为middlewareName
                entranceMap.put("middlewareName", StringUtil.formatString(entranceMap.get("parsedMiddlewareName")));
                entranceMap.remove("parsedMiddlewareName");
                entranceMap.put("userAppKey", key.split("#")[0]);
                entranceMap.put("envCode", key.split("#")[1]);

                String oriServiceName = StringUtil.formatString(entranceMap.get("serviceName"));
                if (oriServiceName.endsWith("/")){
                    oriServiceName = oriServiceName.substring(0, oriServiceName.length() -1);
                }
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
            mysqlSupport.addBatch(linkEntranceInsertSql, new ArrayList<>(
                    linkEntranceModels.stream().map(LinkEntranceModel::getValues).collect(Collectors.toSet())));
            logger.info("{} saveEntrance is ok,size: {}", key, entranceMapList.size());
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
     * @param key,timePair
     */
    public List<Map<String, Object>> queryEntrance(String key, Pair timePair) {
        List<Map<String, Object>> entranceList;
        StringBuilder entranceSql = new StringBuilder();
        String[] arr = key.split("#");
        String userAppKey = arr[0];
        String envCode = arr[1];
        String appName = arr[2];
        try {
            buildQueryCkSql(appName, userAppKey, envCode, timePair, entranceSql);
            logger.info("queryEntrance:{}", entranceSql);
            if (this.isUseCk()) {
                entranceList = clickHouseSupport.queryForList(entranceSql.toString());
            } else {
                entranceList = mysqlSupport.queryForList(entranceSql.toString());
            }
            return entranceList;
        } catch (Throwable e) {
            logger.error("query queryEntrance error " + ExceptionUtils.getStackTrace(e));
        }
        return Collections.EMPTY_LIST;
    }

    private void buildQueryCkSql(String appName, String userAppKey, String envCode, Pair timePair, StringBuilder entranceSql) {
        //查询入口数据(HTTP/DUBBO/JOB)
        //排除压测流量
        entranceSql.append(SqlConstants.QUERY_ENTRANCE_SQL + "startDate between '" + timePair.getFirst() + "' and '" + timePair.getSecond() + "' and appName='" + appName + "' and parsedServiceName != '' and clusterTest = '0' and " + "(rpcType in ('" + MiddlewareType.TYPE_WEB_SERVER + "','" + MiddlewareType.TYPE_JOB + "') or logType in ('" + PradarLogType.LOG_TYPE_RPC_SERVER + "','" + PradarLogType.LOG_TYPE_TRACE + "'))  and userAppKey = '" + userAppKey + "' and envCode = '" + envCode + "'  limit 100 ");
    }

    /**
     * 失效数据
     *
     * @throws IOException
     */
    public void shareExpire() {
        expireData(DateUtils.addDays(new Date(), -Integer.parseInt(expireDays.get())));
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
