package io.shulie.surge.data.suppliers.grpc.remoting.node;

import java.io.Serializable;
import java.util.List;

/**
 * @Description 磁盘信息
 * @Author ocean_wll
 * @Date 2022/11/1 10:33
 */
public class DiskInfo implements Serializable {
    private final static long serialVersionUID = 1L;

    /**
     * 磁盘信息 [{ 文件系统目录, 挂载点, 总空间, // 单位KB 已使用, // 单位KB 使用率 }]
     */
    private List<DiskItemInfo> diskInfo;

    public List<DiskItemInfo> getDiskInfo() {
        return diskInfo;
    }

    public void setDiskInfo(List<DiskItemInfo> diskInfo) {
        this.diskInfo = diskInfo;
    }

    @Override
    public String toString() {
        return "DiskInfo{" +
                "diskInfo=" + diskInfo +
                '}';
    }
}
