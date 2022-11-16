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

    public int getPid() {
        return pid;
    }

    public String getIp() {
        return ip;
    }

    public String getHost() {
        return host;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }
}
