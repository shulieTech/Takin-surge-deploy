package io.shulie.takin.kafka.receiver.dao.web;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.shulie.takin.kafka.receiver.entity.AgentReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 探针心跳数据 Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Mapper
public interface AgentReportMapper extends BaseMapper<AgentReport> {

    Integer insertOrUpdate(AgentReport agentReport);
}
