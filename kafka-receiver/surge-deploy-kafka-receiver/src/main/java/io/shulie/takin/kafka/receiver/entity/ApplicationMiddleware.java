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
 * 应用中间件
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@TableName("t_application_middleware")
@ApiModel(value="ApplicationMiddleware对象", description="应用中间件")
public class ApplicationMiddleware implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "应用id")
    private Long applicationId;

    @ApiModelProperty(value = "应用名称")
    private String applicationName;

    @ApiModelProperty(value = "项目名称")
    private String artifactId;

    @ApiModelProperty(value = "项目组织名称")
    private String groupId;

    @ApiModelProperty(value = "版本号")
    private String version;

    @ApiModelProperty(value = "类型, 字符串形式")
    @TableField("`type`")
    private String type;

    @ApiModelProperty(value = "状态, 3已支持, 2 未支持, 4 无需支持, 1 未知, 0 无")
    @TableField("`status`")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime gmtCreate;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime gmtUpdate;

    @ApiModelProperty(value = "逻辑删除字段, 0 未删除, 1 已删除")
    private Integer isDeleted;

    @ApiModelProperty(value = "环境code")
    private String envCode;

    @ApiModelProperty(value = "租户id")
    private Long tenantId;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public LocalDateTime getGmtUpdate() {
        return gmtUpdate;
    }

    public void setGmtUpdate(LocalDateTime gmtUpdate) {
        this.gmtUpdate = gmtUpdate;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getEnvCode() {
        return envCode;
    }

    public void setEnvCode(String envCode) {
        this.envCode = envCode;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String toString() {
        return "ApplicationMiddleware{" +
        "id=" + id +
        ", applicationId=" + applicationId +
        ", applicationName=" + applicationName +
        ", artifactId=" + artifactId +
        ", groupId=" + groupId +
        ", version=" + version +
        ", type=" + type +
        ", status=" + status +
        ", gmtCreate=" + gmtCreate +
        ", gmtUpdate=" + gmtUpdate +
        ", isDeleted=" + isDeleted +
        ", envCode=" + envCode +
        ", tenantId=" + tenantId +
        "}";
    }
}
