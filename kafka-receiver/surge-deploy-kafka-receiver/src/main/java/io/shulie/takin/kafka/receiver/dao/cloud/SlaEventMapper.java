package io.shulie.takin.kafka.receiver.dao.cloud;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.shulie.takin.kafka.receiver.entity.SlaEvent;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * sla触发记录 Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@Mapper
public interface SlaEventMapper extends BaseMapper<SlaEvent> {

}
