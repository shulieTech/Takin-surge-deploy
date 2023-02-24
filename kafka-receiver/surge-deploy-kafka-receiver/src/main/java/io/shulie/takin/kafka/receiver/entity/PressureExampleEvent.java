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
 * 任务实例事件
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@TableName("t_pressure_example_event")
@ApiModel(value="PressureExampleEvent对象", description="任务实例事件")
public class PressureExampleEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "任务实例主键")
    private Long pressureExampleId;

    @ApiModelProperty(value = "事件类型")
    @TableField("`type`")
    private Integer type;

    @ApiModelProperty(value = "事件内容")
    private String context;

    @ApiModelProperty(value = "时间")
    private LocalDateTime time;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPressureExampleId() {
        return pressureExampleId;
    }

    public void setPressureExampleId(Long pressureExampleId) {
        this.pressureExampleId = pressureExampleId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "PressureExampleEvent{" +
        "id=" + id +
        ", pressureExampleId=" + pressureExampleId +
        ", type=" + type +
        ", context=" + context +
        ", time=" + time +
        "}";
    }
}
