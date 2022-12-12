package io.shulie.takin.kafka.receive.service.impl;

import io.shulie.takin.kafka.receive.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receive.entity.ShadowJobConfig;
import io.shulie.takin.kafka.receive.dao.web.ShadowJobConfigMapper;
import io.shulie.takin.kafka.receive.service.IShadowJobConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 影子JOB任务配置 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Service
public class ShadowJobConfigServiceImpl extends ServiceImpl<ShadowJobConfigMapper, ShadowJobConfig> implements IShadowJobConfigService {

    @Override
    public void dealMessage(String body, TenantCommonExt dealHeader) {

    }
}
