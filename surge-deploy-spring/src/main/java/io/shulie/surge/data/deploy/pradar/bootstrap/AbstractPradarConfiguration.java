package io.shulie.surge.data.deploy.pradar.bootstrap;

import io.shulie.surge.data.deploy.pradar.collector.OutputCollector;

import java.util.Map;

/**
 * 抽象配置实现
 * @author vincent
 * @date 2022/11/14 18:07
 **/
public abstract class AbstractPradarConfiguration implements PradarConfiguration{


    /**
     * 初始化
     *
     * @param args
     */
    @Override
    public void initArgs(Map<String, Object> args) {

    }

    /**
     * 设置收集器
     *
     * @param outputCollector
     */
    @Override
    public void collector(OutputCollector outputCollector) {

    }
}
