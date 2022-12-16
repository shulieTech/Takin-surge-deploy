package io.shulie.takin.kafka.receiver.entity;

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
 * 探针心跳数据
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@TableName("t_agent_report")
@ApiModel(value="AgentReport对象", description="探针心跳数据")
public class AgentReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "应用id")
    private Long applicationId;

    @ApiModelProperty(value = "应用名")
    private String applicationName;

    private String agentId;

    private String ipAddress;

    @ApiModelProperty(value = "进程号")
    private String progressId;

    @ApiModelProperty(value = "agent版本号")
    private String agentVersion;

    @ApiModelProperty(value = "simulator版本")
    private String simulatorVersion;

    @ApiModelProperty(value = "升级批次 根据升级内容生成MD5")
    private String curUpgradeBatch;

    @ApiModelProperty(value = "节点状态 0:未知,1:启动中,2:升级待重启,3:运行中,4:异常,5:休眠,6:卸载")
    @TableField("`status`")
    private Integer status;

    @ApiModelProperty(value = "agent的错误信息")
    private String agentErrorInfo;

    @ApiModelProperty(value = "simulator错误信息")
    private String simulatorErrorInfo;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtUpdate;

    @ApiModelProperty(value = "环境标识")
    private String envCode;

    @ApiModelProperty(value = "租户 id, 默认 1")
    private Long tenantId;

    @ApiModelProperty(value = "是否有效 0:有效;1:无效")
    @TableField("IS_DELETED")
    private Integer isDeleted;


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

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getProgressId() {
        return progressId;
    }

    public void setProgressId(String progressId) {
        this.progressId = progressId;
    }

    public String getAgentVersion() {
        return agentVersion;
    }

    public void setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
    }

    public String getSimulatorVersion() {
        return simulatorVersion;
    }

    public void setSimulatorVersion(String simulatorVersion) {
        this.simulatorVersion = simulatorVersion;
    }

    public String getCurUpgradeBatch() {
        return curUpgradeBatch;
    }

    public void setCurUpgradeBatch(String curUpgradeBatch) {
        this.curUpgradeBatch = curUpgradeBatch;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getAgentErrorInfo() {
        return agentErrorInfo;
    }

    public void setAgentErrorInfo(String agentErrorInfo) {
        this.agentErrorInfo = agentErrorInfo;
    }

    public String getSimulatorErrorInfo() {
        return simulatorErrorInfo;
    }

    public void setSimulatorErrorInfo(String simulatorErrorInfo) {
        this.simulatorErrorInfo = simulatorErrorInfo;
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

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString() {
        return "AgentReport{" +
        "id=" + id +
        ", applicationId=" + applicationId +
        ", applicationName=" + applicationName +
        ", agentId=" + agentId +
        ", ipAddress=" + ipAddress +
        ", progressId=" + progressId +
        ", agentVersion=" + agentVersion +
        ", simulatorVersion=" + simulatorVersion +
        ", curUpgradeBatch=" + curUpgradeBatch +
        ", status=" + status +
        ", agentErrorInfo=" + agentErrorInfo +
        ", simulatorErrorInfo=" + simulatorErrorInfo +
        ", gmtCreate=" + gmtCreate +
        ", gmtUpdate=" + gmtUpdate +
        ", envCode=" + envCode +
        ", tenantId=" + tenantId +
        ", isDeleted=" + isDeleted +
        "}";
    }
}
