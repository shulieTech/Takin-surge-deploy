package io.shulie.surge.data.suppliers.grpc.remoting.node;

import java.io.Serializable;
import java.util.List;

/**
 * @Description
 * @Author ocean_wll
 * @Date 2022/11/1 10:17
 */
public class NodeInfo implements Serializable {
    private final static long serialVersionUID = 1L;

    /**
     * 基础信息
     */
    private ResourceInfo resource;
    /**
     * 内存信息
     */
    private MemoryInfo memory;

    /**
     * cpu信息
     */
    private CpuInfo cpu;

    /**
     * 磁盘信息
     */
    private DiskInfo disk;

    /**
     * 线程信息
     */
    private ThreadInfo thread;

    /**
     * jvm信息
     */
    private JVMInfo jvmInfo;

    /**
     * gc信息
     */
    private GCInfo gcInfo;

    /**
     * 网络延迟
     */
    private List<NetworkDelayInfo> networkDelayInfos;


    /**
     * 采集时间
     */
    private Long collectionTime;

    public List<NetworkDelayInfo> getNetworkDelayInfos() {
        return networkDelayInfos;
    }

    public void setNetworkDelayInfos(List<NetworkDelayInfo> networkDelayInfos) {
        this.networkDelayInfos = networkDelayInfos;
    }

    public ResourceInfo getResource() {
        return resource;
    }

    public void setResource(ResourceInfo resource) {
        this.resource = resource;
    }

    public CpuInfo getCpu() {
        return cpu;
    }

    public MemoryInfo getMemory() {
        return memory;
    }

    public void setMemory(MemoryInfo memory) {
        this.memory = memory;
    }

    public void setCpu(CpuInfo cpu) {
        this.cpu = cpu;
    }

    public DiskInfo getDisk() {
        return disk;
    }

    public void setDisk(DiskInfo disk) {
        this.disk = disk;
    }

    public ThreadInfo getThread() {
        return thread;
    }

    public void setThread(ThreadInfo thread) {
        this.thread = thread;
    }

    public JVMInfo getJvmInfo() {
        return jvmInfo;
    }

    public void setJvmInfo(JVMInfo jvmInfo) {
        this.jvmInfo = jvmInfo;
    }

    public GCInfo getGcInfo() {
        return gcInfo;
    }

    public void setGcInfo(GCInfo gcInfo) {
        this.gcInfo = gcInfo;
    }

    public Long getCollectionTime() {
        return collectionTime;
    }

    public void setCollectionTime(Long collectionTime) {
        this.collectionTime = collectionTime;
    }

    public static NodeInfo copy(NodeInfo nodeInfo) {
        NodeInfo newNodeInfo = new NodeInfo();
        newNodeInfo.setCpu(nodeInfo.getCpu());
        newNodeInfo.setResource(nodeInfo.getResource());
        newNodeInfo.setDisk(nodeInfo.getDisk());
        newNodeInfo.setThread(nodeInfo.getThread());
        newNodeInfo.setJvmInfo(nodeInfo.getJvmInfo());
        newNodeInfo.setGcInfo(nodeInfo.getGcInfo());
        newNodeInfo.setCollectionTime(nodeInfo.getCollectionTime());
        return newNodeInfo;
    }

    @Override
    public String toString() {
        return "NodeInfo{" + "resource=" + resource + ", cpu=" + cpu + ", disk=" + disk + ", thread=" + thread + ", jvmInfo=" + jvmInfo + ", gcInfo=" + gcInfo + ", collectionTime=" + collectionTime + '}';
    }
}
