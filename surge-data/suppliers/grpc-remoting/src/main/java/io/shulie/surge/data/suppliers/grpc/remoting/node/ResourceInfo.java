package io.shulie.surge.data.suppliers.grpc.remoting.node;

import java.io.Serializable;

/**
 * @Description
 * @Author ocean_wll
 * @Date 2022/11/1 10:23
 */
public class ResourceInfo implements Serializable {
    private final static long serialVersionUID = 1L;

    private String host;

    private String agentId;

    private String tenantCode;

    private String env;

    private String ip;

    private String appName;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    @Override
    public String toString() {
        return "ResourceInfo{" + "host='" + host + '\'' + ", agentId='" + agentId + '\'' + ", tenantCode='" + tenantCode + '\'' + ", env='" + env
                + '\'' + ", ip='" + ip + '\'' + ", appName='" + appName + '\'' + '}';
    }
}
