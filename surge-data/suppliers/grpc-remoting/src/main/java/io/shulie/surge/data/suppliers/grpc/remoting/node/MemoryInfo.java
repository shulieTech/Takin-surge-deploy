package io.shulie.surge.data.suppliers.grpc.remoting.node;

import java.io.Serializable;

/**
 * @Description 机器内存数据
 * @Author ocean_wll
 * @Date 2022/11/1 10:26
 */
public class MemoryInfo implements Serializable {
    private final static long serialVersionUID = 1L;

    /**
     * 节点总内存 KB
     */
    private Long totalMemory;

    /**
     * 节点已使用内存 KB
     */
    private Long memoryUsed;

    public Long getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(Long totalMemory) {
        this.totalMemory = totalMemory;
    }

    public Long getMemoryUsed() {
        return memoryUsed;
    }

    public void setMemoryUsed(Long memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    @Override
    public String toString() {
        return "MemoryInfo{" +
                "totalMemory=" + totalMemory +
                ", memoryUsed=" + memoryUsed +
                '}';
    }
}
