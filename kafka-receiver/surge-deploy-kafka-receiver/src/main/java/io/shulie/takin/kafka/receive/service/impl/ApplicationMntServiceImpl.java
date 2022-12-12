package io.shulie.takin.kafka.receive.service.impl;

import io.shulie.takin.kafka.receive.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receive.entity.ApplicationMnt;
import io.shulie.takin.kafka.receive.dao.web.ApplicationMntMapper;
import io.shulie.takin.kafka.receive.service.IApplicationMntService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 应用管理表 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Service
public class ApplicationMntServiceImpl extends ServiceImpl<ApplicationMntMapper, ApplicationMnt> implements IApplicationMntService {

    @Override
    public void dealAgentVersionMessage(String appName, String agentVersion, String pradarVersion, TenantCommonExt dealHeader) {

    }

    @Override
    public void dealAddApplicationMessage(String toJSONString, TenantCommonExt dealHeader) {

    }
}
