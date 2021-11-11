package io.shulie.surge.data.deploy.pradar.link.processor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.shulie.surge.config.clickhouse.ClickhouseTemplateHolder;
import io.shulie.surge.config.clickhouse.ClickhouseTemplateManager;
import io.shulie.surge.config.common.model.TenantAppEntity;
import io.shulie.surge.data.common.utils.Pair;
import io.shulie.surge.data.deploy.pradar.link.TaskManager;
import io.shulie.surge.data.deploy.pradar.link.model.ShadowBizTableModel;
import io.shulie.surge.data.deploy.pradar.link.model.ShadowDatabaseModel;
import io.shulie.surge.data.deploy.pradar.link.model.TTrackClickhouseModel;
import io.shulie.surge.data.deploy.pradar.link.parse.TemplateParseHandler;
import io.shulie.surge.data.deploy.pradar.parser.MiddlewareType;
import io.shulie.surge.data.deploy.pradar.parser.utils.Md5Utils;
import io.shulie.surge.data.runtime.common.DataOperations;
import io.shulie.surge.data.runtime.common.remote.DefaultValue;
import io.shulie.surge.data.runtime.common.remote.Remote;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.util.CollectionUtils;

import static io.shulie.surge.config.clickhouse.ClickhouseTemplateManager.*;

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
        "select appName,rpcType,parsedMethod,parsedMiddlewareName as middlewareName,flagMessage,userAppKey,envCode from %s where rpcType in ('"
            + ANALYSIS_RPC_TYPE + "') and startDate >= '%s' and startDate <= '%s' and appName = '%s' and userAppKey = '%s' and envCode = '%s' "
            + "and clusterTest = '0' and flagMessage is not null and flagMessage != ''";

    // 表名  插入列  插入数据
    private static final String INSERT_SQL_TEMPLATE = "INSERT IGNORE INTO %s %s VALUES %s ";

    private String insertShadowDatabaseSql = "";

    private String insertShadowBizTableSql = "";

    @Inject
    private ClickhouseTemplateManager clickhouseTemplateManager;

    @Inject
    private MysqlSupport mysqlSupport;

    @Inject
    private TaskManager<String, TenantAppEntity> taskManager;

    @Override
    public void share(List<String> taskIds, String currentTaskId) {
        if (executeDisabled()) { return; }
        Pair<String, String> timePair = getStartAndEndTime();
        List<TenantAppEntity> activeAppNames = queryActiveAppNames(timePair);
        if (!CollectionUtils.isEmpty(activeAppNames)) {
            Map<String, List<TenantAppEntity>> avgMap = taskManager.allotOfAverage(taskIds, activeAppNames);
            List<TenantAppEntity> avgList = avgMap.get(currentTaskId);
            if (!CollectionUtils.isEmpty(avgList)) {
                avgList.forEach(appEntity -> analysisAndSave(appEntity, timePair));
            }
        }
    }

    @Override
    public void share(int taskId) {
        if (executeDisabled() || taskId == -1) { return; }
        Pair<String, String> timePair = getStartAndEndTime();
        List<TenantAppEntity> activeAppNames = queryActiveAppNames(timePair);
        if (!CollectionUtils.isEmpty(activeAppNames)) {
            activeAppNames.forEach(appEntity -> {
                if (appEntity.getAppName().hashCode() % taskId == 0) {
                    analysisAndSave(appEntity, timePair);
                }
            });
        }
    }

    @Override
    public void share() {
        if (executeDisabled()) { return; }
        Pair<String, String> timePair = getStartAndEndTime();
        List<TenantAppEntity> activeAppNames = queryActiveAppNames(timePair);
        if (!CollectionUtils.isEmpty(activeAppNames)) {
            activeAppNames.forEach(appEntity -> analysisAndSave(appEntity, timePair));
        }
    }

    // 查询5分钟内的trace日志并进行解析
    public void analysisAndSave(TenantAppEntity appEntity, Pair<String, String> timePair) {
        String uniqueKey = appEntity.getAppName() + DELIMITER + appEntity.getEnvCode() + DELIMITER + appEntity.getUserAppKey();
        if (logger.isDebugEnabled()) {
            logger.debug("ShadowDatabaseProcessor execute： [{}]", uniqueKey);
        }
        try {
            List<TTrackClickhouseModel> models = queryTraceLog(appEntity, timePair);
            analysisAndSave(models);
        } catch (Exception e) {
            logger.error("Save to {}/{} error! uniqueKey：[{}]", SHADOW_DATABASE, BIZ_TABLE, uniqueKey, e);
        }
    }

    private void analysisAndSave(List<TTrackClickhouseModel> traceModels) {
        if (CollectionUtils.isEmpty(traceModels)) {
            return;
        }
        List<ShadowDatabaseModel> databaseModelList = new ArrayList<>();
        List<ShadowBizTableModel> bizTableModelList = new ArrayList<>();
        for (TTrackClickhouseModel traceModel : traceModels) {
            Pair<ShadowDatabaseModel, ShadowBizTableModel> modelPair = TemplateParseHandler.analysisTraceModel(traceModel);
            if (modelPair != null) {
                String userAppKey = traceModel.getUserAppKey();
                String envCode = traceModel.getEnvCode();
                ShadowDatabaseModel databaseModel = modelPair.getFirst();
                ShadowBizTableModel bizTableModel = modelPair.getSecond();
                if (databaseModel != null) {
                    databaseModel.setUserAppKey(userAppKey);
                    databaseModel.setEnvCode(envCode);
                    databaseModelList.add(databaseModel);
                }
                if (bizTableModel != null) {
                    bizTableModel.setUserAppKey(userAppKey);
                    bizTableModel.setEnvCode(envCode);
                    bizTableModelList.add(bizTableModel);
                }
            }
        }
        // 因为 data_source 长度问题，所以通过增加 uniqueKey 字段为唯一键，对应值为md5(真实唯一索引)
        uniqueSaveDatabase(databaseModelList);
        uniqueSaveBizTable(bizTableModelList);
    }

    private void uniqueSaveDatabase(List<ShadowDatabaseModel> databaseModelList) {
        if (!CollectionUtils.isEmpty(databaseModelList)) {
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
        if (!CollectionUtils.isEmpty(bizTableModelList)) {
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
    private List<TTrackClickhouseModel> queryTraceLog(TenantAppEntity appEntity, Pair<String, String> timePair) {
        ClickhouseTemplateHolder templateHolder = clickhouseTemplateManager.getTemplateHolder(appEntity.getUserAppKey(), appEntity.getEnvCode(), false);
        String querySql = buildSql(templateHolder.getTableName(), appEntity, timePair);
        return templateHolder.getTemplate().queryForList(querySql, TTrackClickhouseModel.class);
    }

    private String buildSql(String tableName, TenantAppEntity appEntity, Pair<String, String> timePair) {
        return String.format(QUERY_SQL, tableName, timePair.getFirst(), timePair.getSecond(), appEntity.getAppName(), appEntity.getUserAppKey(), appEntity.getEnvCode());
    }

    private boolean executeDisabled() {
        return !analysisShadowDatabaseDisable.get() || !isHandler(analysisShadowDatabaseInterval.get());
    }

    @Override
    public void init() {
    }

    public void init(String dataSourceType) {
        this.setDataSourceType(dataSourceType);
        insertShadowDatabaseSql = String.format(INSERT_SQL_TEMPLATE, SHADOW_DATABASE, ShadowDatabaseModel.getCols(),
            ShadowDatabaseModel.getParamCols());
        insertShadowBizTableSql = String.format(INSERT_SQL_TEMPLATE, BIZ_TABLE, ShadowBizTableModel.getCols(),
            ShadowBizTableModel.getParamCols());
    }

    private List<TenantAppEntity> queryActiveAppNames(Pair<String, String> timePair) {
        String sqlTemplate = "select appName,userAppKey,envCode from %s where startDate between '" + timePair.getFirst() + "' and '" + timePair.getSecond() + "' and clusterTest = '0' and flagMessage is not null and flagMessage != '' group by appName,userAppKey,envCode";
        Map<String, ClickhouseTemplateHolder> templateMap = clickhouseTemplateManager.getQueryTemplateMap();
        if (templateMap.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<TenantAppEntity> result = new ArrayList<>();
        templateMap.forEach((key, value) -> {
            DataOperations template = value.getTemplate();
            String tableName = value.getTableName();
            try {
                List<TenantAppEntity> appNames = template.query(String.format(sqlTemplate, tableName), new BeanPropertyRowMapper<>(TenantAppEntity.class));
                if (!CollectionUtils.isEmpty(appNames)) {
                    result.addAll(appNames);
                }
            } catch (Exception e) {
                logger.error("shadowDatabaseProcessor query appNames error, key=[{}]", key, e);
            }
        });
        return result;
    }

    private final static int DEFAULT_DELAY_TIME = 2;

    private Pair<String, String> getStartAndEndTime() {
        long now = System.currentTimeMillis();
        String startTime = DateFormatUtils.format(now - DEFAULT_DELAY_TIME * 60 * 1000, "yyyy-MM-dd HH:mm:ss");
        String endTime = DateFormatUtils.format(now - 5000, "yyyy-MM-dd HH:mm:ss");
        return new Pair<>(startTime, endTime);
    }
}
