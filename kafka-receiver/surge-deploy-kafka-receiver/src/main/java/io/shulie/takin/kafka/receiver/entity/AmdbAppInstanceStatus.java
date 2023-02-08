package io.shulie.takin.kafka.receiver.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * 应用实例探针状态表
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-14
 */
@TableName("t_amdb_app_instance_status")
@ApiModel(value="AmdbAppInstanceStatus对象", description="应用实例探针状态表")
public class AmdbAppInstanceStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "实例id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "应用名")
    private String appName;

    @ApiModelProperty(value = "agentId")
    private String agentId;

    @ApiModelProperty(value = "ip")
    private String ip;

    @ApiModelProperty(value = "进程号")
    private String pid;

    @ApiModelProperty(value = "主机名称")
    private String hostname;

    @ApiModelProperty(value = "Agent 语言")
    private String agentLanguage;

    @ApiModelProperty(value = "Agent 版本号")
    private String agentVersion;

    @ApiModelProperty(value = "探针版本")
    private String probeVersion;

    @ApiModelProperty(value = "探针状态(0-已安装,1-未安装,2-安装中,3-卸载中,4-安装失败,5-卸载失败,99-未知状态)")
    private String probeStatus;

    @ApiModelProperty(value = "错误码")
    private String errorCode;

    @ApiModelProperty(value = "错误信息")
    private String errorMsg;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime gmtCreate;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime gmtModify;

    @ApiModelProperty(value = "agent 状态")
    private String agentStatus;

    @ApiModelProperty(value = "jdk版本号")
    private String jdk;

    @ApiModelProperty(value = "jvm参数")
    private String jvmArgs;

    @ApiModelProperty(value = "agent异常日志")
    private String agentErrorMsg;

    @ApiModelProperty(value = "agnet异常code")
    private String agentErrorCode;

    @ApiModelProperty(value = "租户标识")
    private String userAppKey;

    @ApiModelProperty(value = "环境标识")
    private String envCode;

    @ApiModelProperty(value = "用户标识")
    private String userId;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getAgentLanguage() {
        return agentLanguage;
    }

    public void setAgentLanguage(String agentLanguage) {
        this.agentLanguage = agentLanguage;
    }

    public String getAgentVersion() {
        return agentVersion;
    }

    public void setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
    }

    public String getProbeVersion() {
        return probeVersion;
    }

    public void setProbeVersion(String probeVersion) {
        this.probeVersion = probeVersion;
    }

    public String getProbeStatus() {
        return probeStatus;
    }

    public void setProbeStatus(String probeStatus) {
        this.probeStatus = probeStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public LocalDateTime getGmtModify() {
        return gmtModify;
    }

    public void setGmtModify(LocalDateTime gmtModify) {
        this.gmtModify = gmtModify;
    }

    public String getAgentStatus() {
        return agentStatus;
    }

    public void setAgentStatus(String agentStatus) {
        this.agentStatus = agentStatus;
    }

    public String getJdk() {
        return jdk;
    }

    public void setJdk(String jdk) {
        this.jdk = jdk;
    }

    public String getJvmArgs() {
        return jvmArgs;
    }

    public void setJvmArgs(String jvmArgs) {
        this.jvmArgs = jvmArgs;
    }

    public String getAgentErrorMsg() {
        return agentErrorMsg;
    }

    public void setAgentErrorMsg(String agentErrorMsg) {
        this.agentErrorMsg = agentErrorMsg;
    }

    public String getAgentErrorCode() {
        return agentErrorCode;
    }

    public void setAgentErrorCode(String agentErrorCode) {
        this.agentErrorCode = agentErrorCode;
    }

    public String getUserAppKey() {
        return userAppKey;
    }

    public void setUserAppKey(String userAppKey) {
        this.userAppKey = userAppKey;
    }

    public String getEnvCode() {
        return envCode;
    }

    public void setEnvCode(String envCode) {
        this.envCode = envCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "AmdbAppInstanceStatus{" +
        "id=" + id +
        ", appName=" + appName +
        ", agentId=" + agentId +
        ", ip=" + ip +
        ", pid=" + pid +
        ", hostname=" + hostname +
        ", agentLanguage=" + agentLanguage +
        ", agentVersion=" + agentVersion +
        ", probeVersion=" + probeVersion +
        ", probeStatus=" + probeStatus +
        ", errorCode=" + errorCode +
        ", errorMsg=" + errorMsg +
        ", gmtCreate=" + gmtCreate +
        ", gmtModify=" + gmtModify +
        ", agentStatus=" + agentStatus +
        ", jdk=" + jdk +
        ", jvmArgs=" + jvmArgs +
        ", agentErrorMsg=" + agentErrorMsg +
        ", agentErrorCode=" + agentErrorCode +
        ", userAppKey=" + userAppKey +
        ", envCode=" + envCode +
        ", userId=" + userId +
        "}";
    }
}
