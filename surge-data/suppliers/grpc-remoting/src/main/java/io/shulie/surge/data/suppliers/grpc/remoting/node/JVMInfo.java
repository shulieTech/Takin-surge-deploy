package io.shulie.surge.data.suppliers.grpc.remoting.node;

import java.io.Serializable;

/**
 * @Description
 * @Author ocean_wll
 * @Date 2022/11/4 11:35
 */
public class JVMInfo implements Serializable {
    private final static long serialVersionUID = 1L;

    /**
     * 应用启动参数
     */
    private String jvmArgs;

    /**
     * jdk 版本号
     */
    private String javaVersion;

    /**
     * 最大堆内存
     */
    private Long heapMax;

    /**
     * 最小堆内存
     */
    private Long heapMin;

    /**
     * 已使用堆内存
     */
    private Long heapUsed;

    /**
     * 最小伊甸园区大小
     */
    private Long edenMin;

    /**
     * 最大伊甸园区大小
     */
    private Long edenMax;

    /**
     * 已使用伊甸园区大小
     */
    private Long edenUsed;

    /**
     * 最小幸存者区大小
     */
    private Long survivorMin;

    /**
     * 最大幸存者区大小
     */
    private Long survivorMax;

    /**
     * 幸存者区已使用大小
     */
    private Long survivorUsed;

    /**
     * 最小老年代大小
     */
    private Long oldMin;

    /**
     * 最大老年代大小
     */
    private Long oldMax;

    /**
     * 老年代已使用大小
     */
    private Long oldUsed;

    /**
     * 最大元空间大小
     */
    private Long metaSpaceMax;

    /**
     * 最小元空间大小
     */
    private Long metaSpaceMin;

    /**
     * 已使用元空间大小
     */
    private Long metaSpaceUsed;

    /**
     * 最小代码缓存区大小
     */
    private Long codeCacheMin;

    /**
     * 最大代码缓存区大小
     */
    private Long codeCacheMax;

    /**
     * 已使用代码缓存区大小
     */
    private Long codeCacheUsed;

    /**
     * 最小非堆大小
     */
    private Long nonHeapMin;

    /**
     * 最大非堆大小
     */
    private Long nonHeapMax;

    /**
     * 已使用非堆大小
     */
    private Long nonHeapUsed;


    public String getJvmArgs() {
        return jvmArgs;
    }

    public void setJvmArgs(String jvmArgs) {
        this.jvmArgs = jvmArgs;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public Long getHeapMax() {
        return heapMax;
    }

    public void setHeapMax(Long heapMax) {
        this.heapMax = heapMax;
    }

    public Long getHeapMin() {
        return heapMin;
    }

    public void setHeapMin(Long heapMin) {
        this.heapMin = heapMin;
    }

    public Long getHeapUsed() {
        return heapUsed;
    }

    public void setHeapUsed(Long heapUsed) {
        this.heapUsed = heapUsed;
    }

    public Long getEdenMin() {
        return edenMin;
    }

    public void setEdenMin(Long edenMin) {
        this.edenMin = edenMin;
    }

    public Long getEdenMax() {
        return edenMax;
    }

    public void setEdenMax(Long edenMax) {
        this.edenMax = edenMax;
    }

    public Long getEdenUsed() {
        return edenUsed;
    }

    public void setEdenUsed(Long edenUsed) {
        this.edenUsed = edenUsed;
    }

    public Long getSurvivorMin() {
        return survivorMin;
    }

    public void setSurvivorMin(Long survivorMin) {
        this.survivorMin = survivorMin;
    }

    public Long getSurvivorMax() {
        return survivorMax;
    }

    public void setSurvivorMax(Long survivorMax) {
        this.survivorMax = survivorMax;
    }

    public Long getSurvivorUsed() {
        return survivorUsed;
    }

    public void setSurvivorUsed(Long survivorUsed) {
        this.survivorUsed = survivorUsed;
    }

    public Long getOldMin() {
        return oldMin;
    }

    public void setOldMin(Long oldMin) {
        this.oldMin = oldMin;
    }

    public Long getOldMax() {
        return oldMax;
    }

    public void setOldMax(Long oldMax) {
        this.oldMax = oldMax;
    }

    public Long getOldUsed() {
        return oldUsed;
    }

    public void setOldUsed(Long oldUsed) {
        this.oldUsed = oldUsed;
    }

    public Long getMetaSpaceMax() {
        return metaSpaceMax;
    }

    public void setMetaSpaceMax(Long metaSpaceMax) {
        this.metaSpaceMax = metaSpaceMax;
    }

    public Long getMetaSpaceMin() {
        return metaSpaceMin;
    }

    public void setMetaSpaceMin(Long metaSpaceMin) {
        this.metaSpaceMin = metaSpaceMin;
    }

    public Long getMetaSpaceUsed() {
        return metaSpaceUsed;
    }

    public void setMetaSpaceUsed(Long metaSpaceUsed) {
        this.metaSpaceUsed = metaSpaceUsed;
    }

    public Long getCodeCacheMin() {
        return codeCacheMin;
    }

    public void setCodeCacheMin(Long codeCacheMin) {
        this.codeCacheMin = codeCacheMin;
    }

    public Long getCodeCacheMax() {
        return codeCacheMax;
    }

    public void setCodeCacheMax(Long codeCacheMax) {
        this.codeCacheMax = codeCacheMax;
    }

    public Long getCodeCacheUsed() {
        return codeCacheUsed;
    }

    public void setCodeCacheUsed(Long codeCacheUsed) {
        this.codeCacheUsed = codeCacheUsed;
    }

    public Long getNonHeapMin() {
        return nonHeapMin;
    }

    public void setNonHeapMin(Long nonHeapMin) {
        this.nonHeapMin = nonHeapMin;
    }

    public Long getNonHeapMax() {
        return nonHeapMax;
    }

    public void setNonHeapMax(Long nonHeapMax) {
        this.nonHeapMax = nonHeapMax;
    }

    public Long getNonHeapUsed() {
        return nonHeapUsed;
    }

    public void setNonHeapUsed(Long nonHeapUsed) {
        this.nonHeapUsed = nonHeapUsed;
    }

    @Override
    public String toString() {
        return "JVMInfo{" +
                "jvmArgs='" + jvmArgs + '\'' +
                ", javaVersion='" + javaVersion + '\'' +
                ", heapMax=" + heapMax +
                ", heapMin=" + heapMin +
                ", heapUsed=" + heapUsed +
                ", edenMin=" + edenMin +
                ", edenMax=" + edenMax +
                ", edenUsed=" + edenUsed +
                ", survivorMin=" + survivorMin +
                ", survivorMax=" + survivorMax +
                ", survivorUsed=" + survivorUsed +
                ", oldMin=" + oldMin +
                ", oldMax=" + oldMax +
                ", oldUsed=" + oldUsed +
                ", metaSpaceMax=" + metaSpaceMax +
                ", metaSpaceMin=" + metaSpaceMin +
                ", metaSpaceUsed=" + metaSpaceUsed +
                ", codeCacheMin=" + codeCacheMin +
                ", codeCacheMax=" + codeCacheMax +
                ", codeCacheUsed=" + codeCacheUsed +
                ", nonHeapMin=" + nonHeapMin +
                ", nonHeapMax=" + nonHeapMax +
                ", nonHeapUsed=" + nonHeapUsed +
                '}';
    }
}
