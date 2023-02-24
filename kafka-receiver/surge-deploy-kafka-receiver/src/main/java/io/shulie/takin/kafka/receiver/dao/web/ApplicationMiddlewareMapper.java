package io.shulie.takin.kafka.receiver.dao.web;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.shulie.takin.kafka.receiver.entity.ApplicationMiddleware;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 应用中间件 Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Mapper
public interface ApplicationMiddlewareMapper extends BaseMapper<ApplicationMiddleware> {

}
