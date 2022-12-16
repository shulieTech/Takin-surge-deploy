package io.shulie.takin.kafka.receiver.dao.cloud;

import io.shulie.takin.kafka.receiver.entity.Callback;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 回调表 Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@Mapper
public interface CallbackMapper extends BaseMapper<Callback> {

}
