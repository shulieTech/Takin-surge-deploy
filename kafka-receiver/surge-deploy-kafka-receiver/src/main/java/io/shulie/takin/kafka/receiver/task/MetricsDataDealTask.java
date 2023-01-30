package io.shulie.takin.kafka.receiver.task;

import cn.hutool.core.date.DateUnit;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import io.shulie.takin.kafka.receiver.constant.web.*;
import io.shulie.takin.kafka.receiver.dto.web.PtConfigExt;
import io.shulie.takin.kafka.receiver.dto.web.RtDataOutput;
import io.shulie.takin.kafka.receiver.dto.web.ScriptNode;
import io.shulie.takin.kafka.receiver.entity.*;
import io.shulie.takin.kafka.receiver.service.*;
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MetricsDataDealTask implements InitializingBean {

    @Resource
    private IReportService iReportService;
    @Resource
    private IEngineMetricsAllService iEngineMetricsAllService;
    @Resource
    private IEnginePressureService iEnginePressureService;
    @Resource
    private IEnginePressureAllService iEnginePressureAllService;
    @Resource
    private ISceneManageService iSceneManageService;
    @Value("${report.metric.isSaveLastPoint:true}")
    private boolean isSaveLastPoint;
    @Resource
    protected RedisTemplate<String, Object> redisTemplate;

    private final MessageSendService kafkaMessageInstance = new KafkaSendServiceFactory().getKafkaMessageInstance();
    private static final String topicUrl = "engine/pressure/data";
    private DefaultRedisScript<Void> unlockRedisScript;
    private static final String UNLOCK_SCRIPT = "if redis.call('exists',KEYS[1]) == 1 then\n" +
            "   redis.call('del',KEYS[1])\n" +
            "else\n" +
            "end";

    @Override
    public void afterPropertiesSet() throws Exception {

        unlockRedisScript = new DefaultRedisScript<>();
        unlockRedisScript.setResultType(Void.class);
        unlockRedisScript.setScriptText(UNLOCK_SCRIPT);
    }

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

        //获取结束时间在30s之内的报告
        QueryWrapper<Report> reportQueryWrapper = new QueryWrapper<>();
        reportQueryWrapper.lambda().eq(Report::getIsDeleted, 0);
        reportQueryWrapper.lambda().ne(Report::getPressureType, PressureSceneEnum.INSPECTION_MODE.getCode());
        reportQueryWrapper.lambda().isNotNull(Report::getJobId);
        reportQueryWrapper.lambda().isNotNull(Report::getEndTime);
        reportQueryWrapper.lambda().ge(Report::getEndTime, LocalDateTime.now().plusSeconds(-30));
        List<Report> list = iReportService.list(reportQueryWrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            all.addAll(list);
        }
        if (CollectionUtils.isEmpty(all)) {
            log.debug("没有需要统计的报告！");
            return;
        }
        List<Long> reportIds = all.stream().map(Report::getId).collect(Collectors.toList());
        log.info("找到需要统计的报告：" + JsonHelper.bean2Json(reportIds));
        all.stream().filter(Objects::nonNull).forEach(r -> combineMetricsData(r, false, null));
    }


    private Long getMetricsMinTimeWindow(Long jobId) {
        Long timeWindow = null;
        try {
            QueryWrapper<EngineMetricsAll> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(EngineMetricsAll::getJobId, jobId);
            queryWrapper.lambda().orderByAsc(EngineMetricsAll::getTime);
            queryWrapper.lambda().last("limit 1");
            List<EngineMetricsAll> metrics = iEngineMetricsAllService.list(queryWrapper);
            if (CollectionUtils.isNotEmpty(metrics)) {
                timeWindow = CollectorUtil.getTimeWindowTime(metrics.get(0).getTime());
            }
        } catch (Throwable e) {
            log.error("查询失败", e);
        }
        return timeWindow;
    }

    private List<EngineMetricsAll> queryMetrics(Long jobId, Long sceneId, Long timeWindow) {
        try {
            //查询引擎上报数据时，通过时间窗向前5s来查询，(0,5]
            if (null != timeWindow) {
                QueryWrapper<EngineMetricsAll> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(EngineMetricsAll::getJobId, jobId);
                queryWrapper.lambda().le(EngineMetricsAll::getTime, timeWindow);
                queryWrapper.lambda().gt(EngineMetricsAll::getTime, timeWindow - TimeUnit.MILLISECONDS.convert(CollectorConstants.SEND_TIME, TimeUnit.SECONDS));
                List<EngineMetricsAll> list = iEngineMetricsAllService.list(queryWrapper);
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
            QueryWrapper<EnginePressureAll> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(EnginePressureAll::getJobId, jobId);
            queryWrapper.lambda().eq(EnginePressureAll::getStatus, 0);
            queryWrapper.lambda().orderByAsc(EnginePressureAll::getTime);
            queryWrapper.lambda().last("limit 1");
            List<EnginePressureAll> enginePressureAlls = iEnginePressureAllService.list(queryWrapper);
            if (CollectionUtils.isNotEmpty(enginePressureAlls)) {
                timeWindow = enginePressureAlls.get(0).getTime();
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
            QueryWrapper<EnginePressureAll> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(EnginePressureAll::getJobId, jobId);
            queryWrapper.lambda().eq(EnginePressureAll::getStatus, 1);
            queryWrapper.lambda().orderByDesc(EnginePressureAll::getTime);
            queryWrapper.lambda().last("limit 1");
            List<EnginePressureAll> enginePressureAlls = iEnginePressureAllService.list(queryWrapper);
            if (CollectionUtils.isNotEmpty(enginePressureAlls)) {
                timeWindow = CollectorUtil.getNextTimeWindow(enginePressureAlls.get(0).getTime());
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
        List<EngineMetricsAll> metricsList = queryMetrics(jobId, sceneId, timeWindow);
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
                .map(EngineMetricsAll::getTransaction)
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
                QueryWrapper<EnginePressure> updateQueryWrapper = new QueryWrapper<>();
                updateQueryWrapper.lambda().eq(EnginePressure::getJobId, enginePressure.getJobId());
                updateQueryWrapper.lambda().eq(EnginePressure::getTime, enginePressure.getTime());
                updateQueryWrapper.lambda().eq(EnginePressure::getTestName, enginePressure.getTransaction());
                iEnginePressureService.saveOrUpdate(enginePressure, updateQueryWrapper);
            });
            return null;
        }
        iEnginePressureService.saveBatch(results);

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
        final List<EnginePressure> subPressures = Lists.newArrayList(childPressures);
        if (CollectionUtils.isNotEmpty(childNodes)) {
            childNodes.stream().filter(Objects::nonNull)
                    .map(ScriptNode::getXpathMd5)
                    .filter(StringUtils::isNotBlank)
                    .map(sourceMap::get)
                    .filter(Objects::nonNull)
                    .filter(d -> !childPressures.contains(d))
                    .forEach(subPressures::add);
        }
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
    private List<EngineMetricsAll> filterByTransaction(List<EngineMetricsAll> metricsList, String transaction) {
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
    private EnginePressure toPressureOutput(List<EngineMetricsAll> metricsList, Integer podNum, long time) {
        if (CollectionUtils.isEmpty(metricsList)) {
            return null;
        }
        String transaction = metricsList.get(0).getTransaction();
        String testName = metricsList.get(0).getTestName();

        int count = NumberUtil.sum(metricsList, EngineMetricsAll::getCount);
        int failCount = NumberUtil.sum(metricsList, EngineMetricsAll::getFailCount);
        int saCount = NumberUtil.sum(metricsList, EngineMetricsAll::getSaCount);
        double sa = NumberUtil.getPercentRate(saCount, count);
        double successRate = NumberUtil.getPercentRate(count - failCount, count);
        long sendBytes = NumberUtil.sumLong(metricsList, EngineMetricsAll::getSentBytes);
        long receivedBytes = NumberUtil.sumLong(metricsList, EngineMetricsAll::getReceivedBytes);
        double sumRt = metricsList.stream().filter(Objects::nonNull).map(EngineMetricsAll::getSumRt).mapToDouble(BigDecimal::doubleValue).sum();
        double avgRt = NumberUtil.getRate(sumRt, count);
        BigDecimal maxRt = NumberUtil.maxBigDecimal(metricsList, EngineMetricsAll::getMaxRt);
        BigDecimal minRt = NumberUtil.minBigDecimal(metricsList, EngineMetricsAll::getMinRt);
        int activeThreads = NumberUtil.sum(metricsList, EngineMetricsAll::getActiveThreads);
        double avgTps = NumberUtil.getRate(count, CollectorConstants.SEND_TIME);
        List<String> percentDataList = metricsList.stream().filter(Objects::nonNull)
                .map(EngineMetricsAll::getPercentData)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        String percentSa = calculateSaPercent(percentDataList);
        Set<String> podNos = metricsList.stream().filter(Objects::nonNull)
                .map(EngineMetricsAll::getPodNo)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());

        int dataNum = CollectionUtils.isEmpty(podNos) ? 0 : podNos.size();
        double dataRate = NumberUtil.getPercentRate(dataNum, podNum, 100d);
        int status = dataNum < podNum ? 0 : 1;
        EnginePressure p = new EnginePressure();
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
            if (!lock(lockKey, "1")) {
                return;
            }

            try {
                QueryWrapper<EngineMetricsAll> engineMetricsAllQueryWrapper = new QueryWrapper<>();
                engineMetricsAllQueryWrapper.lambda().eq(EngineMetricsAll::getJobId, r.getJobId());
                engineMetricsAllQueryWrapper.lambda().last("limit 1");
                List<EngineMetricsAll> engineMetricsAlls = iEngineMetricsAllService.list(engineMetricsAllQueryWrapper);
                if (CollectionUtils.isEmpty(engineMetricsAlls)) {
                    log.info("没有找到当前jobId:{}的Metrics数据", r.getJobId());
                    return;
                }

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
                    timeWindow = reduceMetrics(r, podNum, breakTime, timeWindow, nodes, false);
                    if (null == timeWindow) {
                        timeWindow = nowTimeWindow;
                        break;
                    }
                    timeWindow = CollectorUtil.getNextTimeWindow(timeWindow);
                } while (timeWindow <= breakTime);

                if (!dataCalibration && null != r.getEndTime() && timeWindow >= r.getEndTime().toInstant(ZoneOffset.of("+8")).toEpochMilli()) {
                    // 更新压测场景状态  压测引擎运行中,压测引擎停止压测 ---->压测引擎停止压测
                    QueryWrapper<SceneManage> sceneManageQueryWrapper = new QueryWrapper<>();
                    sceneManageQueryWrapper.lambda().eq(SceneManage::getId, sceneId);
                    sceneManageQueryWrapper.lambda().in(SceneManage::getStatus, Arrays.asList(SceneManageStatusEnum.ENGINE_RUNNING.getValue(), SceneManageStatusEnum.STOP.getValue()));
                    sceneManage.setUpdateTime(LocalDateTime.now());
                    sceneManage.setStatus(SceneManageStatusEnum.STOP.getValue());
                    iSceneManageService.update(sceneManage, sceneManageQueryWrapper);
                }
                if (!dataCalibration) {
                    finishPushData(r, podNum, timeWindow, endTime, nodes);
                }
            } catch (Throwable t) {
                log.error("pushData2 error!", t);
            } finally {
                if (Objects.nonNull(finalAction)) {
                    finalAction.run();
                }
                unlock(lockKey, "0");
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

    public Boolean lock(String key, String value) {
        return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            Boolean bl = connection.set(getLockPrefix(key).getBytes(), value.getBytes(), Expiration.seconds(60),
                    RedisStringCommands.SetOption.SET_IF_ABSENT);
            return null != bl && bl;
        });
    }

    public void unlock(String key, String value) {
        redisTemplate.execute(unlockRedisScript, Lists.newArrayList(getLockPrefix(key)), value);
    }

    private String getLockPrefix(String key) {
        return String.format("COLLECTOR LOCK:%s", key);
    }


}
