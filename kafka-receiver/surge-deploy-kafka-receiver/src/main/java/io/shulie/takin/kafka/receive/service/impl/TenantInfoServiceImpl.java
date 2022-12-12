package io.shulie.takin.kafka.receive.service.impl;

import io.shulie.takin.kafka.receive.entity.TenantInfo;
import io.shulie.takin.kafka.receive.dao.web.TenantInfoMapper;
import io.shulie.takin.kafka.receive.service.ITenantInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-09
 */
@Service
public class TenantInfoServiceImpl extends ServiceImpl<TenantInfoMapper, TenantInfo> implements ITenantInfoService {

}
