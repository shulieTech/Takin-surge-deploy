package io.shulie.takin.kafka.receive.service.impl;

import io.shulie.takin.kafka.receive.entity.ApplicationPluginsConfig;
import io.shulie.takin.kafka.receive.dao.web.ApplicationPluginsConfigMapper;
import io.shulie.takin.kafka.receive.service.IApplicationPluginsConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Service
public class ApplicationPluginsConfigServiceImpl extends ServiceImpl<ApplicationPluginsConfigMapper, ApplicationPluginsConfig> implements IApplicationPluginsConfigService {

}
