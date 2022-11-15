package io.shulie.surge.data.deploy.pradar;

import io.shulie.surge.data.common.lifecycle.Lifecycle;

import java.util.Map;

/**
 * @author vincent
 * @date 2022/11/15 10:41
 **/
public interface PradarTaskStarter extends Lifecycle {

    /**
     * 初始化
     *
     * @param args
     */
    void init(Map<String, ?> args);
}
