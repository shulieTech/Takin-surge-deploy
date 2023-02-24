package io.shulie.takin.kafka.receiver.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * 任务
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@TableName("t_pressure")
@ApiModel(value="Pressure对象", description="任务")
public class Pressure implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "任务名称")
    @TableField("`name`")
    private String name;

    @ApiModelProperty(value = "资源主键")
    private Long resourceId;

    @ApiModelProperty(value = "持续时间")
    private Long duration;

    @ApiModelProperty(value = "采样率")
    private Integer sampling;

    @ApiModelProperty(value = "任务的运行模式")
    @TableField("`type`")
    private Integer type;

    @ApiModelProperty(value = "启动选项")
    private String startOption;

    @ApiModelProperty(value = "状态回调接口路径")
    private String callbackUrl;

    @ApiModelProperty(value = "资源实例数量")
    private Integer resourceExampleNumber;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Integer getSampling() {
        return sampling;
    }

    public void setSampling(Integer sampling) {
        this.sampling = sampling;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getStartOption() {
        return startOption;
    }

    public void setStartOption(String startOption) {
        this.startOption = startOption;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public Integer getResourceExampleNumber() {
        return resourceExampleNumber;
    }

    public void setResourceExampleNumber(Integer resourceExampleNumber) {
        this.resourceExampleNumber = resourceExampleNumber;
    }

    @Override
    public String toString() {
        return "Pressure{" +
        "id=" + id +
        ", name=" + name +
        ", resourceId=" + resourceId +
        ", duration=" + duration +
        ", sampling=" + sampling +
        ", type=" + type +
        ", startOption=" + startOption +
        ", callbackUrl=" + callbackUrl +
        ", resourceExampleNumber=" + resourceExampleNumber +
        "}";
    }
}
