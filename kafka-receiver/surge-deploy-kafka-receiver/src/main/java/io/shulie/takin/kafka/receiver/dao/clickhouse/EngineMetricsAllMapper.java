package io.shulie.takin.kafka.receiver.dao.clickhouse;

import io.shulie.takin.kafka.receiver.entity.EngineMetricsAll;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * t_engine_metrics_all Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2023-01-12
 */
@Mapper
public interface EngineMetricsAllMapper extends BaseMapper<EngineMetricsAll> {

}
