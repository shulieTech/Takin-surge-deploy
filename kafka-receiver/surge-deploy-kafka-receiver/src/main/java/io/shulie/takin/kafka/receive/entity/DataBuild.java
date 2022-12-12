package io.shulie.takin.kafka.receive.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * 压测数据构建表
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@TableName("t_data_build")
@ApiModel(value="DataBuild对象", description="压测数据构建表")
public class DataBuild implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "数据构建id")
    @TableId(value = "DATA_BUILD_ID", type = IdType.AUTO)
    private Long dataBuildId;

    @ApiModelProperty(value = "应用id")
    @TableField("APPLICATION_ID")
    private Long applicationId;

    @ApiModelProperty(value = "影子库表结构脚本构建状态(0未启动 1正在执行 2执行成功 3执行失败)")
    @TableField("DDL_BUILD_STATUS")
    private Integer ddlBuildStatus;

    @ApiModelProperty(value = "影子库表结构脚本上一次执行成功时间")
    @TableField("DDL_LAST_SUCCESS_TIME")
    private LocalDateTime ddlLastSuccessTime;

    @ApiModelProperty(value = "缓存预热脚本执行状态")
    @TableField("CACHE_BUILD_STATUS")
    private Integer cacheBuildStatus;

    @ApiModelProperty(value = "缓存预热脚本上一次执行成功时间")
    @TableField("CACHE_LAST_SUCCESS_TIME")
    private LocalDateTime cacheLastSuccessTime;

    @ApiModelProperty(value = "基础数据准备脚本执行状态")
    @TableField("READY_BUILD_STATUS")
    private Integer readyBuildStatus;

    @ApiModelProperty(value = "基础数据准备脚本上一次执行成功时间")
    @TableField("READY_LAST_SUCCESS_TIME")
    private LocalDateTime readyLastSuccessTime;

    @ApiModelProperty(value = "铺底数据脚本执行状态")
    @TableField("BASIC_BUILD_STATUS")
    private Integer basicBuildStatus;

    @ApiModelProperty(value = "铺底数据脚本上一次执行成功时间")
    @TableField("BASIC_LAST_SUCCESS_TIME")
    private LocalDateTime basicLastSuccessTime;

    @ApiModelProperty(value = "数据清理脚本执行状态")
    @TableField("CLEAN_BUILD_STATUS")
    private Integer cleanBuildStatus;

    @ApiModelProperty(value = "数据清理脚本上一次执行成功时间")
    @TableField("CLEAN_LAST_SUCCESS_TIME")
    private LocalDateTime cleanLastSuccessTime;

    @ApiModelProperty(value = "插入时间")
    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "变更时间")
    @TableField("UPDATE_TIME")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "环境code")
    private String envCode;

    @ApiModelProperty(value = "租户id")
    private Long tenantId;


    public Long getDataBuildId() {
        return dataBuildId;
    }

    public void setDataBuildId(Long dataBuildId) {
        this.dataBuildId = dataBuildId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Integer getDdlBuildStatus() {
        return ddlBuildStatus;
    }

    public void setDdlBuildStatus(Integer ddlBuildStatus) {
        this.ddlBuildStatus = ddlBuildStatus;
    }

    public LocalDateTime getDdlLastSuccessTime() {
        return ddlLastSuccessTime;
    }

    public void setDdlLastSuccessTime(LocalDateTime ddlLastSuccessTime) {
        this.ddlLastSuccessTime = ddlLastSuccessTime;
    }

    public Integer getCacheBuildStatus() {
        return cacheBuildStatus;
    }

    public void setCacheBuildStatus(Integer cacheBuildStatus) {
        this.cacheBuildStatus = cacheBuildStatus;
    }

    public LocalDateTime getCacheLastSuccessTime() {
        return cacheLastSuccessTime;
    }

    public void setCacheLastSuccessTime(LocalDateTime cacheLastSuccessTime) {
        this.cacheLastSuccessTime = cacheLastSuccessTime;
    }

    public Integer getReadyBuildStatus() {
        return readyBuildStatus;
    }

    public void setReadyBuildStatus(Integer readyBuildStatus) {
        this.readyBuildStatus = readyBuildStatus;
    }

    public LocalDateTime getReadyLastSuccessTime() {
        return readyLastSuccessTime;
    }

    public void setReadyLastSuccessTime(LocalDateTime readyLastSuccessTime) {
        this.readyLastSuccessTime = readyLastSuccessTime;
    }

    public Integer getBasicBuildStatus() {
        return basicBuildStatus;
    }

    public void setBasicBuildStatus(Integer basicBuildStatus) {
        this.basicBuildStatus = basicBuildStatus;
    }

    public LocalDateTime getBasicLastSuccessTime() {
        return basicLastSuccessTime;
    }

    public void setBasicLastSuccessTime(LocalDateTime basicLastSuccessTime) {
        this.basicLastSuccessTime = basicLastSuccessTime;
    }

    public Integer getCleanBuildStatus() {
        return cleanBuildStatus;
    }

    public void setCleanBuildStatus(Integer cleanBuildStatus) {
        this.cleanBuildStatus = cleanBuildStatus;
    }

    public LocalDateTime getCleanLastSuccessTime() {
        return cleanLastSuccessTime;
    }

    public void setCleanLastSuccessTime(LocalDateTime cleanLastSuccessTime) {
        this.cleanLastSuccessTime = cleanLastSuccessTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
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
        return "DataBuild{" +
        "dataBuildId=" + dataBuildId +
        ", applicationId=" + applicationId +
        ", ddlBuildStatus=" + ddlBuildStatus +
        ", ddlLastSuccessTime=" + ddlLastSuccessTime +
        ", cacheBuildStatus=" + cacheBuildStatus +
        ", cacheLastSuccessTime=" + cacheLastSuccessTime +
        ", readyBuildStatus=" + readyBuildStatus +
        ", readyLastSuccessTime=" + readyLastSuccessTime +
        ", basicBuildStatus=" + basicBuildStatus +
        ", basicLastSuccessTime=" + basicLastSuccessTime +
        ", cleanBuildStatus=" + cleanBuildStatus +
        ", cleanLastSuccessTime=" + cleanLastSuccessTime +
        ", createTime=" + createTime +
        ", updateTime=" + updateTime +
        ", envCode=" + envCode +
        ", tenantId=" + tenantId +
        "}";
    }
}
