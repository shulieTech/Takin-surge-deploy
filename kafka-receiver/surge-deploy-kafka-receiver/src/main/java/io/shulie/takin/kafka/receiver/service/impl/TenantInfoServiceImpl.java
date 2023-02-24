package io.shulie.takin.kafka.receiver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.shulie.takin.kafka.receiver.dao.web.TenantInfoMapper;
import io.shulie.takin.kafka.receiver.entity.TenantInfo;
import io.shulie.takin.kafka.receiver.service.ITenantInfoService;
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
