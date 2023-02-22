package io.shulie.takin.kafka.receiver.dao.web;

import io.shulie.takin.kafka.receiver.entity.ApplicationDsWarn;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2023-02-15
 */
@Mapper
public interface ApplicationDsWarnMapper extends BaseMapper<ApplicationDsWarn> {

    void insertOrUpdate(ApplicationDsWarn applicationDsWarn);
}
