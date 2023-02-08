package io.shulie.takin.kafka.receiver.dao.web;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.shulie.takin.kafka.receiver.entity.ApplicationPluginUpgrade;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 应用升级单 Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@Mapper
public interface ApplicationPluginUpgradeMapper extends BaseMapper<ApplicationPluginUpgrade> {

}
