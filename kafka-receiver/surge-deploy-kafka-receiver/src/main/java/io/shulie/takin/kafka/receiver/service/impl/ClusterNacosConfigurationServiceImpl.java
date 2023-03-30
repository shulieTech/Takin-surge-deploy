package io.shulie.takin.kafka.receiver.service.impl;

import io.shulie.takin.kafka.receiver.entity.ClusterNacosConfiguration;
import io.shulie.takin.kafka.receiver.dao.web.ClusterNacosConfigurationMapper;
import io.shulie.takin.kafka.receiver.service.IClusterNacosConfigurationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 集群nacos配合 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2023-03-30
 */
@Service
public class ClusterNacosConfigurationServiceImpl extends ServiceImpl<ClusterNacosConfigurationMapper, ClusterNacosConfiguration> implements IClusterNacosConfigurationService {

}
