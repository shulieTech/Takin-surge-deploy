package io.shulie.surge.data.deploy.pradar.starter;

import io.shulie.surge.data.deploy.pradar.common.DataBootstrapEnhancer;
import io.shulie.surge.data.deploy.pradar.config.PradarConfiguration;
import io.shulie.surge.data.deploy.pradar.config.PradarSupplierConfiguration;
import io.shulie.surge.data.runtime.common.DataBootstrap;
import io.shulie.surge.data.runtime.common.DataRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 接收器启动类
 *
 * @author vincent
 * @date 2022/11/15 11:03
 **/
public class PradarSupplierStarter implements PradarTaskStarter {
    private static final Logger logger = LoggerFactory.getLogger(PradarLinkStarter.class);
    private DataBootstrap bootstrap;
    private PradarSupplierConfiguration pradarSupplierConfiguration;
    private DataRuntime dataRuntime;

    /**
     * 初始化
     *
     * @param args
     */
    @Override
    public void init(Map<String, ?> args) {
        try {
            logger.info("PradarkafkaSupplierStarter initial.");
            bootstrap = DataBootstrap.create("deploy.properties", "pradar");
            DataBootstrapEnhancer.enhancer(bootstrap);
            pradarSupplierConfiguration = new PradarSupplierConfiguration();
            pradarSupplierConfiguration.initArgs(args);
            pradarSupplierConfiguration.install(bootstrap);
        } catch (Throwable e) {
            throw new RuntimeException("fail to start PradarkafkaSupplierStarter", e);
        }
    }

    /**
     * 获取配置
     *
     * @return
     */
    @Override
    public PradarConfiguration getPradarConfiguration() {
        return pradarSupplierConfiguration;
    }

    /**
     * 开始运行
     */
    @Override
    public void start() throws Exception {
        try {
            logger.info("PradarkafkaSupplierStarter start");
            dataRuntime = bootstrap.startRuntime();
            pradarSupplierConfiguration.doAfterInit(dataRuntime);
        } catch (Throwable e) {
            throw new RuntimeException("fail to start PradarkafkaSupplierStarter", e);
        }
        logger.info("PradarkafkaSupplierStarter start successfull...");
    }

    /**
     * 停止运行。如果已经停止，则应该不会有任何效果。
     * 建议实现使用同步方式执行。
     */
    @Override
    public void stop() throws Exception {
        pradarSupplierConfiguration.stop();
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
