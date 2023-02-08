package io.shulie.takin.kafka.receiver.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * t_engine_metrics_all
 * </p>
 *
 * @author zhaoyong
 * @since 2023-01-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_engine_metrics_all")
@ApiModel(value = "EngineMetricsAll对象", description = "t_engine_metrics_all")
public class EngineMetricsAll implements Serializable {


    @TableField(value = "time")
    private Long time;

    @TableField(value = "transaction")
    private String transaction;

    @TableField(value = "test_name")
    private String testName;

    private Integer count;

    @TableField("fail_count")
    private Integer failCount;

    @TableField("sent_bytes")
    private Long sentBytes;

    @TableField("received_bytes")
    private Long receivedBytes;

    private BigDecimal rt;

    @TableField("sum_rt")
    private BigDecimal sumRt;

    @TableField("sa_count")
    private Integer saCount;

    @TableField("max_rt")
    private BigDecimal maxRt;

    @TableField("min_rt")
    private BigDecimal minRt;

    private Long timestamp;

    @TableField("active_threads")
    private Integer activeThreads;

    @TableField("percent_data")
    private String percentData;

    @TableField(value = "pod_no")
    private String podNo;

    @TableField("job_id")
    private String jobId;

    @TableField("createDate")
    private LocalDateTime createDate;


}
