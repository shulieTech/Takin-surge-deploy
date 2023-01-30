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
 * t_engine_metrics_all
 * </p>
 *
 * @author zhaoyong
 * @since 2023-01-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("default.t_engine_metrics_all")
@ApiModel(value = "EngineMetricsAll对象", description = "t_engine_metrics_all")
public class EngineMetricsAll implements Serializable {


    @TableId(value = "time", type = IdType.AUTO)
    private Integer time;

    @TableId(value = "transaction", type = IdType.AUTO)
    private String transaction;

    @TableId(value = "test_name", type = IdType.AUTO)
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

    private Integer timestamp;

    @TableField("active_threads")
    private Integer activeThreads;

    @TableField("percent_data")
    private String percentData;

    @TableId(value = "pod_no", type = IdType.AUTO)
    private String podNo;

    @TableField("job_id")
    private String jobId;

    private LocalDateTime createDate;


}
