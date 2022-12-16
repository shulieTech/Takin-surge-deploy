package io.shulie.takin.kafka.receiver.service;

import io.shulie.takin.kafka.receiver.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receiver.entity.ApplicationMnt;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 应用管理表 服务类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
public interface IApplicationMntService extends IService<ApplicationMnt> {

    void dealAgentVersionMessage(String appName, String agentVersion, String pradarVersion, TenantCommonExt dealHeader);

    void dealAddApplicationMessage(String toJSONString, TenantCommonExt dealHeader);

    ApplicationMnt getApplicationByTenantIdAndName(String appName, TenantCommonExt dealHeader);
}
