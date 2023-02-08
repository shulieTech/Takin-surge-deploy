package io.shulie.takin.kafka.receiver.dao.web;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.shulie.takin.kafka.receiver.entity.ShadowJobConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 影子JOB任务配置 Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Mapper
public interface ShadowJobConfigMapper extends BaseMapper<ShadowJobConfig> {

}
