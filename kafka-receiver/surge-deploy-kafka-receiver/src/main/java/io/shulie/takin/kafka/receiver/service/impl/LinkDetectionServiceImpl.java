package io.shulie.takin.kafka.receiver.service.impl;

import io.shulie.takin.kafka.receiver.entity.LinkDetection;
import io.shulie.takin.kafka.receiver.dao.web.LinkDetectionMapper;
import io.shulie.takin.kafka.receiver.service.ILinkDetectionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 链路检测表 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Service
public class LinkDetectionServiceImpl extends ServiceImpl<LinkDetectionMapper, LinkDetection> implements ILinkDetectionService {

}
