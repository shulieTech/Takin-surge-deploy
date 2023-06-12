package io.shulie.surge.data.suppliers.grpc.remoting.node;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description cpu信息
 * @Author ocean_wll
 * @Date 2022/11/1 10:30
 */
public class CpuInfo implements Serializable {
    private final static long serialVersionUID = 1L;

    /**
     * 节点 cpu 核数
     */
    private double containerCores;

    /**
     * 机器总cpu核数
     */
    private double totalCores;

    /**
     * cpu 总使用率
     */
    private BigDecimal coresPercent;

    public double getContainerCores() {
        return containerCores;
    }

    public void setContainerCores(double containerCores) {
        this.containerCores = containerCores;
    }

    public double getTotalCores() {
        return totalCores;
    }

    public void setTotalCores(double totalCores) {
        this.totalCores = totalCores;
    }

    public BigDecimal getCoresPercent() {
        return coresPercent;
    }

    public void setCoresPercent(BigDecimal coresPercent) {
        this.coresPercent = coresPercent;
    }

    @Override
    public String toString() {
        return "CpuInfo{" +
                "coresPercent=" + coresPercent +
                '}';
    }
}
