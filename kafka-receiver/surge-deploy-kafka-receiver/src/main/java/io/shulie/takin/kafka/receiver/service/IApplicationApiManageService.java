package io.shulie.takin.kafka.receiver.service;

import io.shulie.takin.kafka.receiver.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receiver.entity.ApplicationApiManage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
public interface IApplicationApiManageService extends IService<ApplicationApiManage> {

    void dealMessage(Map<String, List<String>> register, TenantCommonExt dealHeader);
}
