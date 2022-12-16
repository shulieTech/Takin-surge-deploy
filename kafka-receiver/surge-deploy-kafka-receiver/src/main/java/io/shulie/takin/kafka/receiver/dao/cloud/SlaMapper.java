package io.shulie.takin.kafka.receiver.dao.cloud;

import io.shulie.takin.kafka.receiver.entity.Sla;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * Service Level Agreement(服务等级协议) Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@Mapper
public interface SlaMapper extends BaseMapper<Sla> {

}
