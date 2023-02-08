package io.shulie.takin.kafka.receiver.dao.web;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.shulie.takin.kafka.receiver.entity.ApplicationMnt;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 应用管理表 Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Mapper
public interface ApplicationMntMapper extends BaseMapper<ApplicationMnt> {

}
