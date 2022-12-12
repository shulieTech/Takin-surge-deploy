package io.shulie.takin.kafka.receive.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@TableName("t_application_api_manage")
@ApiModel(value="ApplicationApiManage对象", description="")
public class ApplicationApiManage implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "应用主键")
    @TableField("APPLICATION_ID")
    private Long applicationId;

    @ApiModelProperty(value = "应用名称")
    @TableField("APPLICATION_NAME")
    private String applicationName;

    @ApiModelProperty(value = "租户id")
    @TableField("CUSTOMER_ID")
    private Long customerId;

    @ApiModelProperty(value = "用户id")
    @TableField("USER_ID")
    private Long userId;

    @ApiModelProperty(value = "创建时间")
    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField("UPDATE_TIME")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "是否有效 0:有效;1:无效")
    @TableField("IS_DELETED")
    private Integer isDeleted;

    @ApiModelProperty(value = "api")
    private String api;

    @ApiModelProperty(value = "请求类型")
    private String method;

    @ApiModelProperty(value = "是否有效 0:否;1:是")
    @TableField("IS_AGENT_REGISTE")
    private Integer isAgentRegiste;

    @ApiModelProperty(value = "租户 id, 默认 1")
    private Long tenantId;

    @ApiModelProperty(value = "环境标识")
    private String envCode;

    @ApiModelProperty(value = "部门id")
    private Long deptId;


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

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Integer getIsAgentRegiste() {
        return isAgentRegiste;
    }

    public void setIsAgentRegiste(Integer isAgentRegiste) {
        this.isAgentRegiste = isAgentRegiste;
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

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    @Override
    public String toString() {
        return "ApplicationApiManage{" +
        "id=" + id +
        ", applicationId=" + applicationId +
        ", applicationName=" + applicationName +
        ", customerId=" + customerId +
        ", userId=" + userId +
        ", createTime=" + createTime +
        ", updateTime=" + updateTime +
        ", isDeleted=" + isDeleted +
        ", api=" + api +
        ", method=" + method +
        ", isAgentRegiste=" + isAgentRegiste +
        ", tenantId=" + tenantId +
        ", envCode=" + envCode +
        ", deptId=" + deptId +
        "}";
    }
}
