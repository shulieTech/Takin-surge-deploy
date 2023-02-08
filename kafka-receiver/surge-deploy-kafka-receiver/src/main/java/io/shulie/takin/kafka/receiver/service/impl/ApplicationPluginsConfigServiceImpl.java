package io.shulie.takin.kafka.receiver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.shulie.takin.kafka.receiver.dao.web.ApplicationPluginsConfigMapper;
import io.shulie.takin.kafka.receiver.entity.ApplicationPluginsConfig;
import io.shulie.takin.kafka.receiver.service.IApplicationPluginsConfigService;
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