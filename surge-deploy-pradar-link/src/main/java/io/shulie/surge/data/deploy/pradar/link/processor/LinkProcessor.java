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

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.deploy.pradar.link.AbstractLinkCache;
import io.shulie.surge.data.deploy.pradar.link.TaskManager;
import io.shulie.surge.data.deploy.pradar.link.enums.TraceLogQueryScopeEnum;
import io.shulie.surge.data.deploy.pradar.link.model.LinkEdgeModel;
import io.shulie.surge.data.deploy.pradar.link.model.LinkNodeModel;
import io.shulie.surge.data.deploy.pradar.link.model.TTrackClickhouseModel;
import io.shulie.surge.data.deploy.pradar.link.util.StringUtil;
import io.shulie.surge.data.deploy.pradar.parser.MiddlewareType;
import io.shulie.surge.data.deploy.pradar.parser.PradarLogType;
import io.shulie.surge.data.deploy.pradar.parser.RpcBasedParser;
import io.shulie.surge.data.deploy.pradar.parser.RpcBasedParserFactory;
import io.shulie.surge.data.deploy.pradar.parser.utils.Md5Utils;
import io.shulie.surge.data.runtime.common.remote.DefaultValue;
import io.shulie.surge.data.runtime.common.remote.Remote;
import io.shulie.surge.data.sink.clickhouse.ClickHouseSupport;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * 链路解析器
 *
 * @author vincent
 */
public class LinkProcessor extends AbstractProcessor {
    private static final Logger logger = LoggerFactory.getLogger(LinkProcessor.class);

    private static final String LINKNODE_TABLENAME = "t_amdb_pradar_link_node";
    private static final String LINKEDGE_TABLENAME = "t_amdb_pradar_link_edge";

    private static final String NEW_LINE_MATCHER = "\r\n";

    private static final String LINK_TOPOLOGY_SQL
            = " appName,entranceId,entranceNodeId,traceId,rpcId,logType,rpcType,upAppName,middlewareName,serviceName,parsedServiceName,parsedMiddlewareName,parsedExtend,parsedMethod,methodName,port,remoteIp,userAppKey,envCode,userId ";

    @Inject
    private ClickHouseSupport clickHouseSupport;

    @Inject
    private MysqlSupport mysqlSupport;

    @Inject
    private TaskManager<String> taskManager;

    /**
     * 是否开启链路梳理功能
     */
    @Inject
    @DefaultValue("true")
    @Named("/pradar/config/rt/linkProcessDisable")
    private Remote<Boolean> linkProcessDisable;

    /**
     * 间隔时间,默认2分钟
     */
    @Inject
    @DefaultValue("120")
    @Named("/pradar/config/rt/linkProcess/delayTime")
    private Remote<Long> intervalTime;

    @Inject
    @DefaultValue("false")
    @Named("/pradar/config/rt/linkProcess/isFilterTakinFlag")
    private Remote<Boolean> isFilterTakinFlag;

    @Inject
    @DefaultValue("takin-web,takin-cloud,takin-amdb")
    @Named("/pradar/config/rt/linkProcess/filterTakinConfig")
    private Remote<String> filterTakinConfig;

    @Inject
    @Named("config.link.trace.query.limit")
    private String traceQuerylimit;

    public AbstractLinkCache linkCache = new AbstractLinkCache() {
        @Override
        public void save(String linkId, LinkedBlockingQueue<String> linkedBlockingQueue) {
            return;
        }
    };

    String linkNodeInsertSql = "";
    String linkEdgeInsertSql = "";

