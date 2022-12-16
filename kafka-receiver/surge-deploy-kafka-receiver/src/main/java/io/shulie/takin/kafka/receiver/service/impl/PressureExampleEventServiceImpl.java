package io.shulie.takin.kafka.receiver.service.impl;

import io.shulie.takin.kafka.receiver.entity.PressureExampleEvent;
import io.shulie.takin.kafka.receiver.dao.cloud.PressureExampleEventMapper;
import io.shulie.takin.kafka.receiver.service.IPressureExampleEventService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 任务实例事件 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@Service
public class PressureExampleEventServiceImpl extends ServiceImpl<PressureExampleEventMapper, PressureExampleEvent> implements IPressureExampleEventService {

}
