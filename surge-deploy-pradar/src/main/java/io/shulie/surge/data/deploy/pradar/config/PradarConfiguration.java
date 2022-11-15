package io.shulie.surge.data.deploy.pradar.config;

import io.shulie.surge.data.common.lifecycle.Stoppable;
import io.shulie.surge.data.deploy.pradar.collector.OutputCollector;
import io.shulie.surge.data.runtime.common.DataBootstrap;
import io.shulie.surge.data.runtime.common.DataRuntime;

import java.io.Serializable;
import java.util.Map;

/**
 * @author vincent
 * @date 2022/11/14 17:21
 **/
public interface PradarConfiguration extends Stoppable, Serializable {


    /**
     * 初始化
     *
     * @param args
     */
    public void initArgs(Map<String, ?> args);


    /**
     * 装载module
     *
     * @param bootstrap
     */
    void install(DataBootstrap bootstrap);


    /**
     * 运行时启动后初始化
     */
    void doAfterInit(DataRuntime dataRuntime) throws Exception;


    /**
     * 设置收集器
     */
    void collector(OutputCollector outputCollector);
}
