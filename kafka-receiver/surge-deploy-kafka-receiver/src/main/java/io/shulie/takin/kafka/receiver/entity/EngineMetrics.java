package io.shulie.takin.kafka.receiver.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * t_engine_metrics
 * </p>
 *
 * @author zhaoyong
 * @since 2023-01-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EngineMetrics对象", description = "t_engine_metrics")
public class EngineMetrics implements Serializable {

    @JsonProperty(value = "time")
    private Long time;

    @JsonProperty(value = "transaction")
    private String transaction;

    @JsonProperty(value = "test_name")
    private String testName;

    private Integer count;

    @JsonProperty("fail_count")
    private Integer failCount;

    @JsonProperty("sent_bytes")
    private Long sentBytes;

    @JsonProperty("received_bytes")
    private Long receivedBytes;

    private BigDecimal rt;

    @JsonProperty("sum_rt")
    private BigDecimal sumRt;

    @JsonProperty("sa_count")
    private Integer saCount;

    @JsonProperty("max_rt")
    private BigDecimal maxRt;

    @JsonProperty("min_rt")
    private BigDecimal minRt;

    private Long timestamp;

    @JsonProperty("active_threads")
    private Integer activeThreads;

    @JsonProperty("percent_data")
    private String percentData;

    @JsonProperty(value = "pod_no")
    private String podNo;

    @JsonProperty("job_id")
    private String jobId;

    @JsonProperty("createDate")
    private LocalDateTime createDate;


}
