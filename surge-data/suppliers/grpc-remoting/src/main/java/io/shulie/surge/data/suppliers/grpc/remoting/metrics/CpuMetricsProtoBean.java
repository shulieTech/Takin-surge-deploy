package io.shulie.surge.data.suppliers.grpc.remoting.metrics;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * cpu 指标
 * @author Sunsy
 * @date 2022/4/29
 * @apiNode
 * @email sunshiyu@shulie.io
 */
@Data
public class CpuMetricsProtoBean extends AbstractMetricsProtoBean {

    private static final long serialVersionUID = -1782815936710627769L;
    /**
     * cpu使用率
     */
    private Double cpuUsage;

    /**
     * cpu 1分钟负载
     */
    private Double cpuLoad1;

    /**
     * cpu 5分钟负载
     */
    private Double cpuLoad5;

    /**
     * cpu 15分钟负载
     */
    private Double cpuLoad15;

    /**
     * cpu核数
     */
    private Integer cpuNum;


    @Override
    public String name() {
        return "Cpu";
    }

    @Override
    public List<MetricsProtoBean> export() {
        List<MetricsProtoBean> list = new ArrayList<>();
        MetricsProtoBean cpuUsageProtoBean = new MetricsProtoBean();
        cpuUsageProtoBean.setTenantCode(this.getTenantCode());
        cpuUsageProtoBean.setAppName(this.getAppName());
        cpuUsageProtoBean.setHostIp(this.getHostIp());
        cpuUsageProtoBean.setAgentId(this.getAgentId());
        cpuUsageProtoBean.setMetricType("Gauge");
        cpuUsageProtoBean.getLabels().put("instance",this.getAgentId());
        cpuUsageProtoBean.getLabels().put("hostIp",this.getHostIp());
        cpuUsageProtoBean.getLabels().put("job","node");
        cpuUsageProtoBean.getLabels().put("cpu","0");
        cpuUsageProtoBean.setMetricsName("node_cpu_usage");
        cpuUsageProtoBean.setStartTime(this.getStartTime());
        cpuUsageProtoBean.setProtocol(this.getProtocol());
        cpuUsageProtoBean.setValue(cpuUsage);
        list.add(cpuUsageProtoBean);

        MetricsProtoBean cpuLoad1ProtoBean = new MetricsProtoBean();
        cpuLoad1ProtoBean.setTenantCode(this.getTenantCode());
        cpuLoad1ProtoBean.setAppName(this.getAppName());
        cpuLoad1ProtoBean.setHostIp(this.getHostIp());
        cpuLoad1ProtoBean.setAgentId(this.getAgentId());
        cpuLoad1ProtoBean.setMetricType("Gauge");
        cpuLoad1ProtoBean.getLabels().put("instance",this.getAgentId());
        cpuLoad1ProtoBean.getLabels().put("hostIp",this.getHostIp());
        cpuLoad1ProtoBean.getLabels().put("job","node");
        cpuLoad1ProtoBean.getLabels().put("cpu","0");
        cpuLoad1ProtoBean.setStartTime(this.getStartTime());
        cpuLoad1ProtoBean.setMetricsName("node_cpu_load1");
        cpuLoad1ProtoBean.setProtocol(this.getProtocol());
        cpuLoad1ProtoBean.setValue(cpuLoad1);
        list.add(cpuLoad1ProtoBean);

        MetricsProtoBean cpuLoad5ProtoBean = new MetricsProtoBean();
        cpuLoad5ProtoBean.setTenantCode(this.getTenantCode());
        cpuLoad5ProtoBean.setAppName(this.getAppName());
        cpuLoad5ProtoBean.setHostIp(this.getHostIp());
        cpuLoad5ProtoBean.setAgentId(this.getAgentId());
        cpuLoad5ProtoBean.setMetricType("Gauge");
        cpuLoad5ProtoBean.getLabels().put("instance",this.getAgentId());
        cpuLoad5ProtoBean.getLabels().put("hostIp",this.getHostIp());
        cpuLoad5ProtoBean.getLabels().put("job","node");
        cpuLoad5ProtoBean.getLabels().put("cpu","0");
        cpuLoad5ProtoBean.setMetricsName("node_cpu_load5");
        cpuLoad5ProtoBean.setStartTime(this.getStartTime());
        cpuLoad5ProtoBean.setProtocol(this.getProtocol());
        cpuLoad5ProtoBean.setValue(cpuLoad5);
        list.add(cpuLoad5ProtoBean);

        MetricsProtoBean cpuLoad15ProtoBean = new MetricsProtoBean();
        cpuLoad15ProtoBean.setTenantCode(this.getTenantCode());
        cpuLoad15ProtoBean.setAppName(this.getAppName());
        cpuLoad15ProtoBean.setHostIp(this.getHostIp());
        cpuLoad15ProtoBean.setAgentId(this.getAgentId());
        cpuLoad15ProtoBean.setMetricType("Gauge");
        cpuLoad15ProtoBean.getLabels().put("instance",this.getAgentId());
        cpuLoad15ProtoBean.getLabels().put("hostIp",this.getHostIp());
        cpuLoad15ProtoBean.getLabels().put("job","node");
        cpuLoad15ProtoBean.getLabels().put("cpu","0");
        cpuLoad15ProtoBean.setMetricsName("node_cpu_load15");
        cpuLoad15ProtoBean.setStartTime(this.getStartTime());
        cpuLoad15ProtoBean.setProtocol(this.getProtocol());

        cpuLoad15ProtoBean.setValue(cpuLoad15);
        list.add(cpuLoad15ProtoBean);

        MetricsProtoBean cpuNumProtoBean = new MetricsProtoBean();
        cpuNumProtoBean.setTenantCode(this.getTenantCode());
        cpuNumProtoBean.setAppName(this.getAppName());
        cpuNumProtoBean.setHostIp(this.getHostIp());
        cpuNumProtoBean.setAgentId(this.getAgentId());
        cpuNumProtoBean.setMetricType("Gauge");
        cpuNumProtoBean.getLabels().put("instance",this.getAgentId());
        cpuNumProtoBean.getLabels().put("hostIp",this.getHostIp());
        cpuNumProtoBean.getLabels().put("job","node");
        cpuNumProtoBean.getLabels().put("cpu","0");
        cpuNumProtoBean.setMetricsName("node_cpu_num");
        cpuNumProtoBean.setStartTime(this.getStartTime());
        cpuNumProtoBean.setValue(Double.valueOf(cpuNum));
        cpuNumProtoBean.setProtocol(this.getProtocol());
        list.add(cpuNumProtoBean);

        return list;
    }
}