    /**
     * 解析任务按照任务编号去分配
     *
     * @param currentTaskId
     * @throws IOException
     */
    @Override
    public void share() {
        if (linkProcessDisable.get()) {
            return;
        }
        if (!isHandler(intervalTime.get(), 60)) {
            return;
        }
        Map<String, Map<String, Object>> linkConfig = linkCache.getLinkConfig();
        if (linkConfig == null || linkConfig.isEmpty()) {
            return;
        }
        Set<String> linkIdSet = linkConfig.keySet();
        List<String> avgList = taskManager.allotOfAverage(new ArrayList<>(linkIdSet));
        if (CollectionUtils.isNotEmpty(avgList)) {
            for (int i = 0; i < avgList.size(); i++) {
                String linkId = avgList.get(i);
                Map<String, Object> link = linkConfig.get(avgList.get(i));
                saveLink(linkId, link);
            }
        }
    }

    /**
     * 解析任务按照任务编号去分配
     *
     * @param taskId
     * @throws IOException
     */
    @Override
    public void share(int taskId) {
        if (linkProcessDisable.get()) {
            return;
        }
        if (!isHandler(intervalTime.get(), 60)) {
            return;
        }
        if (taskId == -1) {
            return;
        }

        Map<String, Map<String, Object>> linkConfig = linkCache.getLinkConfig();
        List<Map.Entry<String, Map<String, Object>>> linkList = Lists.newArrayList(linkConfig.entrySet());
        for (int i = 0; i < linkList.size(); i++) {
            if (i % taskId == 0) {
                Map.Entry<String, Map<String, Object>> link = linkList.get(i);
                saveLink(link.getKey(), link.getValue());
            }
        }
    }
//
//    /**
//     * 解析任务不按照任务编号去分配
//     *
//     * @throws IOException
//     */
//    @Override
//    public void share() {
//        if (linkProcessDisable.get()) {
//            return;
//        }
//        if (!isHandler(intervalTime.get())) {
//            return;
//        }
//        Map<String, Map<String, Object>> linkConfig = linkCache.getLinkConfig();
//        for (Map.Entry<String, Map<String, Object>> entry : linkConfig.entrySet()) {
//            saveLink(entry.getKey(), entry.getValue());
//        }
//    }

    @Override
    public void init() {
        linkNodeInsertSql = "INSERT INTO " + LINKNODE_TABLENAME + LinkNodeModel.getCols() + " VALUES " + LinkNodeModel
                .getParamCols() + LinkNodeModel.getOnDuplicateCols();
        linkEdgeInsertSql = "INSERT INTO " + LINKEDGE_TABLENAME + LinkEdgeModel.getCols() + " VALUES " + LinkEdgeModel
                .getParamCols() + LinkEdgeModel.getOnDuplicateCols();
    }

    /**
     * 保存链路信息
     */
    public void saveLink(String linkId, Map<String, Object> linkConfig) {
        logger.info("LinkProcessor {},{}", linkId, linkConfig);
        try {
            //写入MySQL
            Pair<Set<LinkNodeModel>, Set<LinkEdgeModel>> linkPair = link(linkId, linkConfig,
                    TraceLogQueryScopeEnum.MINUTE);
            saveTopology(linkId, linkConfig, linkPair);
            logger.info("LinkProcessor save is ok size {},{}", linkPair.getKey().size(), linkPair.getValue().size());
        } catch (Exception e) {
            logger.error("Save to pradar_link_info error!" + linkId, e);
            //ignore
        }
    }

    public void saveTopology(String linkId, Map<String, Object> linkConfig, Pair<Set<LinkNodeModel>, Set<LinkEdgeModel>> linkPair) {
        Set<LinkNodeModel> linkNodeModels = linkPair.getLeft();
        Set<LinkEdgeModel> linkEdgeModels = linkPair.getRight();
        if (CollectionUtils.isEmpty(linkNodeModels) || CollectionUtils.isEmpty(linkEdgeModels)) {
            logger.warn("LinkProcessor is empty  {}, {}", linkId, linkConfig);
            return;
        }
        mysqlSupport.addBatch(linkNodeInsertSql,
                linkNodeModels.stream().map(LinkNodeModel::getValues).collect(Collectors.toList()));
        mysqlSupport.addBatch(linkEdgeInsertSql,
                linkEdgeModels.stream().map(LinkEdgeModel::getValues).collect(Collectors.toList()));
    }

