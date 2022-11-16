package io.shulie.surge.data.deploy.pradar;

import io.shulie.surge.data.deploy.pradar.common.DataBootstrapEnhancer;
import io.shulie.surge.data.deploy.pradar.starter.PradarSupplierStarter;
import io.shulie.surge.data.runtime.common.DataBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 接收器启动类
 *
 * @author vincent
 * @date 2022/11/15 11:03
 **/
public class PradarKafkaSupplierStarter extends PradarSupplierStarter {
    private static final Logger logger = LoggerFactory.getLogger(PradarKafkaSupplierStarter.class);

    /**
     * 初始化
     *
     * @param args
     */
    @Override
    public void init(Map<String, ?> args) {
        try {
            logger.info("PradarSupplierStarter initial.");
            bootstrap = DataBootstrap.create("deploy.properties", "pradar");
            DataBootstrapEnhancer.enhancer(bootstrap);
            pradarSupplierConfiguration = new PradaKafkaSupplierConfiguration();
            pradarSupplierConfiguration.initArgs(args);
            pradarSupplierConfiguration.install(bootstrap);
        } catch (Throwable e) {
            throw new RuntimeException("fail to start PradarSupplierStarter", e);
        }
    }
}
