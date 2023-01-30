package io.shulie.takin.kafka.receiver.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-09
 */
@TableName("t_scene_manage")
@ApiModel(value="SceneManage对象", description="")
@Data
public class SceneManage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "负责人id")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "客户id")
    @TableField("custom_id")
    private Long customId;

    @ApiModelProperty(value = "场景名称")
    @TableField("scene_name")
    private String sceneName;

    @ApiModelProperty(value = "参考数据字典 场景状态")
    private Integer status;

    @ApiModelProperty(value = "最新压测时间")
    @TableField("last_pt_time")
    private LocalDateTime lastPtTime;

    @ApiModelProperty(value = "施压配置")
    @TableField("pt_config")
    private String ptConfig;

    @ApiModelProperty(value = "场景类型:0普通场景，1流量调试")
    private Integer type;

    @ApiModelProperty(value = "脚本类型：0-Jmeter 1-Gatling")
    @TableField("script_type")
    private Integer scriptType;

    @TableField("is_archive")
    private Integer isArchive;

    @ApiModelProperty(value = "是否删除：0-否 1-是")
    @TableField("is_deleted")
    private Integer isDeleted;

    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "创建人")
    @TableField("create_name")
    private String createName;

    @ApiModelProperty(value = "最后修改时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "最后修改人")
    @TableField("update_name")
    private String updateName;

    @ApiModelProperty(value = "扩展字段")
    private String features;

    @ApiModelProperty(value = "脚本解析结果")
    @TableField("script_analysis_result")
    private String scriptAnalysisResult;

    @ApiModelProperty(value = "部门id")
    @TableField("dept_id")
    private Long deptId;

    @ApiModelProperty(value = "创建人id")
    @TableField("create_uid")
    private Long createUid;

    @ApiModelProperty(value = "租户字段，customer_id,custom_id已废弃")
    @TableField("customer_id")
    private Long customerId;

    @ApiModelProperty(value = "租户 id, 默认 1")
    @TableField("tenant_id")
    private Long tenantId;

    @ApiModelProperty(value = "环境标识")
    @TableField("env_code")
    private String envCode;

    @ApiModelProperty(value = "数据签名")
    private String sign;

    @TableField("business_flow_id")
    private Long businessFlowId;


}
