package io.shulie.takin.kafka.receiver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.shulie.takin.kafka.receiver.dao.amdb.AmdbAppMapper;
import io.shulie.takin.kafka.receiver.entity.AmdbApp;
import io.shulie.takin.kafka.receiver.service.IAmdbAppService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-14
 */
@Service
public class AmdbAppServiceImpl extends ServiceImpl<AmdbAppMapper, AmdbApp> implements IAmdbAppService {

}
