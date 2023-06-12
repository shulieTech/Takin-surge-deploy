package io.shulie.surge.data.suppliers.grpc.remoting.metrics;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


/**
 * 内存指标协议类
 *
 * @author Sunsy
 * @date 2022/4/29
 * @apiNode
 * @email sunshiyu@shulie.io
 */
@Data
public class MemoryMetricsProtoBean extends AbstractMetricsProtoBean {
    private static final long serialVersionUID = -5193218659505293478L;
    /**
     * 内存使用率
     */
    private Double memoryUsage;

    /**
     * 总内存
     */
    private Long totalMemory;

    /**
     * 可用内存
     */
    private Long availableMemory;

    @Override
    public String name() {
        return "Memory";
    }

    @Override
    public List<MetricsProtoBean> export() {

        List<MetricsProtoBean> list = new ArrayList<>();

        MetricsProtoBean memUsageProtoBean = new MetricsProtoBean();
        memUsageProtoBean.setTenantCode(this.getTenantCode());
        memUsageProtoBean.setAppName(this.getAppName());
        memUsageProtoBean.setHostIp(this.getHostIp());
        memUsageProtoBean.setAgentId(this.getAgentId());
        memUsageProtoBean.setMetricType("Gauge");
        memUsageProtoBean.getLabels().put("instance",this.getAgentId());
        memUsageProtoBean.getLabels().put("hostIp",this.getHostIp());
        memUsageProtoBean.getLabels().put("job","node");
        memUsageProtoBean.getLabels().put("mode","iowait");
        memUsageProtoBean.setMetricsName("node_memory_MemUsage");
        memUsageProtoBean.setValue(getMemoryUsage());
        memUsageProtoBean.setStartTime(this.getStartTime());
        memUsageProtoBean.setProtocol(this.getProtocol());

        list.add(memUsageProtoBean);

        MetricsProtoBean totalMemoryProtoBean = new MetricsProtoBean();
        totalMemoryProtoBean.setTenantCode(this.getTenantCode());
        totalMemoryProtoBean.setAppName(this.getAppName());
        totalMemoryProtoBean.setHostIp(this.getHostIp());
        totalMemoryProtoBean.setAgentId(this.getAgentId());
        totalMemoryProtoBean.setMetricType("Gauge");
        totalMemoryProtoBean.getLabels().put("instance",this.getAgentId());
        totalMemoryProtoBean.getLabels().put("hostIp",this.getHostIp());
        totalMemoryProtoBean.getLabels().put("job","node");
        totalMemoryProtoBean.setMetricsName("node_memory_total");
        totalMemoryProtoBean.setValue(Double.valueOf(getTotalMemory()));
        totalMemoryProtoBean.setStartTime(this.getStartTime());
        totalMemoryProtoBean.setProtocol(this.getProtocol());

        list.add(totalMemoryProtoBean);


        MetricsProtoBean availableMemoryProtoBean = new MetricsProtoBean();
        availableMemoryProtoBean.setTenantCode(this.getTenantCode());
        availableMemoryProtoBean.setAppName(this.getAppName());
        availableMemoryProtoBean.setHostIp(this.getHostIp());
        availableMemoryProtoBean.setAgentId(this.getAgentId());
        availableMemoryProtoBean.setMetricType("Gauge");
        availableMemoryProtoBean.getLabels().put("instance",this.getAgentId());
        availableMemoryProtoBean.getLabels().put("hostIp",this.getHostIp());
        availableMemoryProtoBean.getLabels().put("job","node");
        availableMemoryProtoBean.setMetricsName("node_memory_avaliable");
        availableMemoryProtoBean.setValue(Double.valueOf(getAvailableMemory()));
        availableMemoryProtoBean.setStartTime(this.getStartTime());
        availableMemoryProtoBean.setProtocol(this.getProtocol());

        list.add(availableMemoryProtoBean);

        return list;
    }
}
