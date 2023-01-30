package io.shulie.takin.kafka.receiver.dao.clickhouse;

import io.shulie.takin.kafka.receiver.entity.EngineMetrics;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * t_engine_metrics Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2023-01-05
 */
@Mapper
public interface EngineMetricsMapper extends BaseMapper<EngineMetrics> {

}
