package io.shulie.takin.kafka.receiver.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.shulie.takin.kafka.receiver.constant.cloud.PressureEngineConstants;
import io.shulie.takin.kafka.receiver.dao.cloud.PressureMapper;
import io.shulie.takin.kafka.receiver.dto.cloud.MetricsInfo;
import io.shulie.takin.kafka.receiver.dto.web.PtConfigExt;
import io.shulie.takin.kafka.receiver.dto.web.ScriptNode;
import io.shulie.takin.kafka.receiver.entity.EngineMetrics;
import io.shulie.takin.kafka.receiver.entity.Pressure;
import io.shulie.takin.kafka.receiver.entity.PressureExample;
import io.shulie.takin.kafka.receiver.entity.Report;
import io.shulie.takin.kafka.receiver.service.IEngineMetricsService;
import io.shulie.takin.kafka.receiver.service.IPressureExampleService;
import io.shulie.takin.kafka.receiver.service.IPressureService;
import io.shulie.takin.kafka.receiver.service.IReportService;
import io.shulie.takin.kafka.receiver.task.MetricsDataDealTask;
import io.shulie.takin.kafka.receiver.util.CollectorUtil;
import io.shulie.takin.utils.json.JsonHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private IEngineMetricsService iEngineMetricsService;
    @Resource
    private MetricsDataDealTask metricsDataDealTask;
    @Resource
    private IReportService iReportService;
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
        long timestamp = metricsInfos.get(0).getTimestamp();
        log.debug("Metrics-Upload({}-{}):接受到的数据:{}", pressureId, pressureExampleId, metricsInfos);
        log.info("Metrics-Upload({}-{}): 接收到的数据:{}条,时间范围:{},延时:{}", pressureId, pressureExampleId,
                metricsInfos.size(), timestamp, (System.currentTimeMillis() - timestamp));
        // 回调数据
        iPressureExampleService.onHeartbeat(pressureExampleId);
        // 写入InfluxDB
        collectorToClickhouse(pressureId, metricsInfos);
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
            //处理时间戳-纳秒转成毫秒，防止插入influxdb报错
            if (Objects.nonNull(metrics.getTime()) && metrics.getTime() > MAX_ACCEPT_TIMESTAMP) {
                metrics.setTimestamp(metrics.getTimestamp() / 1000000);
            }
            if (Objects.nonNull(metrics.getTimestamp()) && metrics.getTimestamp() > MAX_ACCEPT_TIMESTAMP) {
                metrics.setTimestamp(metrics.getTimestamp() / 1000000);
            }
            EngineMetrics engineMetrics = new EngineMetrics();
            engineMetrics.setTime(metrics.getTime());
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
        iEngineMetricsService.saveBatch(engineMetricsList);

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
}