    /**
     * 获取linkId对应的链路关系
     *
     * @param linkId
     * @return
     * @throws IOException
     */
    public Pair<Set<LinkNodeModel>, Set<LinkEdgeModel>> link(String linkId, Map<String, Object> linkConfig,
                                                             TraceLogQueryScopeEnum queryScope) throws IOException {
        Set<LinkEdgeModel> edges = new HashSet<>();
        Set<LinkNodeModel> nodes = new HashSet<>();
        Pair<List<RpcBased>, Map<String, String>> pair = getTraceLog(linkConfig, queryScope);
        List<RpcBased> rpcBaseds = pair.getLeft();
        if (CollectionUtils.isNotEmpty(rpcBaseds)) {
            Pair<Set<LinkNodeModel>, Set<LinkEdgeModel>> linkRelationPair = linkAnalysis(linkId, linkConfig, rpcBaseds, pair.getRight());
            nodes.addAll(linkRelationPair.getLeft());
            edges.addAll(linkRelationPair.getRight());
        }
        return Pair.of(nodes, edges);
    }

    /**
     * 提供AMDB根据指定traceId计算链路拓扑的功能
     *
     * @param param
     * @return
     * @throws IOException
     */
    public Pair<Set<LinkNodeModel>, Set<LinkEdgeModel>> link(Map<String, String> param) throws IOException {
        Set<LinkEdgeModel> edges = new HashSet<>();
        Set<LinkNodeModel> nodes = new HashSet<>();
        List<RpcBased> rpcBaseds = getTraceLog(param);
        Map<String, Object> linkConfig = new HashMap<>(param);
        if (CollectionUtils.isNotEmpty(rpcBaseds)) {
            Map<String, String> traceFilter = new HashMap<>();
            traceFilter.put(param.get("traceId"), param.get("rpcId") + "#" + param.get("logType"));
            Pair<Set<LinkNodeModel>, Set<LinkEdgeModel>> linkRelationPair = linkAnalysis(param.get("linkId"), linkConfig, rpcBaseds, traceFilter);
            nodes.addAll(linkRelationPair.getLeft());
            edges.addAll(linkRelationPair.getRight());
        }
        return Pair.of(nodes, edges);
    }

