package io.shulie.takin.kafka.receiver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.shulie.takin.kafka.receiver.entity.ConfigServer;
import io.shulie.takin.kafka.receiver.dao.web.ConfigServerMapper;
import io.shulie.takin.kafka.receiver.service.IConfigServerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 配置表-服务的配置 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@Service
public class ConfigServerServiceImpl extends ServiceImpl<ConfigServerMapper, ConfigServer> implements IConfigServerService {

    @Override
    public ConfigServer queryByKey(String key) {
        QueryWrapper<ConfigServer> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ConfigServer::getKey, key);
        queryWrapper.lambda().eq(ConfigServer::getIsDeleted, 0);
        List<ConfigServer> list = this.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(list)){
            return list.get(0);
        }
        return null;
    }
}
