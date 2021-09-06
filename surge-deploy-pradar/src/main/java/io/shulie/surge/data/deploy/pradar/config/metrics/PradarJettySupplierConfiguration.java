/*
package io.shulie.surge.data.deploy.pradar.config.metrics;

import com.google.common.collect.Maps;
import com.pamirs.pradar.log.parser.DataType;
import io.shulie.surge.data.JettySupplier;
import io.shulie.surge.data.JettySupplierModule;
import io.shulie.surge.data.JettySupplierSpec;
import io.shulie.surge.data.deploy.pradar.config.PradarModule;
import io.shulie.surge.data.deploy.pradar.config.PradarProcessor;
import io.shulie.surge.data.deploy.pradar.config.PradarProcessorConfigSpec;
import io.shulie.surge.data.deploy.pradar.digester.MetricsDigester;
import io.shulie.surge.data.runtime.common.DataBootstrap;
import io.shulie.surge.data.runtime.common.DataRuntime;
import io.shulie.surge.data.runtime.digest.DataDigester;
import io.shulie.surge.data.runtime.processor.DataQueue;
import io.shulie.surge.data.runtime.processor.ProcessorConfigSpec;
import io.shulie.surge.data.sink.influxdb.InfluxDBModule;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Map;

*/
/**
 * @Author: xingchen
 * @ClassName: PradarJettySupplierConfiguration
 * @Package: io.shulie.surge.data
 * @Date: 2020/11/1710:11
 * @Description:
 *//*

public class PradarJettySupplierConfiguration {
    private static final String contentPath = "/pradar-open-web/pradar/receiveFluxInfo.do";

    */
/**
     * 任务启动
     * java -Xms2g -Xmx2g -cp deploy-pradar-1.0.jar io.shulie.surge.data.deploy.pradar.config.metrics.PradarJettySupplierConfiguration
     *
     * @param args
     *//*

    public static void main(String[] args) {
        PradarJettySupplierConfiguration conf = new PradarJettySupplierConfiguration();
        conf.init();
    }

    public void init() {
        try {
            DataBootstrap bootstrap = DataBootstrap.create("deploy.properties");
            bootstrap.install(new PradarModule(), new JettySupplierModule(), new InfluxDBModule());
            DataRuntime dataRuntime = bootstrap.startRuntime();
            buildJettySupplier(dataRuntime).start();
        } catch (Throwable e) {
            System.err.println(ExceptionUtils.getStackTrace(e));
        }
    }

    public JettySupplier buildJettySupplier(DataRuntime runtime) throws Exception {
        JettySupplier jettySupplier = runtime.createGenericInstance(new JettySupplierSpec());

        ProcessorConfigSpec<PradarProcessor> metricsProcessorConfigSpec = new PradarProcessorConfigSpec();
        MetricsDigester metricsDigester = runtime.getInstance(MetricsDigester.class);
        metricsProcessorConfigSpec.setDigesters(new DataDigester[]{metricsDigester});
        PradarProcessor jettyProcessor = runtime.createGenericInstance(metricsProcessorConfigSpec);

        Map<String, DataQueue> queueMap = Maps.newHashMap();
        queueMap.put(String.valueOf(DataType.METRICS_LOG), jettyProcessor);
        jettySupplier.setQueue(queueMap);
        jettySupplier.addServlet(contentPath, new MetricsServletHolder(jettyProcessor));
        return jettySupplier;
    }
}
*/
