package io.shulie.takin.kafka.receiver.dto.cloud;

import lombok.Data;

/**
 * 指标信息
 *
 * @author <a href="mailto:472546172@qq.com">张天赐</a>
 */
@Data
public class MetricsInfo {
    private Long time;
    /**
     * 关键词
     */
    private String transaction;
    /**
     * 业务活动名称
     */
    private String testName;
    /**
     * 请求总数
     */
    private Integer count;
    /**
     * 失败请求总数
     */
    private Integer failCount;
    /**
     * 请求数据大小
     */
    private Long sentBytes;
    /**
     * 响应数据大小
     */
    private Long receivedBytes;
    /**
     * 接口响应时间 - 瓶颈
     */
    private Double rt;
    /**
     * 接口响应时间 - 求和
     */
    private Double sumRt;
    /**
     * SA总数
     */
    private Integer saCount;
    /**
     * 最大接口响应时间
     */
    private Double maxRt;
    /**
     * 最小接口响应时间
     */
    private Double minRt;
    /**
     * 时间戳
     */
    private Long timestamp;
    /**
     * 活跃线程数
     */
    private Integer activeThreads;
    /**
     * 百分位数据
     */
    private String percentData;
    /**
     * 施压任务实例编号
     */
    private String podNo;
    /**
     * 上报的数据类型
     */
    private String type;
}