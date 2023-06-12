package io.shulie.surge.data.suppliers.grpc.remoting.node;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description
 * @Author ocean_wll
 * @Date 2022/11/4 11:35
 */
public class GCInfo implements Serializable {
    private final static long serialVersionUID = 1L;

    /**
     * youngGc次数
     */
    private Integer youngGcCount;

    /**
     * youngGc总时间
     */
    private BigDecimal youngGcCost;

    /**
     * fullGc次数
     */
    private Integer fullGcCount;

    /**
     * fullGc总时间
     */
    private BigDecimal fullGcCost;

    public Integer getYoungGcCount() {
        return youngGcCount;
    }

    public void setYoungGcCount(Integer youngGcCount) {
        this.youngGcCount = youngGcCount;
    }

    public BigDecimal getYoungGcCost() {
        return youngGcCost;
    }

    public void setYoungGcCost(BigDecimal youngGcCost) {
        this.youngGcCost = youngGcCost;
    }

    public Integer getFullGcCount() {
        return fullGcCount;
    }

    public void setFullGcCount(Integer fullGcCount) {
        this.fullGcCount = fullGcCount;
    }

    public BigDecimal getFullGcCost() {
        return fullGcCost;
    }

    public void setFullGcCost(BigDecimal fullGcCost) {
        this.fullGcCost = fullGcCost;
    }

    @Override
    public String toString() {
        return "GCInfo{" +
                "youngGcCount=" + youngGcCount +
                ", youngGcCost=" + youngGcCost +
                ", fullGcCount=" + fullGcCount +
                ", fullGcCost=" + fullGcCost +
                '}';
    }
}
