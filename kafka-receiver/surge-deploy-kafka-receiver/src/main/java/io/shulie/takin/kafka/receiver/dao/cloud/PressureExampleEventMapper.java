package io.shulie.takin.kafka.receiver.dao.cloud;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.shulie.takin.kafka.receiver.entity.PressureExampleEvent;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 任务实例事件 Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@Mapper
public interface PressureExampleEventMapper extends BaseMapper<PressureExampleEvent> {

}
