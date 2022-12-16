package io.shulie.takin.kafka.receiver.service.impl;

import io.shulie.takin.kafka.receiver.entity.AmdbAgentConfig;
import io.shulie.takin.kafka.receiver.dao.amdb.AmdbAgentConfigMapper;
import io.shulie.takin.kafka.receiver.service.IAmdbAgentConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * agent动态配置表 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-14
 */
@Service
public class AmdbAgentConfigServiceImpl extends ServiceImpl<AmdbAgentConfigMapper, AmdbAgentConfig> implements IAmdbAgentConfigService {

}
