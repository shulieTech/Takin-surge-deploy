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
 * t_performance_base_data
 * </p>
 *
 * @author zhaoyong
 * @since 2023-01-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("default.t_performance_base_data")
@ApiModel(value = "PerformanceBaseData对象", description = "t_performance_base_data")
public class PerformanceBaseData implements Serializable {

    private Long timestamp;

    @TableField("total_memory")
    private Long totalMemory;

    @TableField("perm_memory")
    private Long permMemory;

    @TableField("young_memory")
    private Long youngMemory;

    @TableField("old_memory")
    private Long oldMemory;

    @TableField("young_gc_count")
    private Integer youngGcCount;

    @TableField("full_gc_count")
    private Integer fullGcCount;

    @TableField("young_gc_cost")
    private Long youngGcCost;

    @TableField("full_gc_cost")
    private Long fullGcCost;

    @TableField("cpu_use_rate")
    private BigDecimal cpuUseRate;

    @TableField("total_buffer_pool_memory")
    private Long totalBufferPoolMemory;

    @TableField("total_no_heap_memory")
    private Long totalNoHeapMemory;

    @TableField("thread_count")
    private Integer threadCount;

    @TableField("base_id")
    private Long baseId;

    @TableField("agent_id")
    private String agentId;

    @TableField(value = "app_name")
    private String appName;

    @TableField("app_ip")
    private String appIp;

    @TableField("process_id")
    private String processId;

    @TableField("process_name")
    private String processName;

    @TableField("env_code")
    private String envCode;

    @TableField("tenant_app_key")
    private String tenantAppKey;

    @TableField("tenant_id")
    private Long tenantId;

    private LocalDateTime createDate;

    private Long time;

}