    public Pair<List<RpcBased>, Map<String, String>> getTraceLog(Map<String, Object> linkConfig, TraceLogQueryScopeEnum queryScope) {
        Map<String, String> traceFilter = new HashMap<>();
        //是否TAKIN相关应用的业务活动
        Boolean isTakinConcerndFlag = false;
        String method = String.valueOf(linkConfig.get("method"));
        String appName = String.valueOf(linkConfig.get("appName"));
        String rpcType = String.valueOf(linkConfig.get("rpcType"));
        String service = String.valueOf(linkConfig.get("service"));
        if (StringUtils.isBlank(rpcType) || "null".equals(rpcType)) {
            return Pair.of(Collections.EMPTY_LIST, traceFilter);
        }
        String userAppKey = String.valueOf(linkConfig.get("userAppKey"));
        String envCode = String.valueOf(linkConfig.get("envCode"));

        //如果业务活动的应用名称含有TAKIN关键字,说明是TAKIN相关的业务活动,此时不用在链路图中过滤TAKIN相关的边
        List<String> filterTakinAppList = null;
        if (isFilterTakinFlag != null && isFilterTakinFlag.get()) {
            String[] filterTakinApps = filterTakinConfig.get().split(",");
            if (filterTakinApps.length != 0) {
                filterTakinAppList = Arrays.asList(filterTakinApps);
                if (filterTakinAppList.contains(appName.toLowerCase())) {
                    isTakinConcerndFlag = true;
                }
            } else {
                filterTakinAppList = Lists.newArrayList();
            }
        }

        // k=traceId v=rpcId  用来截取从当前rpcId开始的下游节点
        String simpleSql = "select traceId,rpcId,logType from t_trace_all where appName='" + appName +
                "' and parsedMethod = '" + method +
                "' and rpcType = '" + rpcType +
                "' and parsedServiceName = '" + service +
                "' and userAppKey = '" + userAppKey +
                "' and envCode = '" + envCode + "'";
        Calendar beginCalendar = Calendar.getInstance();
        switch (queryScope) {
            case WEEK:
                beginCalendar.add(Calendar.DATE, (int) (-1 * queryScope.getTime()));
                break;
            case DAY:
                beginCalendar.add(Calendar.DATE, (int) (-1 * queryScope.getTime()));
                break;
            case MINUTE:
            case MIN_CUS:
                beginCalendar.add(Calendar.MINUTE, (int) (-1 * queryScope.getTime()));
                break;
            default:
                //do nothing
        }
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.add(Calendar.MINUTE, -1);
        simpleSql += " and startDate>='" + DateFormatUtils.format(beginCalendar.getTime(), "yyyy-MM-dd HH:mm:ss")
                + "' and startDate <='" + DateFormatUtils.format(endCalendar, "yyyy-MM-dd HH:mm:ss")
                + "' order by startDate desc limit 2";

        List<Map<String, Object>> traceMaps = Lists.newArrayList();
        if (this.isUseCk()) {
            traceMaps = clickHouseSupport.queryForList(simpleSql);
        } else {
            traceMaps = mysqlSupport.queryForList(simpleSql);
        }

        StringBuilder sql = new StringBuilder();
        for (Map<String, Object> traceIdMap : traceMaps) {
            if (traceIdMap.containsKey("logType") && "5".equals(traceIdMap.get("logType"))) {
                continue;
            }
            String traceId = Objects.toString(traceIdMap.get("traceId"));
            String rpcId = Objects.toString(traceIdMap.get("rpcId"));
            int logType = NumberUtils.toInt(Objects.toString(traceIdMap.get("logType")));

            //切换为mysql数据源时,当union和limit共同使用时,需要用括号包裹
            //clickhouse执行该用括号包括sql,将会抛出空指针异常
            if (!this.isUseCk()) {
                sql.append("(");
            }
            sql.append("select " + LINK_TOPOLOGY_SQL + " from t_trace_all where traceId ='" + traceId + "'");
            sql.append(" and startDate>='" + DateFormatUtils.format(beginCalendar.getTime(), "yyyy-MM-dd HH:mm:ss") + "'");
            sql.append(" order by rpcId asc limit " + ("".equals(traceQuerylimit) ? "500" : traceQuerylimit));
            if (!this.isUseCk()) {
                sql.append(")");
            }
            sql.append(" union all ");
            traceFilter.put(traceId, rpcId + "#" + logType);
        }

        if (sql.length() <= 0) {
            return Pair.of(Collections.EMPTY_LIST, traceFilter);
        }
        //add trace log limit
        sql.delete(sql.length() - 11, sql.length());
        logger.info("queryLinkTopology:{},{}", sql, traceFilter);

        List<TTrackClickhouseModel> modelList = Lists.newArrayList();
        if (this.isUseCk()) {
            modelList = clickHouseSupport.queryForList(sql.toString(), TTrackClickhouseModel.class);
        } else {
            modelList = mysqlSupport.query(sql.toString(), new BeanPropertyRowMapper(TTrackClickhouseModel.class));
        }

        TTrackClickhouseModel tmpModel = null;
        //首先把跟入口匹配上的数据暂存一份,用于后面为空时的处理
        for (TTrackClickhouseModel model : modelList) {
            String ary[] = traceFilter.get(model.getTraceId()).split("#");
            String filterRpcId = ary[0];
            String filterLogType = ary[1];
            if (model.getRpcId().equals(filterRpcId) && appName.equals(model.getAppName()) && service.equals(model.getParsedServiceName()) && method.equals(model.getMethodName()) && filterLogType.equals(model.getLogType() + "") && userAppKey.equals(model.getUserAppKey()) && envCode.equals(model.getEnvCode())) {
                // 相同RpcID情况处理，如果是选择的当前服务且当前服务是入口，就保留，否则就丢掉
                tmpModel = model;
                break;
            }
        }

        Boolean finalIsTakinConcerndFlag = isTakinConcerndFlag;
        List<String> finalFilterTakinAppList = filterTakinAppList;
        modelList = modelList.stream().filter(model -> {
            String ary[] = traceFilter.get(model.getTraceId()).split("#");
            String filterRpcId = ary[0];
            String filterLogType = ary[1];

            //如果过滤开关打开,并且是非TAKIN相关业务活动,过滤调用链中TAKIN相关应用的边
            if (isFilterTakinFlag != null && isFilterTakinFlag.get() && !finalIsTakinConcerndFlag && (finalFilterTakinAppList.contains(model.getAppName().toLowerCase()) || finalFilterTakinAppList.contains(model.getUpAppName().toLowerCase()))) {
                return false;
            }

            // 针对MQ类型的,由于生产和消费的日志rpcId都一致,当设置消费者为入口时,需要把生产者的日志过滤掉
            if (model.getLogType() == 5 || (model.getRpcType() == 3 && model.getRpcId().equals(filterRpcId) && model.getLogType() == 2)) {
                return false;
            }

            // 相同RpcID情况处理，如果是选择的当前服务且当前服务是入口，就保留，否则就丢掉
            // 如果是以所选服务的RpcId为开始的就保留，否则就丢掉
            return (model.getRpcId().equals(filterRpcId) && appName.equals(model.getAppName()) && model.getParsedServiceName().contains(service) && method.equals(model.getMethodName()) && filterLogType.equals(model.getLogType() + "") && userAppKey.equals(model.getUserAppKey()) && envCode.equals(model.getEnvCode())) || (model.getRpcId().startsWith(filterRpcId) && model.getLogType() != 1);
        }).collect(Collectors.toList());

        //当选择的入口rpcId不为0时,且当前节点为最后一个节点,此时链路图不会展示,需要兼容这种情况
        if (modelList.isEmpty() && tmpModel != null) {
            modelList.add(tmpModel);
        }
        return Pair.of(modelList.stream().map(TTrackClickhouseModel::getRpcBased).collect(Collectors.toList()), traceFilter);
    }

