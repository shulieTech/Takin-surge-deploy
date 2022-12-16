package io.shulie.takin.kafka.receiver.service.impl;

import io.shulie.takin.kafka.receiver.entity.SlaEvent;
import io.shulie.takin.kafka.receiver.dao.cloud.SlaEventMapper;
import io.shulie.takin.kafka.receiver.service.ISlaEventService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * sla触发记录 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@Service
public class SlaEventServiceImpl extends ServiceImpl<SlaEventMapper, SlaEvent> implements ISlaEventService {

}
