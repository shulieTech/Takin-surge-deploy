package io.shulie.surge.data.suppliers.grpc.remoting.node;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @Description
 * @Author ocean_wll
 * @Date 2022/11/1 10:40
 */
public class ThreadInfo implements Serializable {
    private final static long serialVersionUID = 1L;

    /**
     * 创建的线程总数
     */
    private Long createdThreadNum;

    /**
     * 当前活跃线程数
     */
    private Integer currentActiveCount;

    /**
     * 线程池信息
     */
    private List<ThreadPoolInfo> threadPoolInfo;

    /**
     * 死锁信息
     */
    private List<DeadLockInfo> deadLockInfo;

    /**
     * 线程信息
     */
    private Set<ThreadStat> threadInfo;

    public Long getCreatedThreadNum() {
        return createdThreadNum;
    }

    public void setCreatedThreadNum(Long createdThreadNum) {
        this.createdThreadNum = createdThreadNum;
    }

    public Integer getCurrentActiveCount() {
        return currentActiveCount;
    }

    public void setCurrentActiveCount(Integer currentActiveCount) {
        this.currentActiveCount = currentActiveCount;
    }

    public List<ThreadPoolInfo> getThreadPoolInfo() {
        return threadPoolInfo;
    }

    public void setThreadPoolInfo(List<ThreadPoolInfo> threadPoolInfo) {
        this.threadPoolInfo = threadPoolInfo;
    }

    public List<DeadLockInfo> getDeadLockInfo() {
        return deadLockInfo;
    }

    public void setDeadLockInfo(List<DeadLockInfo> deadLockInfo) {
        this.deadLockInfo = deadLockInfo;
    }

    public Set<ThreadStat> getThreadInfo() {
        return threadInfo;
    }

    public void setThreadInfo(Set<ThreadStat> threadInfo) {
        this.threadInfo = threadInfo;
    }

    @Override
    public String toString() {
        return "ThreadInfo{" +
                "createdThreadNum=" + createdThreadNum +
                ", currentActiveCount=" + currentActiveCount +
                ", threadPoolInfo=" + threadPoolInfo +
                ", deadLockInfo=" + deadLockInfo +
                ", threadInfo=" + threadInfo +
                '}';
    }
}
