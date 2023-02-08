package io.shulie.takin.kafka.receiver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.shulie.takin.kafka.receiver.dao.web.ApplicationPluginUpgradeMapper;
import io.shulie.takin.kafka.receiver.entity.ApplicationPluginUpgrade;
import io.shulie.takin.kafka.receiver.service.IApplicationPluginUpgradeService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 应用升级单 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@Service
public class ApplicationPluginUpgradeServiceImpl extends ServiceImpl<ApplicationPluginUpgradeMapper, ApplicationPluginUpgrade> implements IApplicationPluginUpgradeService {

}
