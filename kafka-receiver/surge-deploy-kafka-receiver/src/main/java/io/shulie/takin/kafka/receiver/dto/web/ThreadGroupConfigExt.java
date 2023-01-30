package io.shulie.takin.kafka.receiver.dto.web;

import lombok.Data;

/**
 * @author liyuanba
 * @date 2021/10/29 9:57 上午
 */
@Data
public class ThreadGroupConfigExt {
    /**
     * 压力模式：并发、tps
     */
    private Integer type;
    /**
     * 施压模式：固定压力值，线性递增，阶梯递增
     */
    private Integer mode;
    /**
     * 并发线程数
     */
    private Integer threadNum;
    /**
     * 线程递增时长
     */
    private Integer rampUp;
    /**
     * rampUp的时间单位
     */
    private String rampUpUnit;
    /**
     * 线程启动时从0到最大线程数阶梯层数
     */
    private Integer steps;
    /**
     * 流量预估
     */
    private Double estimateFlow;
}
