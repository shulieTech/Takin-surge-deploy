package io.shulie.takin.kafka.receive.dao.web;

import io.shulie.takin.kafka.receive.entity.UploadInterfaceData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * dubbo和job接口上传收集表 Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Mapper
public interface UploadInterfaceDataMapper extends BaseMapper<UploadInterfaceData> {

}