    public static ThreadLocal<String> threadLocal = new ThreadLocal<String>();

    public List<RpcBased> getTraceLog(Map<String, String> param) {
        String serviceName = param.get("service");
        String methodName = param.get("method");
        String appName = param.get("appName");
        String traceId = param.get("traceId");
        String startTime = param.get("startTime");
        String endTime = param.get("endTime");
        String rpcId = param.get("rpcId");
        String logType = param.get("logType");

        if (StringUtils.isBlank(traceId)) {
            return Collections.EMPTY_LIST;
        }
        Map<String, String> traceFilter = new HashMap<>();
        traceFilter.put(traceId, rpcId + "#" + logType);

        StringBuilder sql = new StringBuilder();
        sql.append("select " + LINK_TOPOLOGY_SQL + " from t_trace_all where startDate between '" + startTime + "' and '" + endTime + "' and traceId = '" + traceId + "' and logType != 5");
        sql.append(" order by rpcId asc limit " + ("".equals(traceQuerylimit) ? "500" : traceQuerylimit));
        logger.info("LinkProcessor query traceIds:{},sql:{}", traceFilter, sql);

        List<TTrackClickhouseModel> modelList = Lists.newArrayList();
        if (this.isUseCk()) {
            modelList = clickHouseSupport.queryForList(sql.toString(), TTrackClickhouseModel.class);
        } else {
            modelList = mysqlSupport.query(sql.toString(), new BeanPropertyRowMapper(TTrackClickhouseModel.class));
        }

        modelList = modelList.stream().filter(model -> {
            String ary[] = traceFilter.get(model.getTraceId()).split("#");
            String filterRpcId = ary[0];
            String filterLogType = ary[1];

            // 针对MQ类型的,由于生产和消费的日志rpcId都一致,当设置消费者为入口时,需要把生产者的日志过滤掉
            if (model.getLogType() == 5 || (model.getRpcType() == 3 && model.getRpcId().equals(filterRpcId) && model.getLogType() == 2)) {
                return false;
            }

            // 如果为入口且是以所选服务的RpcId为开始的就保留，否则就丢掉
            return (model.getRpcId().equals(filterRpcId) && appName.equals(model.getAppName()) && model.getParsedServiceName().contains(serviceName) && methodName.equals(model.getMethodName()) && filterLogType.equals(model.getLogType() + "")) || (model.getRpcId().startsWith(filterRpcId) && model.getLogType() != 1);
        }).collect(Collectors.toList());

        return modelList.stream().map(TTrackClickhouseModel::getRpcBased).collect(Collectors.toList());
    }

