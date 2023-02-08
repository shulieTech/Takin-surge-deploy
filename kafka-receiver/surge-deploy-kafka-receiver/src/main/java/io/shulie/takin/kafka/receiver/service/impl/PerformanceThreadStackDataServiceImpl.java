package io.shulie.takin.kafka.receiver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.shulie.takin.kafka.receiver.dao.web.PerformanceThreadStackDataMapper;
import io.shulie.takin.kafka.receiver.entity.PerformanceThreadStackData;
import io.shulie.takin.kafka.receiver.service.IPerformanceThreadStackDataService;
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
public class PerformanceThreadStackDataServiceImpl extends ServiceImpl<PerformanceThreadStackDataMapper, PerformanceThreadStackData> implements IPerformanceThreadStackDataService {

}
