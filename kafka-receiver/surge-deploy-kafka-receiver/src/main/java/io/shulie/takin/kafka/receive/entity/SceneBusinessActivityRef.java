package io.shulie.takin.kafka.receive.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
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
@TableName("t_scene_business_activity_ref")
@ApiModel(value="SceneBusinessActivityRef对象", description="")
public class SceneBusinessActivityRef implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "场景ID")
    private Long sceneId;

    @ApiModelProperty(value = "业务活动id")
    private Long businessActivityId;

    @ApiModelProperty(value = "业务活动名称")
    private String businessActivityName;

    @ApiModelProperty(value = "关联应用id，多个用,隔开")
    private String applicationIds;

    @ApiModelProperty(value = "目标值，json")
    private String goalValue;

    private String bindRef;

    private Integer isDeleted;

    private LocalDateTime createTime;

    private String createName;

    private LocalDateTime updateTime;

    private String updateName;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSceneId() {
        return sceneId;
    }

    public void setSceneId(Long sceneId) {
        this.sceneId = sceneId;
    }

    public Long getBusinessActivityId() {
        return businessActivityId;
    }

    public void setBusinessActivityId(Long businessActivityId) {
        this.businessActivityId = businessActivityId;
    }

    public String getBusinessActivityName() {
        return businessActivityName;
    }

    public void setBusinessActivityName(String businessActivityName) {
        this.businessActivityName = businessActivityName;
    }

    public String getApplicationIds() {
        return applicationIds;
    }

    public void setApplicationIds(String applicationIds) {
        this.applicationIds = applicationIds;
    }

    public String getGoalValue() {
        return goalValue;
    }

    public void setGoalValue(String goalValue) {
        this.goalValue = goalValue;
    }

    public String getBindRef() {
        return bindRef;
    }

    public void setBindRef(String bindRef) {
        this.bindRef = bindRef;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getCreateName() {
        return createName;
    }

    public void setCreateName(String createName) {
        this.createName = createName;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateName() {
        return updateName;
    }

    public void setUpdateName(String updateName) {
        this.updateName = updateName;
    }

    @Override
    public String toString() {
        return "SceneBusinessActivityRef{" +
        "id=" + id +
        ", sceneId=" + sceneId +
        ", businessActivityId=" + businessActivityId +
        ", businessActivityName=" + businessActivityName +
        ", applicationIds=" + applicationIds +
        ", goalValue=" + goalValue +
        ", bindRef=" + bindRef +
        ", isDeleted=" + isDeleted +
        ", createTime=" + createTime +
        ", createName=" + createName +
        ", updateTime=" + updateTime +
        ", updateName=" + updateName +
        "}";
    }
}
