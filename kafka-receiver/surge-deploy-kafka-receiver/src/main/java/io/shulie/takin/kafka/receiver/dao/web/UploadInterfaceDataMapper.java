package io.shulie.takin.kafka.receiver.dao.web;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.shulie.takin.kafka.receiver.entity.UploadInterfaceData;
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
