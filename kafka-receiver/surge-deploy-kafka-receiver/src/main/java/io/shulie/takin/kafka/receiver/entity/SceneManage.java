package io.shulie.takin.kafka.receiver.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

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
public class SceneManage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "负责人id")
    private Long userId;

    @ApiModelProperty(value = "客户id")
    private Long customId;

    @ApiModelProperty(value = "场景名称")
    private String sceneName;

    @ApiModelProperty(value = "参考数据字典 场景状态")
    @TableField("`status`")
    private Integer status;

    @ApiModelProperty(value = "最新压测时间")
    private LocalDateTime lastPtTime;

    @ApiModelProperty(value = "施压配置")
    private String ptConfig;

    @ApiModelProperty(value = "场景类型:0普通场景，1流量调试")
    @TableField("`type`")
    private Integer type;

    @ApiModelProperty(value = "脚本类型：0-Jmeter 1-Gatling")
    private Integer scriptType;

    private Integer isArchive;

    @ApiModelProperty(value = "是否删除：0-否 1-是")
    private Integer isDeleted;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "创建人")
    private String createName;

    @ApiModelProperty(value = "最后修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "最后修改人")
    private String updateName;

    @ApiModelProperty(value = "扩展字段")
    private String features;

    @ApiModelProperty(value = "脚本解析结果")
    private String scriptAnalysisResult;

    @ApiModelProperty(value = "部门id")
    private Long deptId;

    @ApiModelProperty(value = "创建人id")
    private Long createUid;

    @ApiModelProperty(value = "租户字段，customer_id,custom_id已废弃")
    private Long customerId;

    @ApiModelProperty(value = "租户 id, 默认 1")
    private Long tenantId;

    @ApiModelProperty(value = "环境标识")
    private String envCode;

    @ApiModelProperty(value = "数据签名")
    private String sign;

    private Long businessFlowId;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCustomId() {
        return customId;
    }

    public void setCustomId(Long customId) {
        this.customId = customId;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getLastPtTime() {
        return lastPtTime;
    }

    public void setLastPtTime(LocalDateTime lastPtTime) {
        this.lastPtTime = lastPtTime;
    }

    public String getPtConfig() {
        return ptConfig;
    }

    public void setPtConfig(String ptConfig) {
        this.ptConfig = ptConfig;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getScriptType() {
        return scriptType;
    }

    public void setScriptType(Integer scriptType) {
        this.scriptType = scriptType;
    }

    public Integer getIsArchive() {
        return isArchive;
    }

    public void setIsArchive(Integer isArchive) {
        this.isArchive = isArchive;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getCreateName() {
        return createName;
    }

    public void setCreateName(String createName) {
        this.createName = createName;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateName() {
        return updateName;
    }

    public void setUpdateName(String updateName) {
        this.updateName = updateName;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public String getScriptAnalysisResult() {
        return scriptAnalysisResult;
    }

    public void setScriptAnalysisResult(String scriptAnalysisResult) {
        this.scriptAnalysisResult = scriptAnalysisResult;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getCreateUid() {
        return createUid;
    }

    public void setCreateUid(Long createUid) {
        this.createUid = createUid;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getEnvCode() {
        return envCode;
    }

    public void setEnvCode(String envCode) {
        this.envCode = envCode;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public Long getBusinessFlowId() {
        return businessFlowId;
    }

    public void setBusinessFlowId(Long businessFlowId) {
        this.businessFlowId = businessFlowId;
    }

    @Override
    public String toString() {
        return "SceneManage{" +
        "id=" + id +
        ", userId=" + userId +
        ", customId=" + customId +
        ", sceneName=" + sceneName +
        ", status=" + status +
        ", lastPtTime=" + lastPtTime +
        ", ptConfig=" + ptConfig +
        ", type=" + type +
        ", scriptType=" + scriptType +
        ", isArchive=" + isArchive +
        ", isDeleted=" + isDeleted +
        ", createTime=" + createTime +
        ", createName=" + createName +
        ", updateTime=" + updateTime +
        ", updateName=" + updateName +
        ", features=" + features +
        ", scriptAnalysisResult=" + scriptAnalysisResult +
        ", deptId=" + deptId +
        ", createUid=" + createUid +
        ", customerId=" + customerId +
        ", tenantId=" + tenantId +
        ", envCode=" + envCode +
        ", sign=" + sign +
        ", businessFlowId=" + businessFlowId +
        "}";
    }
}
