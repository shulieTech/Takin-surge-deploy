package io.shulie.surge.data.suppliers.grpc.remoting.metrics;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Sunsy
 * @date 2022/4/29
 * @apiNode
 * @email sunshiyu@shulie.io
 */
@Data
public class DiskMetricsProtoBean extends AbstractMetricsProtoBean{
    private static final long serialVersionUID = 536593467780277651L;
    /**
     * io wait
     */
    private Double ioWait;

    /**
     * 磁盘总量
     */
    private Long totalDisk;

    /**
     * 磁盘使用
     */
    private Long usedDisk;

    /**
     * 磁盘读字节
     */
    private Long diskReadBytes;

    /**
     * 磁盘写字节
     */
    private Long diskWriteBytes;

    @Override
    public String name() {
        return "Disk";
    }

    @Override
    public List<MetricsProtoBean> export() {

        List<MetricsProtoBean> list = new ArrayList<>();

        MetricsProtoBean ioWaitProtoBean = new MetricsProtoBean();
        ioWaitProtoBean.setTenantCode(this.getTenantCode());
        ioWaitProtoBean.setAppName(this.getAppName());
        ioWaitProtoBean.setHostIp(this.getHostIp());
        ioWaitProtoBean.setAgentId(this.getAgentId());
        ioWaitProtoBean.setMetricType("Gauge");
        ioWaitProtoBean.getLabels().put("cpu","0");
        ioWaitProtoBean.getLabels().put("instance",this.getAgentId());
        ioWaitProtoBean.getLabels().put("hostIp",this.getHostIp());
        ioWaitProtoBean.getLabels().put("job","node");
        ioWaitProtoBean.getLabels().put("mode","iowait");
        ioWaitProtoBean.setMetricsName("node_cpu_seconds_total");
        ioWaitProtoBean.setStartTime(this.getStartTime());
        ioWaitProtoBean.setValue(ioWait);
        list.add(ioWaitProtoBean);


        MetricsProtoBean totalDiskProtoBean = new MetricsProtoBean();
        totalDiskProtoBean.setTenantCode(this.getTenantCode());
        totalDiskProtoBean.setAppName(this.getAppName());
        totalDiskProtoBean.setHostIp(this.getHostIp());
        totalDiskProtoBean.setAgentId(this.getAgentId());
        totalDiskProtoBean.setMetricType("Gauge");
        totalDiskProtoBean.getLabels().put("device","/");
        totalDiskProtoBean.getLabels().put("instance",this.getAgentId());
        totalDiskProtoBean.getLabels().put("hostIp",this.getHostIp());
        totalDiskProtoBean.getLabels().put("job","node");
        totalDiskProtoBean.setMetricsName("node_disk_total");
        totalDiskProtoBean.setStartTime(this.getStartTime());
        totalDiskProtoBean.setProtocol(this.getProtocol());

        totalDiskProtoBean.setValue(Double.valueOf(totalDisk));
        list.add(totalDiskProtoBean);

        MetricsProtoBean usedDiskProtoBean = new MetricsProtoBean();
        usedDiskProtoBean.setTenantCode(this.getTenantCode());
        usedDiskProtoBean.setAppName(this.getAppName());
        usedDiskProtoBean.setHostIp(this.getHostIp());
        usedDiskProtoBean.setAgentId(this.getAgentId());
        usedDiskProtoBean.setMetricType("Gauge");
        usedDiskProtoBean.getLabels().put("device","/");
        usedDiskProtoBean.getLabels().put("instance",this.getAgentId());
        usedDiskProtoBean.getLabels().put("hostIp",this.getHostIp());
        usedDiskProtoBean.getLabels().put("job","node");
        usedDiskProtoBean.setMetricsName("node_disk_used");
        usedDiskProtoBean.setStartTime(this.getStartTime());
        usedDiskProtoBean.setValue(Double.valueOf(usedDisk));
        usedDiskProtoBean.setProtocol(this.getProtocol());

        list.add(usedDiskProtoBean);

        MetricsProtoBean diskReadBytesProtoBean = new MetricsProtoBean();
        diskReadBytesProtoBean.setTenantCode(this.getTenantCode());
        diskReadBytesProtoBean.setAppName(this.getAppName());
        diskReadBytesProtoBean.setHostIp(this.getHostIp());
        diskReadBytesProtoBean.setAgentId(this.getAgentId());
        diskReadBytesProtoBean.setMetricType("Counter");
        diskReadBytesProtoBean.getLabels().put("device","/");
        diskReadBytesProtoBean.getLabels().put("instance",this.getAgentId());
        diskReadBytesProtoBean.getLabels().put("hostIp",this.getHostIp());
        diskReadBytesProtoBean.getLabels().put("job","node");
        diskReadBytesProtoBean.setMetricsName("node_disk_read_bytes_total");
        diskReadBytesProtoBean.setStartTime(this.getStartTime());
        diskReadBytesProtoBean.setValue(Double.valueOf(diskReadBytes));
        diskReadBytesProtoBean.setProtocol(this.getProtocol());

        list.add(diskReadBytesProtoBean);


        MetricsProtoBean diskWriteBytesProtoBean = new MetricsProtoBean();
        diskWriteBytesProtoBean.setTenantCode(this.getTenantCode());
        diskWriteBytesProtoBean.setAppName(this.getAppName());
        diskWriteBytesProtoBean.setHostIp(this.getHostIp());
        diskWriteBytesProtoBean.setAgentId(this.getAgentId());
        diskWriteBytesProtoBean.setMetricType("Counter");
        diskWriteBytesProtoBean.getLabels().put("device","/");
        diskWriteBytesProtoBean.getLabels().put("instance",this.getAgentId());
        diskWriteBytesProtoBean.getLabels().put("hostIp",this.getHostIp());
        diskWriteBytesProtoBean.getLabels().put("job","node");
        diskWriteBytesProtoBean.setMetricsName("node_disk_written_bytes_total");
        diskWriteBytesProtoBean.setStartTime(this.getStartTime());
        diskWriteBytesProtoBean.setValue(Double.valueOf(diskWriteBytes));
        diskWriteBytesProtoBean.setProtocol(this.getProtocol());

        list.add(diskWriteBytesProtoBean);

        return list;
    }
}
