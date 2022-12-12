package io.shulie.takin.kafka.receive.entity;

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
@TableName("t_performance_thread_stack_data")
@ApiModel(value="PerformanceThreadStackData对象", description="")
public class PerformanceThreadStackData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "线程堆栈")
    private String threadStack;

    @ApiModelProperty(value = "influxDB关联")
    @TableId(value = "thread_stack_link", type = IdType.AUTO)
    private Long threadStackLink;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime gmtCreate;

    @ApiModelProperty(value = "环境code")
    private String envCode;

    @ApiModelProperty(value = "租户id")
    private Long tenantId;


    public String getThreadStack() {
        return threadStack;
    }

    public void setThreadStack(String threadStack) {
        this.threadStack = threadStack;
    }

    public Long getThreadStackLink() {
        return threadStackLink;
    }

    public void setThreadStackLink(Long threadStackLink) {
        this.threadStackLink = threadStackLink;
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
        return "PerformanceThreadStackData{" +
        "threadStack=" + threadStack +
        ", threadStackLink=" + threadStackLink +
        ", gmtCreate=" + gmtCreate +
        ", envCode=" + envCode +
        ", tenantId=" + tenantId +
        "}";
    }
}
