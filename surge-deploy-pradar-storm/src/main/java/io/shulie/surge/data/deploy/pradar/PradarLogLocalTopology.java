package io.shulie.surge.data.deploy.pradar;

import com.google.common.collect.Maps;
import io.shulie.surge.data.deploy.pradar.common.ParamUtil;
import io.shulie.surge.data.deploy.pradar.common.PradarRtConstant;
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
    public static void main1(String[] args) throws Exception {
//        args = new String[1];
//        args[0] = "-DHostName={\"sunshiyudeMacBook-Pro.local\":\"127.0.0.1\"}";
        LocalCluster cluster = new LocalCluster();
        Map<String, String> inputMap = Maps.newHashMap();
        ParamUtil.parseInputParam(inputMap, args);
        Integer workers = Integer.valueOf(inputMap.get(ParamUtil.WORKERS));
        Config config = StormConfig.createConfig(workers);
        config.putAll(inputMap);
        config.put(ParamUtil.REGISTERZK, CommonStat.TRUE);
        TopologyBuilder topologyBuilder =
                PradarLogTopology.createLogBuilder(Integer.valueOf(workers), false);

        cluster.submitTopology(PradarLogTopology.class.getSimpleName(), config, topologyBuilder.createTopology());
        TimeUnit.MINUTES.sleep(60);
    }

    public static void main(String[] args) throws Exception {
        LocalCluster cluster = new LocalCluster();
        Map<String, String> inputMap = Maps.newHashMap();
        ParamUtil.parseInputParam(inputMap, args);
        Integer workers = Integer.valueOf(inputMap.get(ParamUtil.WORKERS));
        Boolean generalVersion = Boolean.valueOf(inputMap.get(ParamUtil.GENERAL_VERSION));
        Config config = StormConfig.createConfig(workers);
        config.put(ParamUtil.REGISTERZK, CommonStat.TRUE);
        config.putAll(inputMap);
        TopologyBuilder topologyBuilder = createLogBuilder(workers, generalVersion);

        cluster.submitTopology(PradarLogTopology.class.getSimpleName(), config, topologyBuilder.createTopology());

        TimeUnit.MINUTES.sleep(60);
    }

    public static TopologyBuilder createLogBuilder(int workers, Boolean isGeneralVersion) {
        TopologyBuilder builder = new TopologyBuilder();
        // reduce数为spout的一半
        Double reduceCount = Math.ceil(Double.valueOf(workers) / 2);
        builder.setSpout(PradarLogSpout.class.getSimpleName(), new PradarLogSpout(), workers);
        if (!isGeneralVersion) {
            // 使用trace记录metrics,通用版本里面暂时不计算流量,可通过配置打开
            builder.setBolt(PradarTraceReduceBolt.class.getSimpleName(), new PradarTraceReduceBolt(), reduceCount.intValue())
                    .directGrouping(PradarLogSpout.class.getSimpleName(), PradarRtConstant.REDUCE_TRACE_METRICS_STREAM_ID);
        }
        // E2E巡检指标计算
        builder.setBolt(E2ETraceReduceBolt.class.getSimpleName(), new E2ETraceReduceBolt(), reduceCount.intValue())
                .directGrouping(PradarLogSpout.class.getSimpleName(), PradarRtConstant.REDUCE_E2E_TRACE_METRICS_STREAM_ID);
        return builder;
    }
}
