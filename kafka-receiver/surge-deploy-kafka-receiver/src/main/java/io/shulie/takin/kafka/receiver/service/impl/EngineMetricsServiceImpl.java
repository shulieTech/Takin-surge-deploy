package io.shulie.takin.kafka.receiver.service.impl;

import io.shulie.takin.kafka.receiver.entity.EngineMetrics;
import io.shulie.takin.kafka.receiver.dao.clickhouse.EngineMetricsMapper;
import io.shulie.takin.kafka.receiver.service.IEngineMetricsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * t_engine_metrics 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2023-01-05
 */
@Service
public class EngineMetricsServiceImpl extends ServiceImpl<EngineMetricsMapper, EngineMetrics> implements IEngineMetricsService {

}
