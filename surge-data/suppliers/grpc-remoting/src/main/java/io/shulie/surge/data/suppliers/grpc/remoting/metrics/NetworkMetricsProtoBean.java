package io.shulie.surge.data.suppliers.grpc.remoting.metrics;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 网络指标协议类
 * @author Sunsy
 * @date 2022/4/29
 * @apiNode
 * @email sunshiyu@shulie.io
 */
@Data
public class NetworkMetricsProtoBean extends AbstractMetricsProtoBean {

    private static final long serialVersionUID = 142709472774129103L;
    /**
     * 网卡使用率
     */
    private Double networkUsage;

    /**
     * 网卡速率
     */
    private Double networkSpeed;

    @Override
    public String name() {
        return "Network";
    }

    @Override
    public List<MetricsProtoBean> export() {
        List<MetricsProtoBean> list = new ArrayList<>();

        MetricsProtoBean networkUsageProtoBean = new MetricsProtoBean();
        networkUsageProtoBean.setTenantCode(this.getTenantCode());
        networkUsageProtoBean.setAppName(this.getAppName());
        networkUsageProtoBean.setHostIp(this.getHostIp());
        networkUsageProtoBean.setAgentId(this.getAgentId());
        networkUsageProtoBean.setMetricType("Gauge");
        networkUsageProtoBean.getLabels().put("instance",this.getAgentId());
        networkUsageProtoBean.getLabels().put("hostIp",this.getHostIp());
        networkUsageProtoBean.getLabels().put("job","node");
        networkUsageProtoBean.getLabels().put("device","");
        networkUsageProtoBean.setMetricsName("node_network_usage");
        networkUsageProtoBean.setValue(getNetworkUsage());
        networkUsageProtoBean.setStartTime(this.getStartTime());
        networkUsageProtoBean.setProtocol(this.getProtocol());

        list.add(networkUsageProtoBean);

        MetricsProtoBean networkSpeedProtoBean = new MetricsProtoBean();
        networkSpeedProtoBean.setTenantCode(this.getTenantCode());
        networkSpeedProtoBean.setAppName(this.getAppName());
        networkSpeedProtoBean.setHostIp(this.getHostIp());
        networkSpeedProtoBean.setAgentId(this.getAgentId());
        networkSpeedProtoBean.setMetricType("Gauge");
        networkSpeedProtoBean.getLabels().put("instance",this.getAgentId());
        networkSpeedProtoBean.getLabels().put("hostIp",this.getHostIp());
        networkSpeedProtoBean.getLabels().put("job","node");
        networkSpeedProtoBean.getLabels().put("mode","iowait");
        networkSpeedProtoBean.setMetricsName("node_network_speed_bytes");
        networkSpeedProtoBean.setStartTime(this.getStartTime());
        networkSpeedProtoBean.setProtocol(this.getProtocol());

        networkSpeedProtoBean.setValue(getNetworkSpeed());
        list.add(networkSpeedProtoBean);

        return list;
    }


}
