package io.shulie.takin.kafka.receiver.service.impl;

import io.shulie.takin.kafka.receiver.entity.ResourceExample;
import io.shulie.takin.kafka.receiver.dao.cloud.ResourceExampleMapper;
import io.shulie.takin.kafka.receiver.service.IResourceExampleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 资源实例表 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@Service
public class ResourceExampleServiceImpl extends ServiceImpl<ResourceExampleMapper, ResourceExample> implements IResourceExampleService {

}
