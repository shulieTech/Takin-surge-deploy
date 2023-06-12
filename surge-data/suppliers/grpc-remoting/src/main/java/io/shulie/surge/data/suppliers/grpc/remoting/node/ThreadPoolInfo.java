package io.shulie.surge.data.suppliers.grpc.remoting.node;

import java.io.Serializable;
import java.util.List;

/**
 * @Description
 * @Author ocean_wll
 * @Date 2022/11/1 10:41
 */
public class ThreadPoolInfo implements Serializable {
    private final static long serialVersionUID = 1L;

    /**
     * 线程池Id 对象的hash值
     */
    private String threadPoolId;

    /**
     * 最小线程数 （核心线程数）
     */
    private Integer minPoolSize;

    /**
     * 最大线程数
     */
    private Integer maxPoolSize;

    /**
     * 当前线程池中的线程数
     */
    private Integer poolSize;

    /**
     * 活跃线程数
     */
    private Integer activeCount;

    /**
     * 任务数
     */
    private Long taskCount;

    /**
     * 子线程id集合
     */
    private List<String> childThreadIdList;

    /**
     * 线程名
     */
    private String threadPoolName;

    public String getThreadPoolId() {
        return threadPoolId;
    }

    public void setThreadPoolId(String threadPoolId) {
        this.threadPoolId = threadPoolId;
    }

    public Integer getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(Integer minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public Integer getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(Integer maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public Integer getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(Integer poolSize) {
        this.poolSize = poolSize;
    }

    public Integer getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(Integer activeCount) {
        this.activeCount = activeCount;
    }

    public Long getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Long taskCount) {
        this.taskCount = taskCount;
    }

    public List<String> getChildThreadIdList() {
        return childThreadIdList;
    }

    public void setChildThreadIdList(List<String> childThreadIdList) {
        this.childThreadIdList = childThreadIdList;
    }

    public String getThreadPoolName() {
        return threadPoolName;
    }

    public void setThreadPoolName(String threadPoolName) {
        this.threadPoolName = threadPoolName;
    }

    @Override
    public String toString() {
        return "ThreadPoolItemInfo{" +
                "threadPoolId='" + threadPoolId + '\'' +
                ", minPoolSize=" + minPoolSize +
                ", maxPoolSize=" + maxPoolSize +
                ", poolSize=" + poolSize +
                ", activeCount=" + activeCount +
                ", taskCount=" + taskCount +
                '}';
    }
}
