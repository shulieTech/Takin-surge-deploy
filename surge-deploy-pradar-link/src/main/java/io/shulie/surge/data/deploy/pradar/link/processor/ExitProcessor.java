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
import io.shulie.surge.data.common.utils.Pair;
import io.shulie.surge.data.deploy.pradar.link.TaskManager;
import io.shulie.surge.data.deploy.pradar.link.constants.SqlConstants;
import io.shulie.surge.data.deploy.pradar.link.model.LinkEntranceModel;
import io.shulie.surge.data.deploy.pradar.link.util.StringUtil;
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
 * 出口(远程调用)梳理
 *
 * @author sunshiyu
 */
public class ExitProcessor extends AbstractProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ExitProcessor.class);

    //远程调用/白名单保存表
    private static final String LINK_ENTRANCE_NAME = "t_amdb_pradar_link_entrance";

    @Inject
    private ClickHouseSupport clickHouseSupport;

    @Inject
    private MysqlSupport mysqlSupport;

    @Inject
    private TaskManager<String, String> taskManager;

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
        String startTime = DateFormatUtils.format(
                now - DEFAULT_DELAY_TIME * 60 * 1000, "yyyy-MM-dd HH:mm:ss");
        String endTime = DateFormatUtils.format(
                now - 5000, "yyyy-MM-dd HH:mm:ss");
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
        List<Map<String, Object>> appNames = queryAppNames(timePair);
        if (CollectionUtils.isNotEmpty(appNames)) {
            appNames.stream().forEach(appNameMap -> {
                String key = appNameMap.get("userAppKey") + "#" + appNameMap.get("envCode") + "#" + appNameMap.get("appName");
                saveExit(key, timePair);
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
        List<Map<String, Object>> appNames = queryAppNames(timePair);
        if (CollectionUtils.isNotEmpty(appNames)) {
            appNames.stream().forEach(appNameMap -> {
                String key = appNameMap.get("userAppKey") + "#" + appNameMap.get("envCode") + "#" + appNameMap.get("appName");
                if (key.hashCode() % taskId == 0) {
                    saveExit(key, timePair);
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
        List<Map<String, Object>> appNames = queryAppNames(timePair);
        if (CollectionUtils.isNotEmpty(appNames)) {
            appNames.stream().forEach(appNameMap -> {
                String key = appNameMap.get("userAppKey") + "#" + appNameMap.get("envCode") + "#" + appNameMap.get("appName");
                saveExit(key, timePair);
            });
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
        List<Map<String, Object>> appNames = queryAppNames(timePair);
        if (CollectionUtils.isNotEmpty(appNames)) {
            Map<String, List<Map<String, Object>>> nameMap = appNames.stream().collect(
                    Collectors.groupingBy(name -> name.get("userAppKey") + "#" + name.get("envCode") + "#" + name.get("appName")));
            List<String> nameList = new ArrayList<>(nameMap.keySet());
            Map<String, List<String>> idMap = taskManager.allotOfAverage(taskIds, nameList);
            List<String> avgList = idMap.get(currentId);
            if (CollectionUtils.isNotEmpty(avgList)) {
                avgList.stream().forEach(avg -> {
                    Optional<Map<String, Object>> appMap = nameMap.get(avg).stream().findFirst();
                    if (appMap.isPresent()) {
                        saveExit(appMap.get().get("userAppKey") + "#" + appMap.get().get("envCode") + "#" + appMap.get().get("appName"), timePair);
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
            //统计当前时间往前两分钟到当前时间往前5s期间所有远程调用日志的应用列表
            String appNameSql = "select userAppKey,envCode,appName  from t_trace_all where startDate between '" + timePair.getFirst() + "' and '" + timePair.getSecond() + "' and logType=2 and clusterTest = '0' group by userAppKey,envCode,appName";
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
     * 保存链路出口(远程调用)/出口信息
     */
    public void saveExit(String key, Pair timePair) {
        //压测引擎的不处理
        if (key.contains("pressure-engine")) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("saveExit:{},startTime,endTime:{}", key, timePair);
        }
        try {
            List<Map<String, Object>> exitMapList = queryExit(key, timePair);
            if (CollectionUtils.isEmpty(exitMapList)) {
                return;
            }
            //对于serviceName和methodName超过256的出口(远程调用),采取截断方式
            exitMapList = exitMapList.stream().map(exitMap -> {
                exitMap.put("userAppKey", key.split("#")[0]);
                exitMap.put("envCode", key.split("#")[1]);

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
            logger.info("{} saveExit is ok,size: {}", key, exitMapList.size());
        } catch (Throwable e) {
            logger.error("saveExit error!" + ExceptionUtils.getStackTrace(e));
            //ignore
        }
    }


    /**
     * @param appNameKey,timePair
     * @return
     */
    public List<Map<String, Object>> queryExit(String appNameKey, Pair timePair) {
        List<Map<String, Object>> result = Lists.newArrayList();
        List<Map<String, Object>> exitList;
        Map<String, Map<String, Object>> clientMap = Maps.newHashMap();
        Map<String, Map<String, Object>> serverMap = Maps.newHashMap();
        StringBuilder exitSql = new StringBuilder();

        String[] arr = appNameKey.split("#");
        String userAppKey = arr[0];
        String envCode = arr[1];
        String appName = arr[2];

        try {
            if (this.isUseCk()) {
                buildQueryCkSql(appName, userAppKey, envCode, timePair, exitSql);
                exitList = clickHouseSupport.queryForList(exitSql.toString());
            } else {
                buildQueryMysqlSql(appName, userAppKey, envCode, timePair, exitSql);
                exitList = mysqlSupport.queryForList(exitSql.toString());
            }
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
                    //fix 对于服务端接不了探针的第三方应用,如果配置了出口规则,则需要用出口规则替换
                    value.put("serviceName", value.get("defaultWhiteInfo"));
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
        return Collections.EMPTY_LIST;
    }

    private void buildQueryCkSql(String appName, String userAppKey, String envCode, Pair timePair, StringBuilder exitSql) {
        //查询DUBBO,FEIGN以及GRPC的出口数据
        exitSql.append(SqlConstants.QUERY_EXIT_SQL + "startDate between '" + timePair.getFirst() + "' and '" + timePair.getSecond() + "' and appName='" + appName + "' and parsedServiceName != '' and logType=2 and clusterTest = '0' and parsedMiddlewareName in ('DUBBO','FEIGN','GRPC') and userAppKey = '" + userAppKey + "' and envCode = '" + envCode + "' limit 100 ").append(SqlConstants.UNION_ALL);
        //查询默认白名单(flagMessage不为空)的HTTP出口数据
        exitSql.append(SqlConstants.QUERY_DEFAULT_WHITE_SQL + "startDate between '" + timePair.getFirst() + "' and '" + timePair.getSecond() + "' and appName='" + appName + "' and parsedServiceName != '' and logType=2 and clusterTest = '0' and parsedMiddlewareName = 'HTTP' and flagMessage != '' and userAppKey = '" + userAppKey + "' and envCode = '" + envCode + "' limit 100 ").append(SqlConstants.UNION_ALL);
        //查询所有非默认白名单(flagMessage为空)的出口(2分钟前到现在往前5s)和所有上游应用名称是目前客户端应用的服务端出口(远程调用)
        //可能存在客户端日志已经产生了,但是服务端日志还没有产生或者写入ck,此时会把这种有服务端的日志标识成第三方服务
        exitSql.append(SqlConstants.QUERY_ALL_SQL + " (( startDate between '" + timePair.getFirst() + "' and '" + timePair.getSecond() + "' and appName='" + appName + "' and logType=2 and flagMessage='') or (startDate >= '" + timePair.getFirst() + "' and upAppName='" + appName + "' and logType=3 )) and parsedServiceName != '' and parsedMiddlewareName = 'HTTP' and clusterTest = '0' and userAppKey = '" + userAppKey + "' and envCode = '" + envCode + "' ");
    }

    private void buildQueryMysqlSql(String appName, String userAppKey, String envCode, Pair timePair, StringBuilder exitSql) {
        /**
         * mysql的UNION ALL必须把语句用括号包裹
         */

        //查询DUBBO,FEIGN以及GRPC的出口数据
        exitSql.append(SqlConstants.BRACKETS_LEFT).append(SqlConstants.QUERY_EXIT_SQL + "startDate between '" + timePair.getFirst() + "' and '" + timePair.getSecond() + "' and appName='" + appName + "' and parsedServiceName != '' and logType=2 and clusterTest = '0' and parsedMiddlewareName in ('DUBBO','FEIGN','GRPC') and userAppKey = '" + userAppKey + "' and envCode = '" + envCode + "'  limit 100 ").append(SqlConstants.BRACKETS_RIGHT).append(SqlConstants.UNION_ALL);
        //查询默认白名单(flagMessage不为空)的HTTP出口数据
        exitSql.append(SqlConstants.BRACKETS_LEFT).append(SqlConstants.QUERY_DEFAULT_WHITE_SQL + "startDate between '" + timePair.getFirst() + "' and '" + timePair.getSecond() + "' and appName='" + appName + "' and parsedServiceName != '' and logType=2 and clusterTest = '0' and parsedMiddlewareName = 'HTTP' and flagMessage != '' and userAppKey = '" + userAppKey + "' and envCode = '" + envCode + "'  limit 100 ").append(SqlConstants.BRACKETS_RIGHT).append(SqlConstants.UNION_ALL);
        //查询所有非默认白名单(flagMessage为空)的出口(2分钟前到现在往前5s)和所有上游应用名称是目前客户端应用的服务端出口(远程调用)
        //可能存在客户端日志已经产生了,但是服务端日志还没有产生或者写入ck,此时会把这种有服务端的日志标识成第三方服务
        exitSql.append(SqlConstants.BRACKETS_LEFT).append(SqlConstants.QUERY_ALL_SQL + " (( startDate between '" + timePair.getFirst() + "' and '" + timePair.getSecond() + "' and appName='" + appName + "' and logType=2 and flagMessage='') or (startDate >= '" + timePair.getFirst() + "' and upAppName='" + appName + "' and logType=3 )) and parsedServiceName != '' and parsedMiddlewareName = 'HTTP' and clusterTest = '0' and userAppKey = '" + userAppKey + "' and envCode = '" + envCode + "' ").append(SqlConstants.BRACKETS_RIGHT);
    }

}
