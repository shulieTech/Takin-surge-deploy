package io.shulie.takin.kafka.receive.service.impl;

import io.shulie.takin.kafka.receive.entity.SceneManage;
import io.shulie.takin.kafka.receive.dao.web.SceneManageMapper;
import io.shulie.takin.kafka.receive.service.ISceneManageService;
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
public class SceneManageServiceImpl extends ServiceImpl<SceneManageMapper, SceneManage> implements ISceneManageService {

}
