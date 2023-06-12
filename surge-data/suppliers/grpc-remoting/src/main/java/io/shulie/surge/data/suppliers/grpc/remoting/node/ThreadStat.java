package io.shulie.surge.data.suppliers.grpc.remoting.node;

import java.io.Serializable;

/**
 * @Description
 * @Author ocean_wll
 * @Date 2022/11/29 11:28
 */
public class ThreadStat implements Serializable {
    private final static long serialVersionUID = 1L;

    /**
     * 线程 ID
     */
    private long threadId;

    /**
     * 线程状态
     */
    private String state;

    /**
     * 线程名称
     */
    private String threadName;
    /**
     * 线程组名称
     */
    private String groupName;
    /**
     * cpu 使用率
     */
    private double cpuUsage;
    /**
     * cpu 时间 毫秒
     */
    private double cpuTime;

    /**
     * 是否是中断
     */
    private boolean interrupted;

    /**
     * 是否是守护线程
     */
    private boolean daemon;

    /**
     * 优先级
     */
    private int priority;

    /**
     * 线程堆栈等信息
     */
    private String threadInfo;

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isDaemon() {
        return daemon;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public boolean isInterrupted() {
        return interrupted;
    }

    public void setInterrupted(boolean interrupted) {
        this.interrupted = interrupted;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public double getCpuTime() {
        return cpuTime;
    }

    public void setCpuTime(double cpuTime) {
        this.cpuTime = cpuTime;
    }

    public String getThreadInfo() {
        return threadInfo;
    }

    public void setThreadInfo(String threadInfo) {
        this.threadInfo = threadInfo;
    }

    @Override
    public String toString() {
        return "ThreadStat{" +
                "threadId=" + threadId +
                ", stat='" + state + '\'' +
                ", threadName='" + threadName + '\'' +
                ", groupName='" + groupName + '\'' +
                ", cpuUsage=" + cpuUsage +
                ", cpuTime=" + cpuTime +
                ", interrupted=" + interrupted +
                ", daemon=" + daemon +
                ", priority=" + priority +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ThreadStat)) {
            return false;
        }

        ThreadStat that = (ThreadStat) o;

        if (getThreadId() != that.getThreadId()) {
            return false;
        }
        return getThreadName() != null ? getThreadName().equals(that.getThreadName()) : that.getThreadName() == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (getThreadId() ^ (getThreadId() >>> 32));
        result = 31 * result + (getThreadName() != null ? getThreadName().hashCode() : 0);
        return result;
    }
}
