package io.shulie.takin.kafka.receiver.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.shulie.surge.data.sink.clickhouse.ClickHouseShardSupport;
import io.shulie.takin.kafka.receiver.constant.cloud.PressureEngineConstants;
import io.shulie.takin.kafka.receiver.dao.cloud.PressureMapper;
import io.shulie.takin.kafka.receiver.dto.cloud.MetricsInfo;
import io.shulie.takin.kafka.receiver.dto.web.PtConfigExt;
import io.shulie.takin.kafka.receiver.dto.web.ScriptNode;
import io.shulie.takin.kafka.receiver.entity.*;
import io.shulie.takin.kafka.receiver.service.*;
import io.shulie.takin.kafka.receiver.task.MetricsDataDealTask;
import io.shulie.takin.kafka.receiver.util.CollectorUtil;
import io.shulie.takin.utils.json.JsonHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 任务 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@Service
public class PressureServiceImpl extends ServiceImpl<PressureMapper, Pressure> implements IPressureService {

    private static final Logger log = LoggerFactory.getLogger(PressureServiceImpl.class);
    @Resource
    private IPressureExampleService iPressureExampleService;
    @Resource
    private MetricsDataDealTask metricsDataDealTask;
    @Resource
    private IReportService iReportService;
    @Resource
    private ClickHouseShardSupport clickHouseShardSupport;

    public static final long MAX_ACCEPT_TIMESTAMP = 9223372036854L;

    @Override
    public void upload(List<MetricsInfo> metricsInfos, Long jobId) {
        if (metricsInfos.isEmpty() || jobId == null) {
            log.warn("没有获取到metrics数据或者jobId");
            return;
        }
        // 兼容老版本
        Pressure pressure = this.getById(jobId);
        if (pressure == null) {
            log.warn("没根据jobId获取到Pressure,jobId:{}", jobId);
            return;
        }
        String pressureExampleNumberString = metricsInfos.get(0).getPodNo();
        Integer pressureExampleNumber = Integer.parseInt(pressureExampleNumberString);
        // 根据任务和任务实例编号找到任务实例
        QueryWrapper<PressureExample> pressureExampleQueryWrapper = new QueryWrapper<>();
        pressureExampleQueryWrapper.lambda().eq(PressureExample::getPressureId, jobId);
        List<PressureExample> pressureExamples = iPressureExampleService.list(pressureExampleQueryWrapper);
        PressureExample pressureExampleEntity = pressureExamples.stream().filter(t -> t.getNumber().equals(pressureExampleNumber)).findFirst().orElse(null);
        if (pressureExampleEntity == null) {
            log.warn("未找到任务:{}对应,实例编号:{}对应的任务实例", jobId, pressureExampleNumberString);
            return;
        }
        List<MetricsInfo> filterData = metricsInfos.stream().filter(t -> "response".equals(t.getType())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(filterData)) {
            return;
        }
        Long pressureExampleId = pressureExampleEntity.getId();
        Long pressureId = pressure.getId();
        long timestamp = filterData.get(0).getTimestamp();
        log.info("Metrics-Upload({}-{}): 接收到的数据:{}条,时间范围:{},延时:{}", pressureId, pressureExampleId,
                metricsInfos.size(), timestamp, (System.currentTimeMillis() - timestamp));
        // 回调数据
        iPressureExampleService.onHeartbeat(pressureExampleId);
        // 写入Clickhouse
        collectorToClickhouse(pressureId, filterData);
    }

