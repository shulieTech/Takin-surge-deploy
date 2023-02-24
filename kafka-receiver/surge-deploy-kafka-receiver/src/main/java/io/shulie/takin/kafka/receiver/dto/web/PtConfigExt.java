package io.shulie.takin.kafka.receiver.dto.web;

import lombok.Data;

import java.util.Map;

/**
 * 施压配置
 *
 * @author liyuanba
 * @date 2021/10/29 9:51 上午
 */
@Data
public class PtConfigExt {
    /**
     * pod数
     */
    private Integer podNum;
    /**
     * 压测时长
     */
    private Long duration;
    /**
     * 压测时长单位
     */
    private String unit;
    /**
     * 流量预估：各个线程组的总和
     */
    private Double estimateFlow;
    /**
     * 线程组配置
     */
    private Map<String, ThreadGroupConfigExt> threadGroupConfigMap;
}
