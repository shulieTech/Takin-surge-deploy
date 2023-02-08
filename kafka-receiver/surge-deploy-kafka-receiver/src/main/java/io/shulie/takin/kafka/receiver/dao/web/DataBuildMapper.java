package io.shulie.takin.kafka.receiver.dao.web;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.shulie.takin.kafka.receiver.entity.DataBuild;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 压测数据构建表 Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Mapper
public interface DataBuildMapper extends BaseMapper<DataBuild> {

}
