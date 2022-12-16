package io.shulie.takin.kafka.receiver.dao.cloud;

import io.shulie.takin.kafka.receiver.entity.Pressure;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 任务 Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@Mapper
public interface PressureMapper extends BaseMapper<Pressure> {

}
