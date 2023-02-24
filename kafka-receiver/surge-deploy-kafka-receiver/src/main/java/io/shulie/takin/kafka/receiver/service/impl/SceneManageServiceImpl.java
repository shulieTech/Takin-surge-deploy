package io.shulie.takin.kafka.receiver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.shulie.takin.kafka.receiver.dao.web.SceneManageMapper;
import io.shulie.takin.kafka.receiver.entity.SceneManage;
import io.shulie.takin.kafka.receiver.service.ISceneManageService;
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
public class SceneManageServiceImpl extends ServiceImpl<SceneManageMapper, SceneManage> implements ISceneManageService {

}
