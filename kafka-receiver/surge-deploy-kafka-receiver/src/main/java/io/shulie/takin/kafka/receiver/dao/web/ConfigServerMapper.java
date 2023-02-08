package io.shulie.takin.kafka.receiver.dao.web;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.shulie.takin.kafka.receiver.entity.ConfigServer;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 配置表-服务的配置 Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@Mapper
public interface ConfigServerMapper extends BaseMapper<ConfigServer> {

}
