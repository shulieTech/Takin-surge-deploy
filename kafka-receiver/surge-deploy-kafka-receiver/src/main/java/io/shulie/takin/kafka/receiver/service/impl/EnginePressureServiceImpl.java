package io.shulie.takin.kafka.receiver.service.impl;

import io.shulie.takin.kafka.receiver.entity.EnginePressure;
import io.shulie.takin.kafka.receiver.dao.clickhouse.EnginePressureMapper;
import io.shulie.takin.kafka.receiver.service.IEnginePressureService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * t_engine_pressure 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2023-01-12
 */
@Service
public class EnginePressureServiceImpl extends ServiceImpl<EnginePressureMapper, EnginePressure> implements IEnginePressureService {

}
