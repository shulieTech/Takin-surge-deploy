package io.shulie.takin.kafka.receiver.dao.web;

import io.shulie.takin.kafka.receiver.entity.AppAgentConfigReport;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * agent配置上报详情 Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Mapper
public interface AppAgentConfigReportMapper extends BaseMapper<AppAgentConfigReport> {

}
