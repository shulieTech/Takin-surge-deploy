package io.shulie.takin.kafka.receive.service;

import io.shulie.takin.kafka.receive.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receive.entity.ApplicationApiManage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
public interface IApplicationApiManageService extends IService<ApplicationApiManage> {

    void dealMessage(String toJSONString, TenantCommonExt dealHeader);
}
