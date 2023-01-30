package io.shulie.takin.kafka.receiver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * t_engine_pressure
 * </p>
 *
 * @author zhaoyong
 * @since 2023-01-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("default.t_engine_pressure")
@ApiModel(value = "EnginePressure对象", description = "t_engine_pressure")
public class EnginePressure implements Serializable {

    @TableField(value = "time")
    private Long time;

    @TableField(value = "transaction")
    private String transaction;

    @TableField("avg_rt")
    private BigDecimal avgRt;

    @TableField("avg_tps")
    private BigDecimal avgTps;

    @TableField(value = "test_name")
    private String testName;

    private Integer count;

    @TableField("create_time")
    private Integer createTime;

    @TableField("data_num")
    private Integer dataNum;

    @TableField("data_rate")
    private BigDecimal dataRate;

    @TableField("fail_count")
    private Integer failCount;

    @TableField("sent_bytes")
    private Long sentBytes;

    @TableField("received_bytes")
    private Long receivedBytes;

    @TableField("sum_rt")
    private BigDecimal sumRt;

    private BigDecimal sa;

    @TableField("sa_count")
    private Integer saCount;

    @TableField("max_rt")
    private BigDecimal maxRt;

    @TableField("min_rt")
    private BigDecimal minRt;

    @TableField("active_threads")
    private Integer activeThreads;

    @TableField("sa_percent")
    private String saPercent;

    private Integer status;

    @TableField("success_rate")
    private BigDecimal successRate;

    @TableField("job_id")
    private String jobId;

    private LocalDateTime createDate;


}
