package io.shulie.surge.data.suppliers.grpc.remoting.metrics;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * metrics proto bean
 *
 * @author vincent
 * @date 2022/07/02 22:27
 **/
@Data
public class MetricsProtoBean extends AbstractMetricsProtoBean {
    private static final long serialVersionUID = -6702063777908380528L;
    private String metricsName;
    private String description;
    /**
     * Counter（计数器）、Gauge（仪表盘）、Histogram（直方图）、Summary（摘要）
     */
    private String metricType = "Sum";
    private Map<String, String> labels = new TreeMap<String, String>();
    private Double value;

    @Override
    public List<MetricsProtoBean> export() {
        List<MetricsProtoBean> list = new ArrayList<>();
        list.add(this);
        return list;
    }
}
