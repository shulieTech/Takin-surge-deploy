package io.shulie.surge.data.deploy.pradar.starter;

import io.shulie.surge.data.common.lifecycle.Lifecycle;
import io.shulie.surge.data.deploy.pradar.config.PradarConfiguration;

import java.util.Map;

/**
 * 任务启动类
 *
 * @author vincent
 * @date 2022/11/15 10:41
 **/
public interface PradarTaskStarter extends Lifecycle {

    /**
     * 初始化
     *
     * @param args
     */
    void init(Map<String, String> args);


    /**
     * 获取配置
     *
     * @return
     */
    PradarConfiguration getPradarConfiguration();
}
