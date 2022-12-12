package io.shulie.takin.kafka.receive.service;

import io.shulie.takin.kafka.receive.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receive.entity.UploadInterfaceData;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * dubbo和job接口上传收集表 服务类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
public interface IUploadInterfaceDataService extends IService<UploadInterfaceData> {

    void dealMessage(String toJSONString, TenantCommonExt dealHeader);
}
