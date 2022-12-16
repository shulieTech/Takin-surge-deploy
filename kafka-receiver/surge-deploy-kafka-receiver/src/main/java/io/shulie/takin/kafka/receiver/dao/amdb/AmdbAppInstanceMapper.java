package io.shulie.takin.kafka.receiver.dao.amdb;

import io.shulie.takin.kafka.receiver.entity.AmdbAppInstance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-14
 */
@Mapper
public interface AmdbAppInstanceMapper extends BaseMapper<AmdbAppInstance> {

}
