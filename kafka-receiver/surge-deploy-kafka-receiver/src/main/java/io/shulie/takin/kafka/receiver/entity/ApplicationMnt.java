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
 * 应用管理表
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@TableName("t_application_mnt")
@ApiModel(value="ApplicationMnt对象", description="应用管理表")
public class ApplicationMnt implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "应用id")
    @TableField("APPLICATION_ID")
    private Long applicationId;

    @ApiModelProperty(value = "应用名称")
    @TableField("APPLICATION_NAME")
    private String applicationName;

    @ApiModelProperty(value = "应用说明")
    @TableField("APPLICATION_DESC")
    private String applicationDesc;

    @ApiModelProperty(value = "影子库表结构脚本路径")
    @TableField("DDL_SCRIPT_PATH")
    private String ddlScriptPath;

    @ApiModelProperty(value = "数据清理脚本路径")
    @TableField("CLEAN_SCRIPT_PATH")
    private String cleanScriptPath;

    @ApiModelProperty(value = "基础数据准备脚本路径")
    @TableField("READY_SCRIPT_PATH")
    private String readyScriptPath;

    @ApiModelProperty(value = "铺底数据脚本路径")
    @TableField("BASIC_SCRIPT_PATH")
    private String basicScriptPath;

    @ApiModelProperty(value = "缓存预热脚本地址")
    @TableField("CACHE_SCRIPT_PATH")
    private String cacheScriptPath;

    @ApiModelProperty(value = "缓存失效时间(单位秒)")
    @TableField("CACHE_EXP_TIME")
    private Long cacheExpTime;

    @ApiModelProperty(value = "是否可用(0表示启用,1表示未启用)")
    @TableField("USE_YN")
    private Integer useYn;

    @ApiModelProperty(value = "java agent版本")
    @TableField("AGENT_VERSION")
    private String agentVersion;

    @ApiModelProperty(value = "节点数量")
    @TableField("NODE_NUM")
    private Integer nodeNum;

    @ApiModelProperty(value = "接入状态； 0：正常 ； 1；待配置 ；2：待检测 ; 3：异常")
    @TableField("ACCESS_STATUS")
    private Integer accessStatus;

    @TableField("SWITCH_STATUS")
    private String switchStatus;

    @ApiModelProperty(value = "接入异常信息")
    @TableField("EXCEPTION_INFO")
    private String exceptionInfo;

    @ApiModelProperty(value = "创建时间")
    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "修改时间")
    @TableField("UPDATE_TIME")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "告警人")
    @TableField("ALARM_PERSON")
    private String alarmPerson;

    @ApiModelProperty(value = "pradarAgent版本")
    @TableField("PRADAR_VERSION")
    private String pradarVersion;

    @ApiModelProperty(value = "租户id ,废弃")
    private Long customerId;

    @ApiModelProperty(value = "所属用户")
    @TableField("USER_ID")
    private Long userId;

    @ApiModelProperty(value = "环境code")
    private String envCode;

    @ApiModelProperty(value = "租户id")
    private Long tenantId;

    @ApiModelProperty(value = "部门id")
    private Long deptId;

    @ApiModelProperty(value = "集群名称")
    private String clusterName;


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

    public String getApplicationDesc() {
        return applicationDesc;
    }

    public void setApplicationDesc(String applicationDesc) {
        this.applicationDesc = applicationDesc;
    }

    public String getDdlScriptPath() {
        return ddlScriptPath;
    }

    public void setDdlScriptPath(String ddlScriptPath) {
        this.ddlScriptPath = ddlScriptPath;
    }

    public String getCleanScriptPath() {
        return cleanScriptPath;
    }

    public void setCleanScriptPath(String cleanScriptPath) {
        this.cleanScriptPath = cleanScriptPath;
    }

    public String getReadyScriptPath() {
        return readyScriptPath;
    }

    public void setReadyScriptPath(String readyScriptPath) {
        this.readyScriptPath = readyScriptPath;
    }

    public String getBasicScriptPath() {
        return basicScriptPath;
    }

    public void setBasicScriptPath(String basicScriptPath) {
        this.basicScriptPath = basicScriptPath;
    }

    public String getCacheScriptPath() {
        return cacheScriptPath;
    }

    public void setCacheScriptPath(String cacheScriptPath) {
        this.cacheScriptPath = cacheScriptPath;
    }

    public Long getCacheExpTime() {
        return cacheExpTime;
    }

    public void setCacheExpTime(Long cacheExpTime) {
        this.cacheExpTime = cacheExpTime;
    }

    public Integer getUseYn() {
        return useYn;
    }

    public void setUseYn(Integer useYn) {
        this.useYn = useYn;
    }

    public String getAgentVersion() {
        return agentVersion;
    }

    public void setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
    }

    public Integer getNodeNum() {
        return nodeNum;
    }

    public void setNodeNum(Integer nodeNum) {
        this.nodeNum = nodeNum;
    }

    public Integer getAccessStatus() {
        return accessStatus;
    }

    public void setAccessStatus(Integer accessStatus) {
        this.accessStatus = accessStatus;
    }

    public String getSwitchStatus() {
        return switchStatus;
    }

    public void setSwitchStatus(String switchStatus) {
        this.switchStatus = switchStatus;
    }

    public String getExceptionInfo() {
        return exceptionInfo;
    }

    public void setExceptionInfo(String exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
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

    public String getAlarmPerson() {
        return alarmPerson;
    }

    public void setAlarmPerson(String alarmPerson) {
        this.alarmPerson = alarmPerson;
    }

    public String getPradarVersion() {
        return pradarVersion;
    }

    public void setPradarVersion(String pradarVersion) {
        this.pradarVersion = pradarVersion;
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

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @Override
    public String toString() {
        return "ApplicationMnt{" +
        "id=" + id +
        ", applicationId=" + applicationId +
        ", applicationName=" + applicationName +
        ", applicationDesc=" + applicationDesc +
        ", ddlScriptPath=" + ddlScriptPath +
        ", cleanScriptPath=" + cleanScriptPath +
        ", readyScriptPath=" + readyScriptPath +
        ", basicScriptPath=" + basicScriptPath +
        ", cacheScriptPath=" + cacheScriptPath +
        ", cacheExpTime=" + cacheExpTime +
        ", useYn=" + useYn +
        ", agentVersion=" + agentVersion +
        ", nodeNum=" + nodeNum +
        ", accessStatus=" + accessStatus +
        ", switchStatus=" + switchStatus +
        ", exceptionInfo=" + exceptionInfo +
        ", createTime=" + createTime +
        ", updateTime=" + updateTime +
        ", alarmPerson=" + alarmPerson +
        ", pradarVersion=" + pradarVersion +
        ", customerId=" + customerId +
        ", userId=" + userId +
        ", envCode=" + envCode +
        ", tenantId=" + tenantId +
        ", deptId=" + deptId +
        ", clusterName=" + clusterName +
        "}";
    }
}
