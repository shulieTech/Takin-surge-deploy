package io.shulie.takin.kafka.receiver.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * 任务实例
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@TableName("t_pressure_example")
@ApiModel(value="PressureExample对象", description="任务实例")
public class PressureExample implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "资源实例主键")
    private Long resourceExampleId;

    @ApiModelProperty(value = "序列号")
    private Integer number;

    @ApiModelProperty(value = "持续时长(毫秒)")
    private Long duration;

    @ApiModelProperty(value = "任务主键")
    private Long pressureId;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResourceExampleId() {
        return resourceExampleId;
    }

    public void setResourceExampleId(Long resourceExampleId) {
        this.resourceExampleId = resourceExampleId;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getPressureId() {
        return pressureId;
    }

    public void setPressureId(Long pressureId) {
        this.pressureId = pressureId;
    }

    @Override
    public String toString() {
        return "PressureExample{" +
        "id=" + id +
        ", resourceExampleId=" + resourceExampleId +
        ", number=" + number +
        ", duration=" + duration +
        ", pressureId=" + pressureId +
        "}";
    }
}
