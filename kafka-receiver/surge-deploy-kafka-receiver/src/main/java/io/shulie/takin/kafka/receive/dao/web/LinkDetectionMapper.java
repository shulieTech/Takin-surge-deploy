package io.shulie.takin.kafka.receive.dao.web;

import io.shulie.takin.kafka.receive.entity.LinkDetection;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 链路检测表 Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Mapper
public interface LinkDetectionMapper extends BaseMapper<LinkDetection> {

}
