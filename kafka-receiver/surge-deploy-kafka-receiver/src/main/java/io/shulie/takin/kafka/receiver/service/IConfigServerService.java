package io.shulie.takin.kafka.receiver.service;

import io.shulie.takin.kafka.receiver.entity.ConfigServer;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 配置表-服务的配置 服务类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
public interface IConfigServerService extends IService<ConfigServer> {

    ConfigServer queryByKey(String key);

}
