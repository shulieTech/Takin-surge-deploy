package io.shulie.takin.kafka.receive.service.impl;

import io.shulie.takin.kafka.receive.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receive.entity.UploadInterfaceData;
import io.shulie.takin.kafka.receive.dao.web.UploadInterfaceDataMapper;
import io.shulie.takin.kafka.receive.service.IUploadInterfaceDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * dubbo和job接口上传收集表 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Service
public class UploadInterfaceDataServiceImpl extends ServiceImpl<UploadInterfaceDataMapper, UploadInterfaceData> implements IUploadInterfaceDataService {

    @Override
    public void dealMessage(String toJSONString, TenantCommonExt dealHeader) {

    }
}
