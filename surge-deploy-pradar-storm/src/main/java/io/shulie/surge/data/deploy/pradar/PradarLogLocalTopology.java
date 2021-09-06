package io.shulie.surge.data.deploy.pradar;

import com.google.common.collect.Maps;
import io.shulie.surge.data.deploy.pradar.common.ParamUtil;
import io.shulie.surge.data.deploy.pradar.common.StormConfig;
import io.shulie.surge.deploy.pradar.common.CommonStat;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用于本地ClassLoader加载启动本地storm服务
 */
public class PradarLogLocalTopology {
    public static void main(String[] args) throws Exception {
        LocalCluster cluster = new LocalCluster();
        Map<String, String> inputMap = Maps.newHashMap();
        ParamUtil.parseInputParam(inputMap, args);
        Integer workers = Integer.valueOf(inputMap.get(ParamUtil.WORKERS));
        Config config = StormConfig.createConfig(workers);
        config.putAll(inputMap);
        config.put(ParamUtil.REGISTERZK, CommonStat.FALSE);
        TopologyBuilder topologyBuilder =
                PradarLogTopology.createLogBuilder(Integer.valueOf(workers), false);

        cluster.submitTopology(PradarLogTopology.class.getSimpleName(), config, topologyBuilder.createTopology());
        TimeUnit.MINUTES.sleep(60);
    }
}
