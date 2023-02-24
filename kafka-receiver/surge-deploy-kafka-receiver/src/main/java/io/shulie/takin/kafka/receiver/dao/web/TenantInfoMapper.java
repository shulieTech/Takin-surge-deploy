package io.shulie.takin.kafka.receiver.dao.web;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.shulie.takin.kafka.receiver.entity.TenantInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-09
 */
@Mapper
public interface TenantInfoMapper extends BaseMapper<TenantInfo> {

}
