package io.shulie.takin.kafka.receiver.service.impl;

import io.shulie.takin.kafka.receiver.entity.EngineMetricsAll;
import io.shulie.takin.kafka.receiver.dao.clickhouse.EngineMetricsAllMapper;
import io.shulie.takin.kafka.receiver.service.IEngineMetricsAllService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * t_engine_metrics_all 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2023-01-12
 */
@Service
public class EngineMetricsAllServiceImpl extends ServiceImpl<EngineMetricsAllMapper, EngineMetricsAll> implements IEngineMetricsAllService {

}
