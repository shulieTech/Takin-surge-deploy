package io.shulie.surge.data.deploy.pradar.link;

import io.shulie.surge.data.common.utils.CommonUtils;
import io.shulie.surge.data.common.utils.IpAddressUtils;

import java.io.Serializable;
import java.util.UUID;

/**
 * 任务节点
 *
 * @author vincent
 * @date 2022/11/16 14:40
 **/
public class TaskNode implements Serializable {
    private int id;

    private String uuid = UUID.randomUUID().toString();

    private int pid = CommonUtils.getPid();

    private String host = IpAddressUtils.getLocalHostName();

    private String ip = IpAddressUtils.getLocalAddress();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "TaskNode{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", pid=" + pid +
                ", host='" + host + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }
}
