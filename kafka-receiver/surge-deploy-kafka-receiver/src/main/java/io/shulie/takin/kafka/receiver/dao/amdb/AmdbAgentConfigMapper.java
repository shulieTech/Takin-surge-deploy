package io.shulie.takin.kafka.receiver.dao.amdb;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.shulie.takin.kafka.receiver.entity.AmdbAgentConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * agent动态配置表 Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-14
 */
@Mapper
public interface AmdbAgentConfigMapper extends BaseMapper<AmdbAgentConfig> {

}
