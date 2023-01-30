package io.shulie.takin.kafka.receiver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
@TableName("default.t_engine_metrics")
@ApiModel(value = "EngineMetrics对象", description = "t_engine_metrics")
public class EngineMetrics implements Serializable {


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
    private Integer sentBytes;

    @TableField("received_bytes")
    private Integer receivedBytes;

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

    private LocalDateTime createDate;


}
