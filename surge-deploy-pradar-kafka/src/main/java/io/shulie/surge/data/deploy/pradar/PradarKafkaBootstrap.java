package io.shulie.surge.data.deploy.pradar;

import io.shulie.surge.data.deploy.pradar.common.ParameterTool;
import io.shulie.surge.data.deploy.pradar.starter.PradarLinkStarter;

import java.util.Map;

/**
 * @author vincent
 * @date 2022/11/15 10:41
 **/
public class PradarKafkaBootstrap {

    public static void main(String[] args) throws Exception {
        Map<String, String> conf = ParameterTool.fromArgs(args).getConfiguration();

        //接收器启动
        PradarkafkaSupplierStarter pradarkafkaSupplierStarter = new PradarkafkaSupplierStarter();
        pradarkafkaSupplierStarter.init(conf);
        pradarkafkaSupplierStarter.start();


        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    pradarkafkaSupplierStarter.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));

        //聚合器启动
        PradarKafkaAggregationStarter pradarKafkaAggregationStarter = new PradarKafkaAggregationStarter();
        pradarKafkaAggregationStarter.init(conf);
        pradarKafkaAggregationStarter.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    pradarKafkaAggregationStarter.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));

        //link分析启动
        PradarLinkStarter pradarKafkaLinkStarter = new PradarLinkStarter();
        pradarKafkaLinkStarter.init(conf);
        pradarKafkaLinkStarter.start();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    pradarKafkaLinkStarter.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }
}
