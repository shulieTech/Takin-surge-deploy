package io.shulie.surge.data.deploy.pradar;

import io.shulie.surge.data.deploy.pradar.common.DataBootstrapEnhancer;
import io.shulie.surge.data.deploy.pradar.starter.PradarAggregationStarter;
import io.shulie.surge.data.runtime.common.DataBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 聚合器启动类
 *
 * @author vincent
 * @date 2022/11/15 10:43
 **/
public class PradarKafkaAggregationStarter extends PradarAggregationStarter {

    private static final Logger logger = LoggerFactory.getLogger(PradarKafkaAggregationStarter.class);

    /**
     * 初始化
     *
     * @param args
     */
    @Override
    public void init(Map<String, String> args) {
        try {
            args.put("receivers", "trace");
            logger.info("PradarKafkaAggregationStarter initial.");
            bootstrap = DataBootstrap.create("deploy.properties", "pradar");
            DataBootstrapEnhancer.enhancer(bootstrap);
            pradarAggregationConfiguration = new PradarKafkaAggregationConfiguration();
            pradarAggregationConfiguration.initArgs(args);
            pradarAggregationConfiguration.install(bootstrap);
        } catch (Throwable e) {
            throw new RuntimeException("fail to start PradarKafkaAggregationStarter", e);
        }
    }
}
