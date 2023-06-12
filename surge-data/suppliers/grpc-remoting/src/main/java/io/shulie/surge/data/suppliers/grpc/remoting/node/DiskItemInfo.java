package io.shulie.surge.data.suppliers.grpc.remoting.node;

import java.io.Serializable;

/**
 * @Description
 * @Author ocean_wll
 * @Date 2022/11/1 10:36
 */
public class DiskItemInfo implements Serializable {
    private final static long serialVersionUID = 1L;

    /**
     * 文件系统
     */
    private String fileSystem;

    /**
     * 挂载点
     */
    private String mounted;

    /**
     * 总空间 KB
     */
    private Long totalSize;

    /**
     * 已使用 KB
     */
    private Long usedSize;

    public String getFileSystem() {
        return fileSystem;
    }

    public void setFileSystem(String fileSystem) {
        this.fileSystem = fileSystem;
    }

    public String getMounted() {
        return mounted;
    }

    public void setMounted(String mounted) {
        this.mounted = mounted;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public Long getUsedSize() {
        return usedSize;
    }

    public void setUsedSize(Long usedSize) {
        this.usedSize = usedSize;
    }

    @Override
    public String toString() {
        return "DiskItemInfo{" +
                "fileSystem='" + fileSystem + '\'' +
                ", mounted='" + mounted + '\'' +
                ", totalSize=" + totalSize +
                ", usedSize=" + usedSize +
                '}';
    }
}
