package io.shulie.takin.kafka.receiver.dao.clickhouse;

import io.shulie.takin.kafka.receiver.entity.PerformanceBaseData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * t_performance_base_data Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2023-01-06
 */
@Mapper
public interface PerformanceBaseDataMapper extends BaseMapper<PerformanceBaseData> {

}
