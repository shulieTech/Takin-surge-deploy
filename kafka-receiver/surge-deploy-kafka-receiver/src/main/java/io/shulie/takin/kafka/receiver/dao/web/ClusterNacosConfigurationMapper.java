package io.shulie.takin.kafka.receiver.dao.web;

import io.shulie.takin.kafka.receiver.entity.ClusterNacosConfiguration;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 集群nacos配合 Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2023-03-30
 */
@Mapper
public interface ClusterNacosConfigurationMapper extends BaseMapper<ClusterNacosConfiguration> {

}
