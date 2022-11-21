package io.shulie.surge.data.deploy.pradar;

import io.shulie.surge.data.deploy.pradar.common.DataBootstrapEnhancer;
import io.shulie.surge.data.deploy.pradar.starter.PradarLinkStarter;
import io.shulie.surge.data.runtime.common.DataBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 接收器启动类
 *
 * @author vincent
 * @date 2022/11/15 10:43
 **/
public class PradarKafkaLinkStarter extends PradarLinkStarter {

    private static final Logger logger = LoggerFactory.getLogger(PradarLinkStarter.class);

    /**
     * 初始化
     *
     * @param args
     */
    @Override
    public void init(Map<String, String> args) {
        try {
            logger.info("PradarLinkStarter initial.");
            String propertiesFile = args.getOrDefault("properties", "deploy.properties");
            bootstrap = DataBootstrap.create(propertiesFile, "pradar");
            DataBootstrapEnhancer.enhancer(bootstrap);
            pradarLinkConfiguration = new PradarKafkaLinkConfiguration();
            pradarLinkConfiguration.initArgs(args);
            pradarLinkConfiguration.install(bootstrap);
        } catch (Throwable e) {
            throw new RuntimeException("fail to start PradarKafkaLinkStarter", e);
        }
    }
}
