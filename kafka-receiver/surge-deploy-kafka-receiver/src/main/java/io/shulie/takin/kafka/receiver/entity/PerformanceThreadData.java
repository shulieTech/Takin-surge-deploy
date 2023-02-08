package io.shulie.takin.kafka.receiver.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
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
@TableName("t_performance_thread_data")
@ApiModel(value="PerformanceThreadData对象", description="")
public class PerformanceThreadData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "agent_id")
    private String agentId;

    @ApiModelProperty(value = "app_name")
    private String appName;

    @ApiModelProperty(value = "timestamp")
    private String timestamp;

    @ApiModelProperty(value = "app_ip")
    private String appIp;

    @ApiModelProperty(value = "线程栈数据")
    private String threadData;

    @ApiModelProperty(value = "base_thread_关联关系")
    @TableId(value = "base_id", type = IdType.AUTO)
    private Long baseId;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime gmtCreate;

    @ApiModelProperty(value = "环境code")
    private String envCode;

    @ApiModelProperty(value = "租户id")
    private Long tenantId;


    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getAppIp() {
        return appIp;
    }

    public void setAppIp(String appIp) {
        this.appIp = appIp;
    }

    public String getThreadData() {
        return threadData;
    }

    public void setThreadData(String threadData) {
        this.threadData = threadData;
    }

    public Long getBaseId() {
        return baseId;
    }

    public void setBaseId(Long baseId) {
        this.baseId = baseId;
    }

    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
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
        return "PerformanceThreadData{" +
        "agentId=" + agentId +
        ", appName=" + appName +
        ", timestamp=" + timestamp +
        ", appIp=" + appIp +
        ", threadData=" + threadData +
        ", baseId=" + baseId +
        ", gmtCreate=" + gmtCreate +
        ", envCode=" + envCode +
        ", tenantId=" + tenantId +
        "}";
    }
}
