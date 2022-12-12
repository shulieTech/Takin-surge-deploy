package io.shulie.takin.kafka.receive.service.impl;

import io.shulie.takin.kafka.receive.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receive.entity.ApplicationApiManage;
import io.shulie.takin.kafka.receive.dao.web.ApplicationApiManageMapper;
import io.shulie.takin.kafka.receive.service.IApplicationApiManageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Service
public class ApplicationApiManageServiceImpl extends ServiceImpl<ApplicationApiManageMapper, ApplicationApiManage> implements IApplicationApiManageService {

    @Override
    public void dealMessage(String toJSONString, TenantCommonExt dealHeader) {

    }
}
