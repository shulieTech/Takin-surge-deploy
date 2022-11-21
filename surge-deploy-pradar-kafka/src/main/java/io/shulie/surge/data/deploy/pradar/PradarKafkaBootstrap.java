package io.shulie.surge.data.deploy.pradar;

import com.google.common.collect.Maps;
import io.shulie.surge.data.deploy.pradar.common.ParameterTool;
import io.shulie.surge.data.deploy.pradar.starter.PradarLinkStarter;

import java.util.Map;

/**
 * kafka 启动类
 *
 * @author vincent
 * @date 2022/11/15 10:41
 **/
public class PradarKafkaBootstrap {

    public static void main(String[] args) throws Exception {
        Map<String, String> conf = ParameterTool.fromArgs(args).getConfiguration();

        if (PradarKafkaSwitcher.SUPPLIER_SWITCHER) {
            //接收器启动
            PradarKafkaSupplierStarter pradarkafkaSupplierStarter = new PradarKafkaSupplierStarter();
            pradarkafkaSupplierStarter.init(Maps.newHashMap(conf));
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

        }

        if (PradarKafkaSwitcher.AGGREGATION_SWITCHER) {

            //聚合器启动
            PradarKafkaAggregationStarter pradarKafkaAggregationStarter = new PradarKafkaAggregationStarter();
            pradarKafkaAggregationStarter.init(Maps.newHashMap(conf));
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
        }

        if (PradarKafkaSwitcher.LINK_TASK_SWITCHER) {
            //link分析启动
            PradarLinkStarter pradarKafkaLinkStarter = new PradarLinkStarter();
            pradarKafkaLinkStarter.init(Maps.newHashMap(conf));
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
}
