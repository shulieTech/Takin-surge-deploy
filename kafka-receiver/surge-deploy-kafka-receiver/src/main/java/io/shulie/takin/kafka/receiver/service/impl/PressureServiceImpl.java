package io.shulie.takin.kafka.receiver.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.shulie.takin.kafka.receiver.constant.cloud.PressureEngineConstants;
import io.shulie.takin.kafka.receiver.dto.cloud.MetricsInfo;
import io.shulie.takin.kafka.receiver.entity.Pressure;
import io.shulie.takin.kafka.receiver.dao.cloud.PressureMapper;
import io.shulie.takin.kafka.receiver.entity.PressureExample;
import io.shulie.takin.kafka.receiver.entity.SlaEvent;
import io.shulie.takin.kafka.receiver.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.shulie.takin.kafka.receiver.util.InfluxUtil;
import io.shulie.takin.kafka.receiver.util.InfluxWriter;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
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
    private ISlaService iSlaService;
    @Resource
    private InfluxWriter influxWriter;

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
        if (CollectionUtils.isEmpty(filterData)){
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
        collectorToInfluxdb(pressureId, metricsInfos);
        // SLA检查
        List<SlaEvent> check = iSlaService.check(pressureId, pressureExampleId, metricsInfos);
        // 进行通知
        iSlaService.event(pressureId, pressureExampleId, check);
    }

    private void collectorToInfluxdb(Long pressureId, List<MetricsInfo> metricsList) {
        if (CollUtil.isEmpty(metricsList)) {
            return;
        }
        String measurement = InfluxUtil.getMetricsMeasurement(pressureId);
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
        metricsInfoList.stream().map(metrics -> {
                    //处理时间戳-纳秒转成毫秒，防止插入influxdb报错
                    if (Objects.nonNull(metrics.getTime()) && metrics.getTime() > InfluxUtil.MAX_ACCEPT_TIMESTAMP) {
                        metrics.setTimestamp(metrics.getTimestamp() / 1000000);
                    }
                    if (Objects.nonNull(metrics.getTimestamp()) && metrics.getTimestamp() > InfluxUtil.MAX_ACCEPT_TIMESTAMP) {
                        metrics.setTimestamp(metrics.getTimestamp() / 1000000);
                    }
                    return InfluxUtil.toPoint(measurement, metrics.getTimestamp(), metrics);
                })
                .forEach(influxWriter::insert);
    }
}
