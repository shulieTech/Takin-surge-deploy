package io.shulie.takin.kafka.receive.service.impl;

import io.shulie.takin.kafka.receive.entity.PerformanceThreadStackData;
import io.shulie.takin.kafka.receive.dao.web.PerformanceThreadStackDataMapper;
import io.shulie.takin.kafka.receive.service.IPerformanceThreadStackDataService;
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
public class PerformanceThreadStackDataServiceImpl extends ServiceImpl<PerformanceThreadStackDataMapper, PerformanceThreadStackData> implements IPerformanceThreadStackDataService {

}
