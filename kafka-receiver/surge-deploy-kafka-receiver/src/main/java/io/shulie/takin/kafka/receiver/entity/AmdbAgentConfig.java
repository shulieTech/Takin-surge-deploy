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
 * agent动态配置表
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-14
 */
@TableName("t_amdb_agent_config")
@ApiModel(value="AmdbAgentConfig对象", description="agent动态配置表")
public class AmdbAgentConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "应用名称")
    private String appName;

    @ApiModelProperty(value = "agent id")
    private String agentId;

    @ApiModelProperty(value = "配置key")
    private String configKey;

    @ApiModelProperty(value = "配置value")
    private String configValue;

    @ApiModelProperty(value = "配置校验结果 ture 校验成功 false校验失败")
    @TableField("`status`")
    private Boolean status;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime gmtCreate;

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

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
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
        return "AmdbAgentConfig{" +
        "id=" + id +
        ", appName=" + appName +
        ", agentId=" + agentId +
        ", configKey=" + configKey +
        ", configValue=" + configValue +
        ", status=" + status +
        ", gmtCreate=" + gmtCreate +
        ", userAppKey=" + userAppKey +
        ", envCode=" + envCode +
        ", userId=" + userId +
        "}";
    }
}
