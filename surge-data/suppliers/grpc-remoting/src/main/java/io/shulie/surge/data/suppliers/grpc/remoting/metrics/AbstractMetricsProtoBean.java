package io.shulie.surge.data.suppliers.grpc.remoting.metrics;

import io.shulie.surge.data.suppliers.grpc.remoting.ProtoBean;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * metrics 协议的bean
 *
 * @author vincent
 * @date 2022/04/26 20:47
 **/
@Getter
@Setter
@ToString
public abstract class AbstractMetricsProtoBean implements ProtoBean {
    private static final long serialVersionUID = 3539146472227440672L;
    private String appName;
    private String tenantCode;
    private String hostIp;
    private String agentId;
    private String protocol;


    /**
     * 是否容器标识
     */
    private long isContainerFlag;

    private LocalDateTime startTime;

    public AbstractMetricsProtoBean() {
    }

    public AbstractMetricsProtoBean(String appName, String tenantCode, String hostIp, String agentId,
                                    long isContainerFlag, LocalDateTime startTime) {
        this.appName = appName;
        this.tenantCode = tenantCode;
        this.hostIp = hostIp;
        this.agentId = agentId;
        this.isContainerFlag = isContainerFlag;
        this.startTime = startTime;
    }

    @Override
    public LocalDateTime startTime() {
        return startTime;
    }

    /**
     * metrics 名称
     *
     * @return
     */
    public String name() {
        return null;
    }

    public abstract List<MetricsProtoBean> export();

}
