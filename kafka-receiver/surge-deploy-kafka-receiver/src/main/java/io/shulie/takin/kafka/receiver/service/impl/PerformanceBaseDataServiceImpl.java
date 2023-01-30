package io.shulie.takin.kafka.receiver.service.impl;

import io.shulie.takin.kafka.receiver.entity.PerformanceBaseData;
import io.shulie.takin.kafka.receiver.dao.clickhouse.PerformanceBaseDataMapper;
import io.shulie.takin.kafka.receiver.service.IPerformanceBaseDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * t_performance_base_data 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2023-01-06
 */
@Service
public class PerformanceBaseDataServiceImpl extends ServiceImpl<PerformanceBaseDataMapper, PerformanceBaseData> implements IPerformanceBaseDataService {

}
