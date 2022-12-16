package io.shulie.takin.kafka.receiver.service.impl;

import io.shulie.takin.kafka.receiver.entity.DataBuild;
import io.shulie.takin.kafka.receiver.dao.web.DataBuildMapper;
import io.shulie.takin.kafka.receiver.service.IDataBuildService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 压测数据构建表 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Service
public class DataBuildServiceImpl extends ServiceImpl<DataBuildMapper, DataBuild> implements IDataBuildService {

}
