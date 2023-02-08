package io.shulie.takin.kafka.receiver.entity;

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
 * 链路检测表
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@TableName("t_link_detection")
@ApiModel(value="LinkDetection对象", description="链路检测表")
public class LinkDetection implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键id")
    @TableId(value = "LINK_DETECTION_ID", type = IdType.AUTO)
    private Long linkDetectionId;

    @ApiModelProperty(value = "应用id")
    @TableField("APPLICATION_ID")
    private Long applicationId;

    @ApiModelProperty(value = "影子库整体同步检测状态(0未启用,1正在检测,2检测成功,3检测失败)")
    @TableField("SHADOWLIB_CHECK")
    private Integer shadowlibCheck;

    @ApiModelProperty(value = "影子库检测失败内容")
    @TableField("SHADOWLIB_ERROR")
    private String shadowlibError;

    @ApiModelProperty(value = "缓存预热校验状态(0未启用,1正在检测,2检测成功,3检测失败)")
    @TableField("CACHE_CHECK")
    private Integer cacheCheck;

    @ApiModelProperty(value = "缓存预热实时检测失败内容")
    @TableField("CACHE_ERROR")
    private String cacheError;

    @ApiModelProperty(value = "白名单校验状态(0未启用,1正在检测,2检测成功,3检测失败)")
    @TableField("WLIST_CHECK")
    private Integer wlistCheck;

    @ApiModelProperty(value = "白名单异常错误信息")
    @TableField("WLIST_ERROR")
    private String wlistError;

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


    public Long getLinkDetectionId() {
        return linkDetectionId;
    }

    public void setLinkDetectionId(Long linkDetectionId) {
        this.linkDetectionId = linkDetectionId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Integer getShadowlibCheck() {
        return shadowlibCheck;
    }

    public void setShadowlibCheck(Integer shadowlibCheck) {
        this.shadowlibCheck = shadowlibCheck;
    }

    public String getShadowlibError() {
        return shadowlibError;
    }

    public void setShadowlibError(String shadowlibError) {
        this.shadowlibError = shadowlibError;
    }

    public Integer getCacheCheck() {
        return cacheCheck;
    }

    public void setCacheCheck(Integer cacheCheck) {
        this.cacheCheck = cacheCheck;
    }

    public String getCacheError() {
        return cacheError;
    }

    public void setCacheError(String cacheError) {
        this.cacheError = cacheError;
    }

    public Integer getWlistCheck() {
        return wlistCheck;
    }

    public void setWlistCheck(Integer wlistCheck) {
        this.wlistCheck = wlistCheck;
    }

    public String getWlistError() {
        return wlistError;
    }

    public void setWlistError(String wlistError) {
        this.wlistError = wlistError;
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
        return "LinkDetection{" +
        "linkDetectionId=" + linkDetectionId +
        ", applicationId=" + applicationId +
        ", shadowlibCheck=" + shadowlibCheck +
        ", shadowlibError=" + shadowlibError +
        ", cacheCheck=" + cacheCheck +
        ", cacheError=" + cacheError +
        ", wlistCheck=" + wlistCheck +
        ", wlistError=" + wlistError +
        ", createTime=" + createTime +
        ", updateTime=" + updateTime +
        ", envCode=" + envCode +
        ", tenantId=" + tenantId +
        "}";
    }
}
