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
 * 资源实例表
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@TableName("t_resource_example")
@ApiModel(value="ResourceExample对象", description="资源实例表")
public class ResourceExample implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "资源主键")
    private Long resourceId;

    @ApiModelProperty(value = "调度器主键")
    private Long watchmanId;

    @ApiModelProperty(value = "需要的CPU")
    private String cpu;

    @ApiModelProperty(value = "需要的内存")
    private String memory;

    @ApiModelProperty(value = "限定的CPU")
    private String limitCpu;

    @ApiModelProperty(value = "限定的内存")
    private String limitMemory;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "资源镜像信息")
    private String image;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getWatchmanId() {
        return watchmanId;
    }

    public void setWatchmanId(Long watchmanId) {
        this.watchmanId = watchmanId;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getLimitCpu() {
        return limitCpu;
    }

    public void setLimitCpu(String limitCpu) {
        this.limitCpu = limitCpu;
    }

    public String getLimitMemory() {
        return limitMemory;
    }

    public void setLimitMemory(String limitMemory) {
        this.limitMemory = limitMemory;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "ResourceExample{" +
        "id=" + id +
        ", resourceId=" + resourceId +
        ", watchmanId=" + watchmanId +
        ", cpu=" + cpu +
        ", memory=" + memory +
        ", limitCpu=" + limitCpu +
        ", limitMemory=" + limitMemory +
        ", createTime=" + createTime +
        ", image=" + image +
        "}";
    }
}