    private static String objectToString(Object value, String defaultStr) {
        if (value == null || "null".equalsIgnoreCase(value.toString())) {
            return "";
        }
        return ObjectUtils.toString(value);
    }

    /**
     * 链路关系分析
     *
     * @param rpcBaseds
     * @param traceFilter
     */
    public Pair<Set<LinkNodeModel>, Set<LinkEdgeModel>> linkAnalysis(String linkId, Map<String, Object> linkConfig, List<RpcBased> rpcBaseds, Map<String, String> traceFilter) {
        Set<LinkEdgeModel> edges = new HashSet<>();
        Set<LinkNodeModel> nodes = new HashSet<>();

        String rpcType = StringUtil.formatString(linkConfig.get("rpcType"));
        String appName = StringUtil.formatString(linkConfig.get("appName"));
        String service = StringUtil.formatString(linkConfig.get("service"));
        String method = StringUtil.formatString(linkConfig.get("method"));
        String userAppKey = StringUtil.formatString(linkConfig.get("userAppKey"));
        String envCode = StringUtil.formatString(linkConfig.get("envCode"));
        String userId = StringUtil.formatString(linkConfig.get("userId"));

        Boolean isCalculateNonTraceLogUpAppNode = true;
        //如果当前业务活动只有一条边并且不为业务真实入口,不计算其上游节点
        if (rpcBaseds.size() == 1 && !"0".equals(rpcBaseds.get(0).getRpcId())) {
            StringBuffer tags = new StringBuffer();
            tags.append(objectToString(rpcBaseds.get(0).getParsedServiceName(), ""))
                    .append("|")
                    .append(objectToString(rpcBaseds.get(0).getMethodName(), ""))
                    .append("|")
                    .append(objectToString(rpcBaseds.get(0).getAppName(), ""))
                    .append("|")
                    .append(objectToString(rpcBaseds.get(0).getRpcType(), ""))
                    .append("|")
                    .append("");
            if (linkId.equals(Md5Utils.md5(tags.toString()))) {
                isCalculateNonTraceLogUpAppNode = false;
            }
        }

        for (RpcBased rpcBased : rpcBaseds) {
            //在链路拓扑图这里客户端的rpc日志我们不解
            if (rpcBased == null || (PradarLogType.LOG_TYPE_RPC_CLIENT == rpcBased.getLogType() && MiddlewareType.TYPE_RPC == rpcBased.getRpcType()) || (StringUtils.isNotBlank(rpcBased.getMiddlewareName()) && rpcBased.getMiddlewareName().contains("sentinel_terminal_message"))) {
                continue;
            }

            RpcBasedParser rpcBasedParser = RpcBasedParserFactory.getInstance(rpcBased.getLogType(), rpcBased.getRpcType());
            if (rpcBasedParser == null) {
                continue;
            }
            String edgeId = rpcBasedParser.edgeId("", rpcBased);
            Map<String, Object> edgeTags = rpcBasedParser.edgeTags("", rpcBased);
            String fromAppId = rpcBasedParser.fromAppId(linkId, rpcBased);
            String toAppId = rpcBasedParser.toAppId(linkId, rpcBased);
            Map<String, Object> fromAppTags = rpcBasedParser.fromAppTags(linkId, rpcBased);

            //如果业务活动非真实业务入口,而是中间节点,也生成virtual节点,这样就不会导致链路图上除了虚拟节点以外还多出一个上游的问题
            // 2022-03-04 排除自己调用自己又不是入口调用日志时候,此时会导致生成的点只有一个,且为入口虚拟节点
            if (rpcBased.getRpcType() == Integer.parseInt(rpcType) && rpcBased.getAppName().equals(appName) && rpcBased.getServiceName().equals(service) && rpcBased.getMethodName().equals(method) && !rpcBased.getAppName().equals(rpcBased.getUpAppName())) {
                String ary[] = traceFilter.get(rpcBased.getTraceId()).split("#");
                String filterRpcId = ary[0];
                String filterLogType = ary[1];
                //如果已经处理成虚拟入口了,则不再次处理
                if (!(StringUtil.formatString(fromAppTags.get("appName")).contains("-Virtual")) && Objects.toString(rpcBased.getLogType()).equals(filterLogType) && rpcBased.getRpcId().equals(filterRpcId)) {
                    fromAppTags.put("appName", fromAppTags.get("appName") + "-Virtual");
                }
                fromAppTags.put("middlewareName", "virtual");
            }

            Map<String, Object> toAppTags = rpcBasedParser.toAppTags(linkId, rpcBased);
            fromAppTags.put("appId", fromAppId);
            toAppTags.put("appId", toAppId);
            edgeTags.put("edgeId", edgeId);
            edgeTags.put("fromAppId", fromAppId);
            edgeTags.put("toAppId", toAppId);
            edgeTags.put("linkId", linkId);

            //赋值业务活动对应的租户和环境,防止跨环境调用产生的链路图NPE问题
            fromAppTags.put("userAppKey", userAppKey);
            fromAppTags.put("envCode", envCode);
            fromAppTags.put("userId", userId);
            toAppTags.put("userAppKey", userAppKey);
            toAppTags.put("envCode", envCode);
            toAppTags.put("userId", userId);
            edgeTags.put("userAppKey", userAppKey);
            edgeTags.put("envCode", envCode);
            edgeTags.put("userId", userId);
            //如果只有一条日志,并且是rpcId不为0的入口日志,则不生成上游应用节点,用虚拟节点指向,否则会出出现虚拟节点和对应上游节点同时存在的情况
            if (isCalculateNonTraceLogUpAppNode) {
                LinkNodeModel fromNodeModel = LinkNodeModel.parseFromDataMap(fromAppTags);
                if (StringUtils.isNotBlank((String) fromAppTags.get("middlewareName"))) {
                    Map<String, Object> fromNodeExtendInfo = new HashMap<>();
                    fromNodeExtendInfo.put("ip", rpcBased.getRemoteIp());
                    fromNodeExtendInfo.put("port", rpcBased.getPort());
                    fromNodeModel.setExtend(JSON.toJSONString(fromNodeExtendInfo));
                }
                nodes.add(fromNodeModel);
            }
            LinkNodeModel toNodeModel = LinkNodeModel.parseFromDataMap(toAppTags);
            if (StringUtils.isNotBlank((String) toAppTags.get("middlewareName"))) {
                Map<String, Object> toNodeExtendInfo = new HashMap<>();
                toNodeExtendInfo.put("ip", rpcBased.getRemoteIp());
                toNodeExtendInfo.put("port", rpcBased.getPort());
                toNodeModel.setExtend(JSON.toJSONString(toNodeExtendInfo));
            }
            nodes.add(toNodeModel);
            edges.add(LinkEdgeModel.parseFromDataMap(edgeTags));
        }
        return Pair.of(nodes, edges);
    }

