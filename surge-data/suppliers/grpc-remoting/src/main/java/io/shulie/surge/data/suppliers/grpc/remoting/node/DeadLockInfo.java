package io.shulie.surge.data.suppliers.grpc.remoting.node;

import java.io.Serializable;
import java.util.Objects;

/**
 * @Description
 * @Author ocean_wll
 * @Date 2022/11/29 11:26
 */
public class DeadLockInfo implements Serializable {
    private final static long serialVersionUID = 1L;

    /**
     * 线程id
     */
    private Long threadId;

    /**
     * 锁线程ID
     */
    private Long lockOwnerId;

    /**
     * 死锁信息
     */
    private String deadLockInfo;

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public Long getLockOwnerId() {
        return lockOwnerId;
    }

    public void setLockOwnerId(Long lockOwnerId) {
        this.lockOwnerId = lockOwnerId;
    }

    public String getDeadLockInfo() {
        return deadLockInfo;
    }

    public void setDeadLockInfo(String deadLockInfo) {
        this.deadLockInfo = deadLockInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeadLockInfo that = (DeadLockInfo) o;

        if (!Objects.equals(threadId, that.threadId)) return false;
        return Objects.equals(lockOwnerId, that.lockOwnerId);
    }

    @Override
    public int hashCode() {
        int result = threadId != null ? threadId.hashCode() : 0;
        result = 31 * result + (lockOwnerId != null ? lockOwnerId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DeadLockInfo{" +
                "threadId=" + threadId +
                ", lockOwnerId=" + lockOwnerId +
                ", deadLockInfo='" + deadLockInfo + '\'' +
                '}';
    }
}
