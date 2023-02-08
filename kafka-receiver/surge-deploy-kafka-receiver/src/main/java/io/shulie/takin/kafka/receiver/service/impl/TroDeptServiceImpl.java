package io.shulie.takin.kafka.receiver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.shulie.takin.kafka.receiver.dao.web.TroDeptMapper;
import io.shulie.takin.kafka.receiver.entity.TroDept;
import io.shulie.takin.kafka.receiver.service.ITroDeptService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 部门表 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-09
 */
@Service
public class TroDeptServiceImpl extends ServiceImpl<TroDeptMapper, TroDept> implements ITroDeptService {

}
