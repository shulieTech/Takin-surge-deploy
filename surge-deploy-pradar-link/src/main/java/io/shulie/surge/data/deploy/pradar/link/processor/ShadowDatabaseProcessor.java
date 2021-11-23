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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.shulie.surge.data.common.utils.Pair;
import io.shulie.surge.data.deploy.pradar.link.TaskManager;
import io.shulie.surge.data.deploy.pradar.link.enums.TraceLogQueryScopeEnum;
import io.shulie.surge.data.deploy.pradar.link.model.ShadowBizTableModel;
import io.shulie.surge.data.deploy.pradar.link.model.ShadowDatabaseModel;
import io.shulie.surge.data.deploy.pradar.link.model.TTrackClickhouseModel;
import io.shulie.surge.data.deploy.pradar.link.parse.ShadowDatabaseParseResult;
import io.shulie.surge.data.deploy.pradar.link.parse.TemplateParseHandler;
import io.shulie.surge.data.deploy.pradar.parser.MiddlewareType;
import io.shulie.surge.data.deploy.pradar.parser.utils.Md5Utils;
import io.shulie.surge.data.runtime.common.remote.DefaultValue;
import io.shulie.surge.data.runtime.common.remote.Remote;
import io.shulie.surge.data.sink.clickhouse.ClickHouseSupport;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShadowDatabaseProcessor extends AbstractProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ShadowDatabaseProcessor.class);

    private static final String SHADOW_DATABASE = "t_amdb_app_shadowdatabase";

    private static final String BIZ_TABLE = "t_amdb_app_shadowbiztable";

    /**
     * 是否开启影子库/表梳理功能
     */
    @Inject
    @DefaultValue("true")
    @Named("/pradar/config/rt/shadowDatabaseProcessDisable")
    private Remote<Boolean> analysisShadowDatabaseDisable;

    /**
     * 间隔时间,默认5分钟
     */
    @Inject
    @DefaultValue("300")
    @Named("/pradar/config/rt/shadowDatabaseProcess/delayTime")
    private Remote<Long> analysisShadowDatabaseInterval;

    private static final String ANALYSIS_RPC_TYPE = StringUtils.join(
        new String[] {String.valueOf(MiddlewareType.TYPE_DB), String.valueOf(MiddlewareType.TYPE_CACHE),
            String.valueOf(MiddlewareType.TYPE_SEARCH), String.valueOf(MiddlewareType.TYPE_FS)}, "','");

    // 查询 数据库、缓存、搜索、文件 等类型的 且 attachment不为空 的业务trace
    private static final String QUERY_SQL =
        "select appName,rpcType,parsedMethod,parsedMiddlewareName as middlewareName,flagMessage from t_trace_all where rpcType in ('"
            + ANALYSIS_RPC_TYPE + "') and startDate >= '%s' and appName = '%s'"
            + "and clusterTest = '0' and flagMessage is not null and flagMessage != ''";

    // 表名  插入列  插入数据
    private static final String INSERT_SQL_TEMPLATE = "INSERT IGNORE INTO %s %s VALUES %s ";

    private String insertShadowDatabaseSql = "";

    private String insertShadowBizTableSql = "";

    private final AbstractAppCache appCache = new AbstractAppCache();

    @Inject
    private ClickHouseSupport clickHouseSupport;

    @Inject
    private MysqlSupport mysqlSupport;

    @Inject
    private TaskManager<String, String> taskManager;

    @Override
    public void share(List<String> taskIds, String currentTaskId) {
        if (executeDisabled()) { return; }
        List<String> appNameCache = appCache.getAppNameCache();
        if (CollectionUtils.isEmpty(appNameCache)) {
            return;
        }
        Map<String, List<String>> avgMap = taskManager.allotOfAverage(taskIds, appNameCache);
        List<String> avgList = avgMap.get(currentTaskId);
        if (CollectionUtils.isNotEmpty(avgList)) {
            for (int i = 0, size = avgList.size(); i < size; i++) {
                analysisAndSave(appNameCache.get(i));
            }
        }
    }

    @Override
    public void share(int taskId) {
        if (executeDisabled() || taskId == -1) { return; }
        List<String> appNameCache = appCache.getAppNameCache();
        if (CollectionUtils.isEmpty(appNameCache)) {
            return;
        }
        for (int i = 0, size = appNameCache.size(); i < size; i++) {
            if (i % taskId == 0) {
                analysisAndSave(appNameCache.get(i));
            }
        }
    }

    @Override
    public void share() {
        if (executeDisabled()) { return; }
        List<String> appNameCache = appCache.getAppNameCache();
        if (CollectionUtils.isEmpty(appNameCache)) {
            return;
        }
        for (String appName : appNameCache) {
            analysisAndSave(appName);
        }
    }

    // 查询5分钟内的trace日志并进行解析
    public void analysisAndSave(String appName) {
        if (logger.isDebugEnabled()) {
            logger.debug("ShadowDatabaseProcessor execute： {}", appName);
        }
        try {
            List<TTrackClickhouseModel> models = queryTraceLog(appName, TraceLogQueryScopeEnum.build(5));
            analysisAndSave(models);
        } catch (Exception e) {
            logger.error("Save to {}/{} error! appName：[{}]", SHADOW_DATABASE, BIZ_TABLE, appName, e);
        }
    }

    private void analysisAndSave(List<TTrackClickhouseModel> traceModels) {
        if (CollectionUtils.isEmpty(traceModels)) {
            return;
        }
        List<ShadowDatabaseModel> databaseModelList = new ArrayList<>();
        List<ShadowBizTableModel> bizTableModelList = new ArrayList<>();
        for (TTrackClickhouseModel traceModel : traceModels) {
            ShadowDatabaseParseResult parseResult = TemplateParseHandler.analysisTraceModel(traceModel);
            if (parseResult != null) {
                ShadowDatabaseModel databaseModel = parseResult.getDatabaseModel();
                List<ShadowBizTableModel> bizTableModel = parseResult.getTableModelList();
                if (databaseModel != null) {
                    databaseModelList.add(databaseModel);
                }
                if (CollectionUtils.isNotEmpty(bizTableModel)) {
                    bizTableModelList.addAll(bizTableModel);
                }
            }
        }
        // 因为 data_source 长度问题，所以通过增加 uniqueKey 字段为唯一键，对应值为md5(真实唯一索引)
        uniqueSaveDatabase(databaseModelList);
        uniqueSaveBizTable(bizTableModelList);
    }

    private void uniqueSaveDatabase(List<ShadowDatabaseModel> databaseModelList) {
        if (CollectionUtils.isNotEmpty(databaseModelList)) {
            Set<String> uniqueKeySet = new HashSet<>();
            databaseModelList = databaseModelList.stream().filter(model -> {
                String uniqueKey = model.generateUniqueIndex();
                boolean isUnique = uniqueKeySet.add(uniqueKey);
                if (isUnique) {
                    model.setUniqueKey(Md5Utils.md5(uniqueKey));
                }
                return isUnique;
            }).collect(Collectors.toList());
            mysqlSupport.batchUpdate(insertShadowDatabaseSql,
                databaseModelList.stream().map(ShadowDatabaseModel::getValues).collect(Collectors.toList()));
            if (logger.isDebugEnabled()) {
                logger.debug("ShadowDatabaseProcessor save is ok. databaseSize：[{}]", databaseModelList.size());
            }
        }
    }

    private void uniqueSaveBizTable(List<ShadowBizTableModel> bizTableModelList) {
        if (CollectionUtils.isNotEmpty(bizTableModelList)) {
            Set<String> uniqueKeySet = new HashSet<>();
            bizTableModelList = bizTableModelList.stream().filter(model -> {
                String uniqueKey = model.generateUniqueIndex();
                boolean isUnique = uniqueKeySet.add(uniqueKey);
                if (isUnique) {
                    model.setUniqueKey(Md5Utils.md5(uniqueKey));
                }
                return isUnique;
            }).collect(Collectors.toList());
            mysqlSupport.batchUpdate(insertShadowBizTableSql,
                bizTableModelList.stream().map(ShadowBizTableModel::getValues).collect(Collectors.toList()));
            if (logger.isDebugEnabled()) {
                logger.debug("ShadowDatabaseProcessor save is ok. bizTableSize：[{}]", bizTableModelList.size());
            }
        }
    }

    // 查询对应应用的trace日志
    private List<TTrackClickhouseModel> queryTraceLog(String appName, TraceLogQueryScopeEnum timeScope) {
        Calendar startDate = Calendar.getInstance();
        switch (timeScope) {
            case WEEK:
                startDate.add(Calendar.DATE, (int)(-1 * timeScope.getTime()));
                break;
            case DAY:
                startDate.add(Calendar.DATE, (int)(-1 * timeScope.getTime()));
                break;
            case MINUTE:
            case MIN_CUS:
                startDate.add(Calendar.MINUTE, (int)(-1 * timeScope.getTime()));
                break;
            default:
        }
        String querySql = String.format(QUERY_SQL,
            DateFormatUtils.format(startDate.getTime(), "yyyy-MM-dd HH:mm:ss"), appName);
        return this.isUseCk() ? clickHouseSupport.queryForList(querySql, TTrackClickhouseModel.class)
            : mysqlSupport.queryForList(querySql, TTrackClickhouseModel.class);
    }

    private boolean executeDisabled() {
        return !analysisShadowDatabaseDisable.get() || !isHandler(analysisShadowDatabaseInterval.get());
    }

    @Override
    public void init() {
    }

    public void init(String dataSourceType) {
        this.setDataSourceType(dataSourceType);
        appCache.autoRefresh(mysqlSupport);
        insertShadowDatabaseSql = String.format(INSERT_SQL_TEMPLATE, SHADOW_DATABASE, ShadowDatabaseModel.getCols(),
            ShadowDatabaseModel.getParamCols());
        insertShadowBizTableSql = String.format(INSERT_SQL_TEMPLATE, BIZ_TABLE, ShadowBizTableModel.getCols(),
            ShadowBizTableModel.getParamCols());
    }

    public static class AbstractAppCache {

        private static final Logger logger = LoggerFactory.getLogger(AbstractAppCache.class);

        private List<String> appNameList = Lists.newLinkedList();

        public void autoRefresh(MysqlSupport mysqlSupport) {
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(() -> refresh(mysqlSupport), 0, 2, TimeUnit.MINUTES);
        }

        private void refresh(MysqlSupport mysqlSupport) {
            try {
                appNameList = mysqlSupport.queryForList("select app_name from t_amdb_app order by id", String.class);
            } catch (Exception e) {
                logger.error("Query app_name failed.", e);
            }
        }

        public List<String> getAppNameCache() {
            return appNameList;
        }
    }
}
