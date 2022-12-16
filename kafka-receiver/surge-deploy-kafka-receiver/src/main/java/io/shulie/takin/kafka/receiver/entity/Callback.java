package io.shulie.takin.kafka.receiver.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.sql.Blob;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * 回调表
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@TableName("t_callback")
@ApiModel(value="Callback对象", description="回调表")
public class Callback implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "类型")
    @TableField("`type`")
    private Integer type;

    @ApiModelProperty(value = "回调路径")
    private String url;

    @ApiModelProperty(value = "回调内容")
    private byte[] context;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "是否完成")
    private Boolean completed;

    @ApiModelProperty(value = "阈值时间")
    private LocalDateTime thresholdTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public byte[] getContext() {
        return context;
    }

    public void setContext(byte[] context) {
        this.context = context;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getThresholdTime() {
        return thresholdTime;
    }

    public void setThresholdTime(LocalDateTime thresholdTime) {
        this.thresholdTime = thresholdTime;
    }

    @Override
    public String toString() {
        return "Callback{" +
        "id=" + id +
        ", type=" + type +
        ", url=" + url +
        ", context=" + context +
        ", createTime=" + createTime +
        ", completed=" + completed +
        ", thresholdTime=" + thresholdTime +
        "}";
    }
}
