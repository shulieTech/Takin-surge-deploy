package io.shulie.surge.data.deploy.pradar.starter;

import io.shulie.surge.data.deploy.pradar.common.DataBootstrapEnhancer;
import io.shulie.surge.data.deploy.pradar.config.PradarConfiguration;
import io.shulie.surge.data.deploy.pradar.config.PradarLinkConfiguration;
import io.shulie.surge.data.runtime.common.DataBootstrap;
import io.shulie.surge.data.runtime.common.DataRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 接收器启动类
 *
 * @author vincent
 * @date 2022/11/15 10:43
 **/
public class PradarLinkStarter implements PradarTaskStarter {

    private static final Logger logger = LoggerFactory.getLogger(PradarLinkStarter.class);
    private DataBootstrap bootstrap;
    private PradarLinkConfiguration pradarLinkConfiguration;
    private DataRuntime dataRuntime;

    /**
     * 初始化
     *
     * @param args
     */
    @Override
    public void init(Map<String, ?> args) {
        try {
            logger.info("PradarLinkStarter initial.");
            bootstrap = DataBootstrap.create("deploy.properties", "pradar");
            DataBootstrapEnhancer.enhancer(bootstrap);
            pradarLinkConfiguration = new PradarLinkConfiguration();
            pradarLinkConfiguration.initArgs(args);
            pradarLinkConfiguration.install(bootstrap);
        } catch (Throwable e) {
            throw new RuntimeException("fail to start PradarKafkaLinkStarter", e);
        }
    }

    /**
     * 获取配置
     *
     * @return
     */
    @Override
    public PradarConfiguration getPradarConfiguration() {
        return pradarLinkConfiguration;
    }

    /**
     * 开始运行
     */
    @Override
    public void start() throws Exception {
        try {
            logger.info("PradarLinkStarter start");
            dataRuntime = bootstrap.startRuntime();
            pradarLinkConfiguration.doAfterInit(dataRuntime);
        } catch (Throwable e) {
            throw new RuntimeException("fail to start PradarLinkSpout", e);
        }
        logger.info("PradarLinkStarter start successfull...");
    }

    /**
     * 停止运行。如果已经停止，则应该不会有任何效果。
     * 建议实现使用同步方式执行。
     */
    @Override
    public void stop() throws Exception {
        pradarLinkConfiguration.stop();
        dataRuntime.shutdown();
    }

    /**
     * 检查当前是否在运行状态
     */
    @Override
    public boolean isRunning() {
        return true;
    }

}
