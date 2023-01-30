package io.shulie.takin.kafka.receiver.service.impl;

import io.shulie.takin.kafka.receiver.entity.EnginePressureAll;
import io.shulie.takin.kafka.receiver.dao.clickhouse.EnginePressureAllMapper;
import io.shulie.takin.kafka.receiver.service.IEnginePressureAllService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * t_engine_pressure_all 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2023-01-12
 */
@Service
public class EnginePressureAllServiceImpl extends ServiceImpl<EnginePressureAllMapper, EnginePressureAll> implements IEnginePressureAllService {

}
