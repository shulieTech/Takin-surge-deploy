package io.shulie.surge.data.suppliers.grpc.remoting.node;

import java.io.Serializable;

/**
 * @author by: hezhongqi
 * @Package io.shulie.agent.attach.plugin.module
 * @ClassName: AddressBean
 * @Description: TODO
 * @Date: 2023/3/9 16:02
 */
public class NetworkDelayInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String remoteHost;

    private Integer port;

    private Long cost;

    /**
     * 如果请求地址无法访问,则认为是无效地址，succes则false
     */
    private Boolean success;

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Long getCost() {
        return cost;
    }

    public void setCost(Long cost) {
        this.cost = cost;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