    private void collectorToClickhouse(Long pressureId, List<MetricsInfo> metricsList) {
        if (CollUtil.isEmpty(metricsList)) {
            return;
        }
        List<MetricsInfo> metricsInfoList = metricsList.stream().filter(Objects::nonNull).collect(Collectors.toList());
        metricsInfoList.forEach(metrics -> {
            //判断有没有MD5值
            int strPosition = metrics.getTransaction().lastIndexOf(PressureEngineConstants.TRANSACTION_SPLIT_STR);
            if (strPosition > 0) {
                String transaction = metrics.getTransaction();
                metrics.setTransaction(transaction.substring(strPosition + PressureEngineConstants.TRANSACTION_SPLIT_STR.length()));
                metrics.setTestName((transaction.substring(0, strPosition)));
            } else {
                metrics.setTransaction(metrics.getTransaction());
                metrics.setTestName(metrics.getTransaction());
            }
        });
        List<EngineMetrics> engineMetricsList = metricsInfoList.stream().map(metrics -> {
            //处理时间戳-纳秒转成毫秒，防止插入clickhouse报错
            if (Objects.nonNull(metrics.getTimestamp()) && metrics.getTimestamp() > MAX_ACCEPT_TIMESTAMP) {
                metrics.setTime(metrics.getTimestamp() / 1000000);
                metrics.setTimestamp(metrics.getTimestamp() / 1000000);
            }
            EngineMetrics engineMetrics = new EngineMetrics();
            engineMetrics.setTime(metrics.getTimestamp());
            engineMetrics.setTransaction(metrics.getTransaction());
            engineMetrics.setTestName(metrics.getTestName());
            engineMetrics.setCount(metrics.getCount());
            engineMetrics.setFailCount(metrics.getFailCount());
            engineMetrics.setSentBytes(metrics.getSentBytes());
            engineMetrics.setReceivedBytes(metrics.getReceivedBytes());
            engineMetrics.setRt(BigDecimal.valueOf(metrics.getRt()));
            engineMetrics.setSumRt(BigDecimal.valueOf(metrics.getSumRt()));
            engineMetrics.setSaCount(metrics.getSaCount());
            engineMetrics.setMaxRt(BigDecimal.valueOf(metrics.getMaxRt()));
            engineMetrics.setMinRt(BigDecimal.valueOf(metrics.getMinRt()));
            engineMetrics.setTimestamp(metrics.getTimestamp());
            engineMetrics.setActiveThreads(metrics.getActiveThreads());
            engineMetrics.setPercentData(metrics.getPercentData());
            engineMetrics.setPodNo(metrics.getPodNo());
            engineMetrics.setJobId(pressureId + "");
            engineMetrics.setCreateDate(LocalDateTime.now());
            return engineMetrics;
        }).collect(Collectors.toList());

        //构建sql
        EngineMetrics engineMetrics = engineMetricsList.get(0);
        Map<String, Object> engineMap = this.getEngineMetricsMap(engineMetrics);
        String cols = Joiner.on(',').join(engineMap.keySet());
        List<String> params = new ArrayList<>();
        for (String field : engineMap.keySet()) {
            params.add("?");
        }
        String param = Joiner.on(',').join(params);
        Map<String, List<Object[]>> objMap = Maps.newHashMap();
        String tableName = clickHouseShardSupport.isCluster() ? "t_engine_metrics" : "t_engine_metrics_all";
        String sql = "insert into " + tableName + " (" + cols + ") values(" + param + ") ";

        List<Object[]> batchs = Lists.newArrayList();
        //构建批量参数
        engineMetricsList.forEach(metrics -> {
            Map<String, Object> map = this.getEngineMetricsMap(metrics);
            batchs.add(map.values().toArray());
        });
        objMap.put(engineMetrics.getJobId(), batchs);
        log.info("写入clickhouse sql为:{}, 参数为:{}", sql, JsonHelper.bean2Json(objMap));
        clickHouseShardSupport.syncBatchUpdate(sql, objMap);

        try {
            long nowTimeWindow = CollectorUtil.getNowTimeWindow();
            //如果超时10s，重新计算数据所在时间窗的pressure数据
            Map<Long, List<EngineMetrics>> listMap = engineMetricsList.stream()
                    .peek(o -> o.setTime(CollectorUtil.getTimeWindowTime(o.getTime())))
                    .filter(o -> nowTimeWindow - o.getTime() > 10000)
                    .collect(Collectors.groupingBy(EngineMetrics::getTime));
            if (listMap != null && listMap.size() > 0) {
                QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(Report::getJobId, pressureId);
                List<Report> reports = iReportService.list(queryWrapper);
                if (CollectionUtils.isEmpty(reports)) {
                    log.error("在补充数据的时候，没有根据jobId找到对应的报告数据");
                    return;
                }
                Report report = reports.get(0);
                PtConfigExt ptConfig = JsonHelper.json2Bean(report.getPtConfig(), PtConfigExt.class);
                if (null == ptConfig) {
                    log.info("补充数据 report no such ptConfig!sceneId=" + report.getId());
                    return;
                }
                List<ScriptNode> nodes = JsonHelper.json2List(report.getScriptNodeTree(), ScriptNode.class);
                listMap.keySet().forEach(time -> {
                    metricsDataDealTask.reduceMetrics(report, ptConfig.getPodNum(), nowTimeWindow, time, nodes, true);
                });
            }
        } catch (Exception e) {
            log.error("补充数据出现异常", e);
        }

    }

    private Map<String, Object> getEngineMetricsMap(EngineMetrics engineMetrics) {
        Map<String, Object> result = new HashMap<>();
        result.put("time", engineMetrics.getTime());
        result.put("transaction", engineMetrics.getTransaction());
        result.put("test_name", engineMetrics.getTestName());
        result.put("count", engineMetrics.getCount());
        result.put("fail_count", engineMetrics.getFailCount());
        result.put("sent_bytes", engineMetrics.getSentBytes());
        result.put("received_bytes", engineMetrics.getReceivedBytes());
        result.put("rt", engineMetrics.getRt());
        result.put("sum_rt", engineMetrics.getSumRt());
        result.put("sa_count", engineMetrics.getSaCount());
        result.put("max_rt", engineMetrics.getMaxRt());
        result.put("min_rt", engineMetrics.getMinRt());
        result.put("timestamp", engineMetrics.getTimestamp());
        result.put("active_threads", engineMetrics.getActiveThreads());
        result.put("percent_data", engineMetrics.getPercentData());
        result.put("pod_no", engineMetrics.getPodNo());
        result.put("job_id", engineMetrics.getJobId());
        result.put("createDate", engineMetrics.getCreateDate());
        //去掉值为null的数据
        Map<String, Object> copy = new HashMap<>();
        result.forEach((k,v) -> {
            if (v != null) {
                copy.put(k, v);
            }
        });
        return copy;
    }
}