    /**
     * 更新链路信息
     */
    public void init(String dataSourceType) {
        //设置数据源
        this.setDataSourceType(dataSourceType);
        linkCache.autoRefresh(mysqlSupport);
        linkNodeInsertSql = "INSERT INTO " + LINKNODE_TABLENAME + LinkNodeModel.getCols() + " VALUES " + LinkNodeModel
                .getParamCols() + LinkNodeModel.getOnDuplicateCols();
        linkEdgeInsertSql = "INSERT INTO " + LINKEDGE_TABLENAME + LinkEdgeModel.getCols() + " VALUES " + LinkEdgeModel
                .getParamCols() + LinkEdgeModel.getOnDuplicateCols();
    }

    public AbstractLinkCache getLinkCache() {
        return linkCache;
    }


    public static void main(String[] args) throws Exception {
        // create clickhouse JDBCTemplate
//        ClickHouseProperties clickHouseProperties = new ClickHouseProperties();
//        String clickhouseUserName = "default";
//        if (StringUtils.isNotBlank(clickhouseUserName)) {
//            clickHouseProperties.setUser(clickhouseUserName);
//        }
//
//        String clickhousePassword = "rU4zGjA/";
//        if (StringUtils.isNotBlank(clickhousePassword)) {
//            clickHouseProperties.setPassword(clickhousePassword);
//        }
//        String clickhouseUrl = "jdbc:clickhouse://pradar.host.clickhouse01:8123,pradar.host.clickhouse02:8123/default";
//        DataSource clickHouseDataSource = new BalancedClickhouseDataSource(clickhouseUrl, clickHouseProperties);
//        JdbcTemplate jdbcTemplate = new JdbcTemplate(clickHouseDataSource);
//
//        List<Map<String, Object>> resultList = jdbcTemplate.queryForList("select  appName,entranceId,entranceNodeId,traceId,rpcId,logType,rpcType,upAppName,middlewareName,serviceName,parsedServiceName,methodName,port,remoteIp,userAppKey,envCode,userId  from t_trace_all where traceId ='010011ac16463824055372022d58f4' and startDate>='2022-03-04 16:25:19' order by rpcId asc limit 500 union all select  appName,entranceId,entranceNodeId,traceId,rpcId,logType,rpcType,upAppName,middlewareName,serviceName,parsedServiceName,methodName,port,remoteIp,userAppKey,envCode,userId  from t_trace_all where traceId ='010011ac16463824055262021d58f4' and startDate>='2022-03-04 16:25:19' order by rpcId asc limit 500");
//        List<TTrackClickhouseModel> modelList = resultList.stream().map(result -> JSONObject.parseObject(JSON.toJSON(result).toString(), TTrackClickhouseModel.class)).collect(Collectors.toList());
//        Map<String, Object> linkConfig = new HashMap<>();
//        linkConfig.put("rpcType", "1");
//        linkConfig.put("appName", "Demo-dubbo-provider");
//        linkConfig.put("service", "com.example.dubboproviderdemo.service.UserService");
//        linkConfig.put("method", "hello()");
//        linkConfig.put("userAppKey", "c55cf771-12b5-49e4-a566-c84723a5f6f3");
//        linkConfig.put("envCode", "test");
//        linkConfig.put("userId", "-1");
//        Map<String, String> traceFilter = new HashMap<>();
//        traceFilter.put("010011ac16463824055262021d58f4", "9#3");
//        traceFilter.put("010011ac16463824055372022d58f4", "9#3");
//        List<RpcBased> rpcBaseds = modelList.stream().map(TTrackClickhouseModel::getRpcBased).collect(Collectors.toList());
//        Pair<Set<LinkNodeModel>, Set<LinkEdgeModel>> test = linkAnalysis("cd9628528a5c07d1b825102b8ea6be53", linkConfig, rpcBaseds, traceFilter);
//

    }
}
