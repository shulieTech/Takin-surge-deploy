package io.shulie.takin.kafka.receiver.task;

import cn.hutool.core.date.DateUnit;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.shulie.surge.data.sink.clickhouse.ClickHouseShardSupport;
import io.shulie.takin.kafka.receiver.constant.web.CollectorConstants;
import io.shulie.takin.kafka.receiver.constant.web.NodeTypeEnum;
import io.shulie.takin.kafka.receiver.constant.web.PressureSceneEnum;
import io.shulie.takin.kafka.receiver.constant.web.TimeUnitEnum;
import io.shulie.takin.kafka.receiver.dto.clickhouse.ClickhouseQueryRequest;
import io.shulie.takin.kafka.receiver.dto.web.PtConfigExt;
import io.shulie.takin.kafka.receiver.dto.web.RtDataOutput;
import io.shulie.takin.kafka.receiver.dto.web.ScriptNode;
import io.shulie.takin.kafka.receiver.entity.EngineMetrics;
import io.shulie.takin.kafka.receiver.entity.EnginePressure;
import io.shulie.takin.kafka.receiver.entity.Report;
import io.shulie.takin.kafka.receiver.entity.SceneManage;
import io.shulie.takin.kafka.receiver.service.ClickhouseQueryService;
import io.shulie.takin.kafka.receiver.service.IReportService;
import io.shulie.takin.kafka.receiver.service.ISceneManageService;
import io.shulie.takin.kafka.receiver.util.CollectorUtil;
import io.shulie.takin.kafka.receiver.util.DataUtils;
import io.shulie.takin.kafka.receiver.util.JmxUtil;
import io.shulie.takin.kafka.receiver.util.NumberUtil;
import io.shulie.takin.sdk.kafka.HttpSender;
import io.shulie.takin.sdk.kafka.MessageSendCallBack;
import io.shulie.takin.sdk.kafka.MessageSendService;
import io.shulie.takin.sdk.kafka.impl.KafkaSendServiceFactory;
import io.shulie.takin.utils.json.JsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MetricsDataDealTask {

    @Resource
    private IReportService iReportService;
    @Resource
    private ISceneManageService iSceneManageService;
    @Value("${report.metric.isSaveLastPoint:true}")
    private boolean isSaveLastPoint;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ClickhouseQueryService clickhouseQueryService;
    @Resource
    private ClickHouseShardSupport clickHouseShardSupport;

    private final MessageSendService kafkaMessageInstance = new KafkaSendServiceFactory().getKafkaMessageInstance();
    private static final String topicUrl = "engine/pressure/data";

    /**
     * 每五秒执行一次
     */
    @Async("collectorSchedulerPool")
    @Scheduled(cron = "0/5 * * * * ? ")
    public void pushData() {
        List<Report> all = new ArrayList<>();
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Report::getStatus, 0);
        queryWrapper.lambda().eq(Report::getIsDeleted, 0);
        queryWrapper.lambda().ne(Report::getPressureType, PressureSceneEnum.INSPECTION_MODE.getCode());
        queryWrapper.lambda().isNotNull(Report::getJobId);
        List<Report> results = iReportService.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(results)) {
            all.addAll(results);
        }
        List<Long> reportIds = all.stream().map(Report::getId).collect(Collectors.toList());

        //获取结束时间在30s之内的报告
        QueryWrapper<Report> reportQueryWrapper = new QueryWrapper<>();
        reportQueryWrapper.lambda().eq(Report::getIsDeleted, 0);
        reportQueryWrapper.lambda().ne(Report::getPressureType, PressureSceneEnum.INSPECTION_MODE.getCode());
        reportQueryWrapper.lambda().isNotNull(Report::getJobId);
        reportQueryWrapper.lambda().isNotNull(Report::getEndTime);
        reportQueryWrapper.lambda().ge(Report::getEndTime, LocalDateTime.now().plusSeconds(-30));
        List<Report> list = iReportService.list(reportQueryWrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            List<Report> reportList = list.stream().filter(o -> !reportIds.contains(o.getId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(reportList)) {
                all.addAll(list);
            }
        }
        if (CollectionUtils.isEmpty(all)) {
            log.debug("没有需要统计的报告！");
            return;
        }
        List<Long> allReportIds = all.stream().map(Report::getId).collect(Collectors.toList());
        log.info("找到需要统计的报告：" + JsonHelper.bean2Json(allReportIds));
        all.stream().filter(Objects::nonNull).forEach(r -> combineMetricsData(r, false, null));
    }


    private Long getMetricsMinTimeWindow(Long jobId) {
        Long timeWindow = null;
        try {
            ClickhouseQueryRequest clickhouseQueryRequest = new ClickhouseQueryRequest();
            clickhouseQueryRequest.setMeasurement("t_engine_metrics_all");
            Map<String, String> fieldAndAlias = new HashMap<>();
            fieldAndAlias.put("time", null);
            clickhouseQueryRequest.setFieldAndAlias(fieldAndAlias);
            Map<String, Object> whereFilter = new HashMap<>();
            whereFilter.put("job_id", jobId.toString());
            clickhouseQueryRequest.setWhereFilter(whereFilter);
            clickhouseQueryRequest.setLimitRows(1L);
            clickhouseQueryRequest.setOrderByStrategy(0);
            List<EngineMetrics> metrics = clickhouseQueryService.queryObjectByConditions(clickhouseQueryRequest, EngineMetrics.class);
            if (CollectionUtils.isNotEmpty(metrics)) {
                timeWindow = CollectorUtil.getTimeWindowTime(metrics.get(0).getTime());
            }
        } catch (Throwable e) {
            log.error("查询失败", e);
        }
        return timeWindow;
    }

    private List<EngineMetrics> queryMetrics(Long jobId, Long sceneId, Long timeWindow) {
        try {
            //查询引擎上报数据时，通过时间窗向前5s来查询，(0,5]
            if (null != timeWindow) {
                ClickhouseQueryRequest clickhouseQueryRequest = new ClickhouseQueryRequest();
                clickhouseQueryRequest.setStartTime(timeWindow - TimeUnit.MILLISECONDS.convert(CollectorConstants.SEND_TIME, TimeUnit.SECONDS));
                clickhouseQueryRequest.setStartTimeEqual(false);
                clickhouseQueryRequest.setEndTime(timeWindow);
                clickhouseQueryRequest.setEndTimeEqual(true);
                clickhouseQueryRequest.setMeasurement("t_engine_metrics_all");

                clickhouseQueryRequest.setFieldAndAlias(new HashMap<>());
                Map<String, Object> whereFilter = new HashMap<>();
                whereFilter.put("job_id", jobId.toString());
                clickhouseQueryRequest.setWhereFilter(whereFilter);

                List<EngineMetrics> list = clickhouseQueryService.queryObjectByConditions(clickhouseQueryRequest, EngineMetrics.class);
                log.info("汇总查询日志：sceneId:{},查询结果数量:{}", sceneId, list == null ? "null" : list.size());
                return list;
            } else {
                timeWindow = getMetricsMinTimeWindow(jobId);
                if (null != timeWindow) {
                    return queryMetrics(jobId, sceneId, timeWindow);
                }
            }
        } catch (Throwable e) {
            log.error("查询失败", e);
        }
        return null;
    }

    /**
     * 获取当前未完成统计的最小时间窗口
     */
    private Long getWorkingPressureMinTimeWindow(Long jobId) {
        Long timeWindow = null;
        try {
            ClickhouseQueryRequest clickhouseQueryRequest = new ClickhouseQueryRequest();
            clickhouseQueryRequest.setMeasurement("t_engine_pressure_all");
            Map<String, String> fieldAndAlias = new HashMap<>();
            fieldAndAlias.put("time", null);
            clickhouseQueryRequest.setFieldAndAlias(fieldAndAlias);
            Map<String, Object> whereFilter = new HashMap<>();
            whereFilter.put("job_id", jobId.toString());
            whereFilter.put("status", 0);
            clickhouseQueryRequest.setWhereFilter(whereFilter);
            clickhouseQueryRequest.setLimitRows(1L);
            clickhouseQueryRequest.setOrderByStrategy(0);
            List<EnginePressure> enginePressures = clickhouseQueryService.queryObjectByConditions(clickhouseQueryRequest, EnginePressure.class);
            if (CollectionUtils.isNotEmpty(enginePressures)) {
                timeWindow = enginePressures.get(0).getTime();
            }

        } catch (Throwable e) {
            log.error("查询失败", e);
        }
        return timeWindow;
    }


    /**
     * 获取当前统计的最大时间的下一个窗口窗口
     */
    private Long getPressureMaxTimeNextTimeWindow(Long jobId) {
        Long timeWindow = null;
        try {
            ClickhouseQueryRequest clickhouseQueryRequest = new ClickhouseQueryRequest();
            clickhouseQueryRequest.setMeasurement("t_engine_pressure_all");
            Map<String, String> fieldAndAlias = new HashMap<>();
            fieldAndAlias.put("time", null);
            clickhouseQueryRequest.setFieldAndAlias(fieldAndAlias);
            Map<String, Object> whereFilter = new HashMap<>();
            whereFilter.put("job_id", jobId.toString());
            whereFilter.put("status", 1);
            clickhouseQueryRequest.setWhereFilter(whereFilter);
            clickhouseQueryRequest.setLimitRows(1L);
            clickhouseQueryRequest.setOrderByStrategy(1);
            List<EnginePressure> enginePressures = clickhouseQueryService.queryObjectByConditions(clickhouseQueryRequest, EnginePressure.class);

            if (CollectionUtils.isNotEmpty(enginePressures)) {
                timeWindow = CollectorUtil.getNextTimeWindow(enginePressures.get(0).getTime());
            }
        } catch (Throwable e) {
            log.error("查询失败", e);
        }
        return timeWindow;
    }

    public Long reduceMetrics(Report report, Integer podNum, long endTime, Long timeWindow,
                              List<ScriptNode> nodes, boolean isUpdate) {
        if (null == report) {
            return null;
        }
        Long sceneId = report.getSceneId();
        Long reportId = report.getId();
        Long customerId = report.getTenantId();
        Long jobId = report.getJobId();
        String logPre = String.format("reduceMetrics %s-%s-%s:%s",
                sceneId, reportId, customerId, showTime(timeWindow));
        log.info(logPre + " start!");
        //如果时间窗口为空
        if (null == timeWindow) {
            //则通过当前压测统计表的未完成记录时间进行统计（数据统计有缺失的为未完成）
            timeWindow = getWorkingPressureMinTimeWindow(jobId);
            //如果不存在当前未完成记录时间
            if (null == timeWindow) {
                //则根据最新统计记录时间获取下一个时间窗口
                timeWindow = getPressureMaxTimeNextTimeWindow(jobId);
            }
        }
        //如果当前处理的时间窗口已经大于当结束时间窗口，则退出
        if (null != timeWindow && timeWindow > endTime) {
            log.info("{} return 1!timeWindow={}, endTime={}",
                    logPre, showTime(timeWindow), showTime(endTime));
            return timeWindow;
        }
        //timeWindow如果为空，则获取全部metrics数据，如果不为空则获取该时间窗口的数据
        List<EngineMetrics> metricsList = queryMetrics(jobId, sceneId, timeWindow);
        if (CollectionUtils.isEmpty(metricsList)) {
            log.info("{}, timeWindow={} ， metrics 是空集合!", logPre, showTime(timeWindow));
            return timeWindow;
        }
        log.info("{} queryMetrics timeWindow={}, endTime={}, metricsList.size={}",
                logPre, showTime(timeWindow), showTime(endTime), metricsList.size());
        if (null == timeWindow) {
            timeWindow = metricsList.stream().filter(Objects::nonNull)
                    .map(t -> CollectorUtil.getTimeWindowTime(t.getTime()))
                    .filter(l -> l > 0)
                    .findFirst()
                    .orElse(endTime);
        }
        //如果当前处理的时间窗口已经大于结束时间窗口，则退出
        if (timeWindow > endTime) {
            log.info("{} return 3!timeWindow={}, endTime={}",
                    logPre, showTime(timeWindow), showTime(endTime));
            return timeWindow;
        }

        List<String> transactions = metricsList.stream().filter(Objects::nonNull)
                .map(EngineMetrics::getTransaction)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(transactions)) {
            log.info("{} return 4!transactions is empty!", logPre);
            return timeWindow;
        }

        long time = timeWindow;

        List<EnginePressure> results = transactions.stream().filter(StringUtils::isNotBlank)
                .map(s -> this.filterByTransaction(metricsList, s))
                .filter(CollectionUtils::isNotEmpty)
                .map(l -> this.toPressureOutput(l, podNum, time))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(results)) {
            log.info("results is empty!");
            return timeWindow;
        }

        //统计没有回传的节点数据
        if (CollectionUtils.isNotEmpty(nodes)) {
            Map<String, EnginePressure> pressureMap = results.stream().filter(Objects::nonNull)
                    .collect(Collectors.toMap(EnginePressure::getTransaction, o -> o, (o1, o2) -> o1));
            nodes.stream().filter(Objects::nonNull)
                    .forEach(n -> countPressure(n, pressureMap, results));
        }
        if (isUpdate) {
            results.forEach(enginePressure -> {
                try {
                    String delSql = "delete from t_engine_pressure where time= ? and job_id = ? and test_name= ?";
                    List<Object[]> batchs = Lists.newArrayList();
                    batchs.add(new Object[]{enginePressure.getTime(), enginePressure.getJobId(), enginePressure.getTransaction()});
                    Map<String, List<Object[]>> objMap = Maps.newHashMap();
                    objMap.put(enginePressure.getJobId(), batchs);
                    clickHouseShardSupport.syncBatchUpdate(delSql, objMap);
                } catch (Exception e) {
                    log.error("删除EnginePressure数据出现异常", e);
                }
            });
        }

        //构建sql
        EnginePressure pressure = results.get(0);
        Map<String, Object> engineMap = this.getEngineMap(pressure);
        String cols = Joiner.on(',').join(engineMap.keySet());
        List<String> params = new ArrayList<>();
        for (String field : engineMap.keySet()) {
            params.add("?");
        }
        String param = Joiner.on(',').join(params);
        Map<String, List<Object[]>> objMap = Maps.newHashMap();
        String tableName = clickHouseShardSupport.isCluster() ? "t_engine_pressure" : "t_engine_pressure_all";
        String sql = "insert into " + tableName + " (" + cols + ") values(" + param + ") ";

        //构建批量参数
        List<Object[]> batchs = Lists.newArrayList();
        results.forEach(enginePressure -> {
            Map<String, Object> map = this.getEngineMap(enginePressure);
            batchs.add(map.values().toArray());
        });
        objMap.put(pressure.getJobId(), batchs);
        log.info("汇总pressure数据，本次共{}条", batchs.size());
        clickHouseShardSupport.syncBatchUpdate(sql, objMap);

        //补充数据不再发送kafka
        if (isUpdate) {
            return null;
        }
        //发送前处理数据，不需要发送sa明细数据
        results.forEach(enginePressure -> {
            enginePressure.setSaPercent(null);
        });
        kafkaMessageInstance.send(topicUrl, new HashMap<>(), JsonHelper.bean2Json(results), new MessageSendCallBack() {
            @Override
            public void success() {
            }

            @Override
            public void fail(String errorMessage) {
                log.error("发送engine-pressure-data出现异常:{}", errorMessage);
            }
        }, new HttpSender() {
            @Override
            public void sendMessage() {
            }
        });
        log.info("{} finished!timeWindow={}, endTime={}", logPre, showTime(timeWindow), showTime(endTime));
        return timeWindow;
    }

    private Map<String, Object> getEngineMap(EnginePressure enginePressure) {
        Map<String, Object> result = new HashMap<>();
        result.put("time", enginePressure.getTime());
        result.put("transaction", enginePressure.getTransaction());
        result.put("avg_rt", enginePressure.getAvgRt());
        result.put("avg_tps", enginePressure.getAvgTps());
        result.put("test_name", enginePressure.getTestName());
        result.put("count", enginePressure.getCount());
        result.put("create_time", enginePressure.getCreateTime());
        result.put("data_num", enginePressure.getDataNum());
        result.put("data_rate", enginePressure.getDataRate());
        result.put("fail_count", enginePressure.getFailCount());
        result.put("sent_bytes", enginePressure.getSentBytes());
        result.put("received_bytes", enginePressure.getReceivedBytes());
        result.put("sum_rt", enginePressure.getSumRt());
        result.put("sa", enginePressure.getSa());
        result.put("sa_count", enginePressure.getSaCount());
        result.put("max_rt", enginePressure.getMaxRt());
        result.put("min_rt", enginePressure.getMinRt());
        result.put("active_threads", enginePressure.getActiveThreads());
        result.put("sa_percent", enginePressure.getSaPercent());
        result.put("status", enginePressure.getStatus());
        result.put("success_rate", enginePressure.getSuccessRate());
        result.put("job_id", enginePressure.getJobId());
        result.put("createDate", enginePressure.getCreateDate());
        //去掉值为null的数据
        Map<String, Object> copy = new HashMap<>();
        result.forEach((k, v) -> {
            if (v != null) {
                copy.put(k, v);
            }
        });
        return copy;
    }


    /**
     * 统计各个节点的数据
     *
     * @param node      节点
     * @param sourceMap 现有的数据和节点映射（jmeter上报的原生数据统计）
     * @param results   数据结果集合
     * @return 返回当前节点的统计结果
     */
    private EnginePressure countPressure(ScriptNode node, Map<String, EnginePressure> sourceMap,
                                         List<EnginePressure> results) {
        if (null == node || StringUtils.isBlank(node.getXpathMd5()) || null == sourceMap) {
            return null;
        }
        //sourceMap中的key都是jmeter上报的
        EnginePressure result = sourceMap.get(node.getXpathMd5());
        if (null != result) {
            return result;
        }
        if (CollectionUtils.isEmpty(node.getChildren())) {
            return null;
        }
        List<EnginePressure> childPressures = node.getChildren().stream().filter(Objects::nonNull)
                .map(n -> countPressure(n, sourceMap, results))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        result = countPressure(node, childPressures, sourceMap);
        if (null != result) {
            results.add(result);
        }
        return result;
    }

    /**
     * 根据子节点统计结果来统计当前节点的数据
     *
     * @param node           当前节点
     * @param childPressures 子节点统计数据结果(1级子节点)
     * @return 返回当前节点统计结果
     */
    private EnginePressure countPressure(ScriptNode node, List<EnginePressure> childPressures,
                                         Map<String, EnginePressure> sourceMap) {
        if (CollectionUtils.isEmpty(childPressures)) {
            return null;
        }
        long time = childPressures.stream().filter(Objects::nonNull)
                .mapToLong(EnginePressure::getTime)
                .findAny()
                .orElse(0L);
        if (0 == time) {
            return null;
        }
        /*================== childPressures只用1级子节点 统计开始 =====================================================*/
        int activeThreads;
        if (NodeTypeEnum.TEST_PLAN == node.getType()) {//TEST_PLAN节点取加总
            activeThreads = NumberUtil.sum(childPressures, EnginePressure::getActiveThreads);
        } else {//其他分组节点（控制器、线程组）：取平均
            activeThreads = NumberUtil.maxInt(childPressures, EnginePressure::getActiveThreads);
        }
        long sendBytes = NumberUtil.sumLong(childPressures, EnginePressure::getSentBytes);
        long receiveBytes = NumberUtil.sumLong(childPressures, EnginePressure::getReceivedBytes);
        int dataNum = NumberUtil.minInt(childPressures, EnginePressure::getDataNum, 1);
        BigDecimal dataRate = NumberUtil.minBigDecimal(childPressures, EnginePressure::getDataRate, new BigDecimal("1.00"));
        int status = NumberUtil.minInt(childPressures, EnginePressure::getStatus, 1);
        /*=================== childPressures只用1级子节点 统计结束 =====================================================/

        /*=================== subPressures含有1级子节点和事务控制器这种子节点的子节点 计开始 ===============================*/
        /*
         * 多级子节点,满足过滤条件的多级子节点:如果该节点的子节点有自己上报数据，则继续递归取其子节点的子节点：
         * 这里事务控制器会自己上报数据，当父节点包含事务控制器时，会取事务控制器和事务控制的子节点合并计算
         */
        List<ScriptNode> childNodes = JmxUtil.getChildNodesByFilterFunc(node,
                n -> sourceMap.containsKey(n.getXpathMd5()));
        List<String> samples = Lists.newArrayList();
        List<EnginePressure> subPressures = Lists.newArrayList(childPressures);
        if (CollectionUtils.isNotEmpty(childNodes)) {
            samples = childNodes.stream().filter(t -> NodeTypeEnum.SAMPLER == t.getType())
                    .map(ScriptNode::getXpathMd5).collect(Collectors.toList());
            childNodes.stream().filter(Objects::nonNull)
                    .map(ScriptNode::getXpathMd5)
                    .filter(StringUtils::isNotBlank)
                    .map(sourceMap::get)
                    .filter(Objects::nonNull)
                    .filter(d -> !childPressures.contains(d))
                    .forEach(subPressures::add);
        }
        final List<String> finalSample  = samples;
        subPressures =  subPressures.stream().filter(t -> (t.getType() != null && NodeTypeEnum.CONTROLLER != t.getType())
                || finalSample.contains(t.getTransaction())).collect(Collectors.toList());
        int count = NumberUtil.sum(subPressures, EnginePressure::getCount);
        int failCount = NumberUtil.sum(subPressures, EnginePressure::getFailCount);
        int saCount = NumberUtil.sum(subPressures, EnginePressure::getSaCount);
        BigDecimal sumRt = NumberUtil.sumBigDecimal(subPressures, EnginePressure::getSumRt);
        BigDecimal maxRt = NumberUtil.maxBigDecimal(subPressures, EnginePressure::getMaxRt);
        BigDecimal minRt = NumberUtil.minBigDecimal(subPressures, EnginePressure::getMinRt);
        BigDecimal avgTps = NumberUtil.sumBigDecimal(subPressures, EnginePressure::getAvgTps);
        List<String> strings = subPressures.stream().map(EnginePressure::getSaPercent).collect(Collectors.toList());
        String percentSa = calculateSaPercent(strings);
        /*=================== subPressures含有1级子节点和事务控制器这种子节点的子节点 计结束 ===============================*/

        double sa = NumberUtil.getPercentRate(saCount, count);
        double successRate = NumberUtil.getPercentRate(Math.max(0, count - failCount), count);
        double avgRt = NumberUtil.getRate(sumRt, count);

        EnginePressure output = new EnginePressure();
        output.setJobId(childPressures.get(0).getJobId());
        output.setTime(time);
        output.setTransaction(node.getXpathMd5());
        output.setCount(count);
        output.setFailCount(failCount);
        output.setSaCount(saCount);
        output.setSa(new BigDecimal(sa));
        output.setSuccessRate(new BigDecimal(successRate));
        output.setSentBytes(sendBytes);
        output.setReceivedBytes(receiveBytes);
        output.setSumRt(sumRt);
        output.setAvgRt(new BigDecimal(avgRt));
        output.setMaxRt(maxRt);
        output.setMinRt(minRt);
        output.setActiveThreads(activeThreads);
        output.setAvgTps(avgTps);
        output.setSaPercent(percentSa);
        output.setDataNum(dataNum);
        output.setDataRate(dataRate);
        output.setStatus(status);
        output.setTestName(node.getTestName());
        return output;
    }

    /**
     * 单个时间窗口数据，根据transaction过滤
     */
    private List<EngineMetrics> filterByTransaction(List<EngineMetrics> metricsList, String transaction) {
        if (CollectionUtils.isEmpty(metricsList)) {
            return metricsList;
        }
        return metricsList.stream().filter(Objects::nonNull)
                .filter(m -> transaction.equals(m.getTransaction()))
                .collect(Collectors.toList());
    }

    /**
     * 实时数据统计
     */
    private EnginePressure toPressureOutput(List<EngineMetrics> metricsList, Integer podNum, long time) {
        if (CollectionUtils.isEmpty(metricsList)) {
            return null;
        }
        String transaction = metricsList.get(0).getTransaction();
        String testName = metricsList.get(0).getTestName();
        String jobId = metricsList.get(0).getJobId();

        int count = NumberUtil.sum(metricsList, EngineMetrics::getCount);
        int failCount = NumberUtil.sum(metricsList, EngineMetrics::getFailCount);
        int saCount = NumberUtil.sum(metricsList, EngineMetrics::getSaCount);
        double sa = NumberUtil.getPercentRate(saCount, count);
        double successRate = NumberUtil.getPercentRate(count - failCount, count);
        long sendBytes = NumberUtil.sumLong(metricsList, EngineMetrics::getSentBytes);
        long receivedBytes = NumberUtil.sumLong(metricsList, EngineMetrics::getReceivedBytes);
        double sumRt = metricsList.stream().filter(Objects::nonNull).map(EngineMetrics::getSumRt).mapToDouble(BigDecimal::doubleValue).sum();
        double avgRt = NumberUtil.getRate(sumRt, count);
        BigDecimal maxRt = NumberUtil.maxBigDecimal(metricsList, EngineMetrics::getMaxRt);
        BigDecimal minRt = NumberUtil.minBigDecimal(metricsList, EngineMetrics::getMinRt);
        int activeThreads = NumberUtil.sum(metricsList, EngineMetrics::getActiveThreads);
        double avgTps = NumberUtil.getRate(count, CollectorConstants.SEND_TIME);
        List<String> percentDataList = metricsList.stream().filter(Objects::nonNull)
                .map(EngineMetrics::getPercentData)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        String percentSa = calculateSaPercent(percentDataList);
        Set<String> podNos = metricsList.stream().filter(Objects::nonNull)
                .map(EngineMetrics::getPodNo)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());

        int dataNum = CollectionUtils.isEmpty(podNos) ? 0 : podNos.size();
        double dataRate = NumberUtil.getPercentRate(dataNum, podNum, 100d);
        int status = dataNum < podNum ? 0 : 1;
        EnginePressure p = new EnginePressure();
        p.setJobId(jobId);
        p.setTime(time);
        p.setTransaction(transaction);
        p.setCount(count);
        p.setFailCount(failCount);
        p.setSaCount(saCount);
        p.setSa(new BigDecimal(sa));
        p.setSuccessRate(new BigDecimal(successRate));
        p.setSentBytes(sendBytes);
        p.setReceivedBytes(receivedBytes);
        p.setSumRt(new BigDecimal(sumRt));
        p.setAvgRt(new BigDecimal(avgRt));
        p.setMaxRt(maxRt);
        p.setMinRt(minRt);
        p.setActiveThreads(activeThreads);
        p.setAvgTps(new BigDecimal(avgTps));
        p.setSaPercent(percentSa);
        p.setDataNum(dataNum);
        p.setDataRate(new BigDecimal(dataRate));
        p.setStatus(status);
        p.setTestName(testName);
        return p;
    }

    private void finishPushData(Report report, Integer podNum, Long timeWindow, long endTime, List<ScriptNode> nodes) {
        if (null == report) {
            return;
        }
        Long sceneId = report.getSceneId();
        Long reportId = report.getId();
        Long customerId = report.getTenantId();
        long nowTimeWindow = CollectorUtil.getTimeWindowTime(System.currentTimeMillis());
        log.info("finishPushData {}-{}-{}  timeWindow={}, endTime={}, now={}", sceneId, reportId, customerId,
                showTime(timeWindow), showTime(endTime), showTime(nowTimeWindow));

        if (null != report.getEndTime()) {
            endTime = Math.min(endTime, report.getEndTime().toInstant(ZoneOffset.of("+8")).toEpochMilli());
        }

        if (null != timeWindow && timeWindow > endTime) {
            long endTimeWindow = CollectorUtil.getTimeWindowTime(endTime);
            log.info("触发收尾操作，当前时间窗口：{},结束时间窗口：{},", showTime(timeWindow), showTime(endTimeWindow));
            // 比较 endTime timeWindow
            // 如果结束时间 小于等于当前时间，数据不用补充，
            // 如果结束时间 大于 当前时间，需要补充期间每5秒的数据 延后5s
            while (isSaveLastPoint && timeWindow <= endTimeWindow && timeWindow <= nowTimeWindow) {
                timeWindow = reduceMetrics(report, podNum, endTime, timeWindow, nodes, false);
                timeWindow = CollectorUtil.getNextTimeWindow(timeWindow);
            }
            log.info("本次压测{}-{}-{},push data 完成", sceneId, reportId, customerId);
            log.info("---> 本次压测{}-{}-{}完成，已发送finished事件！<------", sceneId, reportId, customerId);
        }
    }


    /**
     * 计算sa
     */
    private String calculateSaPercent(List<String> percentDataList) {
        if (CollectionUtils.isEmpty(percentDataList)) {
            return null;
        }
        List<Map<Integer, RtDataOutput>> percentMapList = percentDataList.stream().filter(StringUtils::isNotBlank)
                .map(DataUtils::parseToPercentMap)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(percentMapList)) {
            return null;
        }
        //请求总数
        int total = percentMapList.stream().filter(Objects::nonNull)
                .map(m -> m.get(100))
                .filter(Objects::nonNull)
                .mapToInt(RtDataOutput::getHits)
                .sum();

        //所有rt按耗时排序
        List<RtDataOutput> rtDataList = percentMapList.stream().filter(Objects::nonNull)
                .peek(DataUtils::percentMapRemoveDuplicateHits)
                .map(Map::values)
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(RtDataOutput::getTime))
                .collect(Collectors.toList());

        Map<Integer, RtDataOutput> result = new HashMap<>(100);
        //计算逻辑
        //每个百分点的目标请求数，如果统计达标，进行下一个百分点的统计，如果tong ji
        for (int i = 1; i <= 100; i++) {
            int hits = 0;
            int time = 0;
            double need = total * i / 100d;
            for (RtDataOutput d : rtDataList) {
                if (hits < need || d.getTime() <= time) {
                    hits += d.getHits();
                    if (d.getTime() > time) {
                        time = d.getTime();
                    }
                }
            }
            result.put(i, new RtDataOutput(hits, time));
        }
        return DataUtils.percentMapToString(result);
    }


    private String showTime(Long timestamp) {
        if (null == timestamp) {
            return "";
        }
        // 忽略时间精度到天
        long d1 = timestamp / DateUnit.DAY.getMillis();
        long d2 = System.currentTimeMillis() / DateUnit.DAY.getMillis();
        // 转换时间
        cn.hutool.core.date.DateTime timestampDate = cn.hutool.core.date.DateUtil.date(timestamp);
        String timeString = d1 == d2 ?
                // 同一日则显示时间 HH:mm:ss
                timestampDate.toTimeStr() :
                // 不同日则显示日期时间 yyyy-MM-dd HH:mm:ss
                timestampDate.toString();
        // 返回
        return timestamp + "(" + timeString + ")";
    }

    public void combineMetricsData(Report r, boolean dataCalibration, Runnable finalAction) {
        if (Objects.isNull(r)) {
            return;
        }
        Runnable runnable = () -> {
            Long sceneId = r.getSceneId();
            String lockKey = String.format("pushData:%s:%s:%s", sceneId, r.getId(), r.getTenantId());
            RLock lock = redissonClient.getLock(this.getLockPrefix(lockKey));

            try {
                if (!lock.tryLock(10, 60 * 1000, TimeUnit.MILLISECONDS)) {
                    log.info("获取锁:{}失败，本次先不计算", lockKey);
                    return;
                }
                log.info("开始统计数据:" + System.currentTimeMillis());

                ClickhouseQueryRequest clickhouseQueryRequest = new ClickhouseQueryRequest();
                clickhouseQueryRequest.setMeasurement("t_engine_metrics_all");
                Map<String, String> field = new HashMap<>();
                field.put("job_id", "jobId");
                clickhouseQueryRequest.setFieldAndAlias(field);
                Map<String, Object> whereFilter = new HashMap<>();
                whereFilter.put("job_id", r.getJobId().toString());
                clickhouseQueryRequest.setWhereFilter(whereFilter);
                clickhouseQueryRequest.setLimitRows(1);
                List<EngineMetrics> EngineMetricss = clickhouseQueryService.queryObjectByConditions(clickhouseQueryRequest, EngineMetrics.class);
                if (CollectionUtils.isEmpty(EngineMetricss)) {
                    log.info("没有找到当前jobId:{}的Metrics数据", r.getJobId());
                    return;
                }
                log.info("查询压测场景:" + System.currentTimeMillis());
                List<ScriptNode> nodes = JsonHelper.json2List(r.getScriptNodeTree(), ScriptNode.class);
                SceneManage sceneManage = iSceneManageService.getById(sceneId);
                if (null == sceneManage) {
                    log.info("no such scene manager!sceneId=" + sceneId);
                    return;
                }
                PtConfigExt ptConfig = JsonHelper.json2Bean(sceneManage.getPtConfig(), PtConfigExt.class);
                if (null == ptConfig) {
                    log.info("scene manager no such ptConfig!sceneId=" + sceneId);
                    return;
                }
                Long pressureTestSecond = convertTime(ptConfig.getDuration(), ptConfig.getUnit());

                //结束时间取开始压测时间-10s+总测试时间+3分钟， 3分钟富裕时间，给与pod启动和压测引擎启动延时时间
                long endTime = TimeUnit.MINUTES.toMillis(3L);
                if (null != r.getStartTime()) {
                    endTime += (r.getStartTime().toInstant(ZoneOffset.of("+8")).toEpochMilli() - TimeUnit.SECONDS.toMillis(10));
                } else if (null != r.getGmtCreate()) {
                    endTime += r.getGmtCreate().toInstant(ZoneOffset.of("+8")).toEpochMilli();
                }

                endTime += TimeUnit.SECONDS.toMillis(pressureTestSecond);

                int podNum = ptConfig.getPodNum();
                long nowTimeWindow = CollectorUtil.getNowTimeWindow();
                long breakTime = Math.min(endTime, nowTimeWindow);
                if (dataCalibration) {
                    // 数据校准时需要增加一个时间窗口，避免因为校准回调太快(与endTime正好处于一个时间窗口)数据缺失
                    breakTime = CollectorUtil.getNextTimeWindow(Math.min(endTime, nowTimeWindow));
                }
                Long timeWindow = null;
                do {
                    //不用递归，而是采用do...while...的方式是防止需要处理的时间段太长引起stackOverFlow错误
                    log.info("处理metrics数据:" + System.currentTimeMillis());
                    timeWindow = reduceMetrics(r, podNum, breakTime, timeWindow, nodes, false);
                    if (null == timeWindow) {
                        timeWindow = nowTimeWindow;
                        break;
                    }
                    timeWindow = CollectorUtil.getNextTimeWindow(timeWindow);
                } while (timeWindow <= breakTime);

                log.info("结尾数据处理:" + System.currentTimeMillis());
                finishPushData(r, podNum, timeWindow, endTime, nodes);
                log.info("数据处理完成:" + System.currentTimeMillis());
            } catch (Throwable t) {
                log.error("pushData2 error!", t);
            } finally {
                if (Objects.nonNull(finalAction)) {
                    finalAction.run();
                }
                log.info("开始释放锁:" + System.currentTimeMillis());
                lock.unlock();
                log.info("释放锁完成:" + System.currentTimeMillis());
            }
        };
        Executors.newCachedThreadPool().execute(runnable);
    }


    private Long convertTime(Long time, String unit) {
        if (time == null) {
            return 0L;
        }
        TimeUnitEnum tue = TimeUnitEnum.value(unit);
        if (null == tue) {
            return time;
        }
        return TimeUnit.SECONDS.convert(time, tue.getUnit());
    }

    private String getLockPrefix(String key) {
        return String.format("COLLECTOR:LOCK:%s", key);
    }

}
