package io.shulie.takin.kafka.receiver.dao.amdb;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.shulie.takin.kafka.receiver.entity.AmdbAppInstanceStatus;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 应用实例探针状态表 Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-14
 */
@Mapper
public interface AmdbAppInstanceStatusMapper extends BaseMapper<AmdbAppInstanceStatus> {

}
