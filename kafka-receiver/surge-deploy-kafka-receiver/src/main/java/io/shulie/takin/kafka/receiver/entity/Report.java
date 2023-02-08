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
 * 
 * </p>
 *
 * @author zhaoyong
 * @since 2023-01-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_report")
@ApiModel(value = "Report对象", description = "")
public class Report implements Serializable {


    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "负责人id")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "客户id")
    @TableField("custom_id")
    private Long customId;

    @ApiModelProperty(value = "流量消耗")
    private BigDecimal amount;

    @ApiModelProperty(value = "场景ID")
    @TableField("scene_id")
    private Long sceneId;

    @ApiModelProperty(value = "场景名称")
    @TableField("scene_name")
    private String sceneName;

    @ApiModelProperty(value = "开始时间")
    @TableField("start_time")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "结束时间")
    @TableField("end_time")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "报表生成状态:0/就绪状态，1/生成中,2/完成生成")
    private Integer status;

    @ApiModelProperty(value = "报告类型；0普通场景，1流量调试")
    private Integer type;

    @ApiModelProperty(value = "压测结论: 0/不通过，1/通过")
    private Integer conclusion;

    @ApiModelProperty(value = "请求总数")
    @TableField("total_request")
    private Long totalRequest;

    @ApiModelProperty(value = "施压类型,0:并发,1:tps,2:自定义;不填默认为0")
    @TableField("pressure_type")
    private Integer pressureType;

    @ApiModelProperty(value = "平均线程数")
    @TableField("avg_concurrent")
    private BigDecimal avgConcurrent;

    @ApiModelProperty(value = "目标tps")
    private Integer tps;

    @ApiModelProperty(value = "平均tps")
    @TableField("avg_tps")
    private BigDecimal avgTps;

    @ApiModelProperty(value = "平均响应时间")
    @TableField("avg_rt")
    private BigDecimal avgRt;

    @ApiModelProperty(value = "最大并发")
    private Integer concurrent;

    @ApiModelProperty(value = "成功率")
    @TableField("success_rate")
    private BigDecimal successRate;

    @ApiModelProperty(value = "sa")
    private BigDecimal sa;

    @ApiModelProperty(value = "操作用户ID")
    @TableField("operate_id")
    private Long operateId;

    @ApiModelProperty(value = "扩展字段，JSON数据格式")
    private String features;

    @ApiModelProperty(value = "是否删除:0/正常，1、已删除")
    @TableField("is_deleted")
    private Integer isDeleted;

    @TableField("gmt_create")
    private LocalDateTime gmtCreate;

    @TableField("gmt_update")
    private LocalDateTime gmtUpdate;

    @ApiModelProperty(value = "部门id")
    @TableField("dept_id")
    private Long deptId;

    @ApiModelProperty(value = "创建人id")
    @TableField("create_uid")
    private Long createUid;

    @ApiModelProperty(value = "1-解锁 9-锁定")
    @TableField("`lock`")
    private Integer lock;

    @ApiModelProperty(value = "脚本id")
    @TableField("script_id")
    private Long scriptId;

    @ApiModelProperty(value = "租户字段，customer_id,custom_id已废弃")
    @TableField("customer_id")
    private Long customerId;

    @ApiModelProperty(value = "租户 id, 默认 1")
    @TableField("tenant_id")
    private Long tenantId;

    @ApiModelProperty(value = "环境标识")
    @TableField("env_code")
    private String envCode;

    @ApiModelProperty(value = "脚本节点树")
    @TableField("script_node_tree")
    private String scriptNodeTree;

    @ApiModelProperty(value = "数据签名")
    @TableField("`sign`")
    private String sign;

    @ApiModelProperty(value = "任务Id")
    @TableField("task_id")
    private Long taskId;

    @ApiModelProperty(value = "资源Id")
    @TableField("resource_id")
    private Long resourceId;

    @ApiModelProperty(value = "压测引擎任务Id")
    @TableField("job_id")
    private Long jobId;

    @ApiModelProperty(value = "数据校准状态")
    @TableField("calibration_status")
    private Integer calibrationStatus;

    @ApiModelProperty(value = "数据校准信息")
    @TableField("calibration_message")
    private String calibrationMessage;

    @ApiModelProperty(value = "施压配置")
    @TableField("pt_config")
    private String ptConfig;


}
