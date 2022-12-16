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
 * 
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-14
 */
@TableName("t_amdb_app")
@ApiModel(value="AmdbApp对象", description="")
public class AmdbApp implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "应用ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "应用名称")
    private String appName;

    @ApiModelProperty(value = "应用负责人")
    private String appManager;

    @ApiModelProperty(value = "工程名称")
    private String projectName;

    @ApiModelProperty(value = "工程版本")
    private String projectVersion;

    @ApiModelProperty(value = "git地址")
    private String gitUrl;

    @ApiModelProperty(value = "发布包名称")
    private String publishPackageName;

    @ApiModelProperty(value = "工程子模块")
    private String projectSubmoudle;

    @ApiModelProperty(value = "异常信息")
    private String exceptionInfo;

    @ApiModelProperty(value = "应用说明")
    private String remark;

    @ApiModelProperty(value = "扩展字段")
    private String ext;

    @ApiModelProperty(value = "标记位")
    private Integer flag;

    @ApiModelProperty(value = "创建人编码")
    private String creator;

    @ApiModelProperty(value = "创建人名称")
    private String creatorName;

    @ApiModelProperty(value = "更新人编码")
    private String modifier;

    @ApiModelProperty(value = "更新人名称")
    private String modifierName;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime gmtCreate;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime gmtModify;

    @ApiModelProperty(value = "应用负责人名称")
    private String appManagerName;

    @ApiModelProperty(value = "租户标示")
    private String tenant;

    @ApiModelProperty(value = "应用类型")
    private String appType;

    @ApiModelProperty(value = "应用类型名称")
    private String appTypeName;

    @ApiModelProperty(value = "租户标识")
    private String userAppKey;

    @ApiModelProperty(value = "环境标识")
    private String envCode;

    @ApiModelProperty(value = "用户标识")
    private String userId;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppManager() {
        return appManager;
    }

    public void setAppManager(String appManager) {
        this.appManager = appManager;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public String getPublishPackageName() {
        return publishPackageName;
    }

    public void setPublishPackageName(String publishPackageName) {
        this.publishPackageName = publishPackageName;
    }

    public String getProjectSubmoudle() {
        return projectSubmoudle;
    }

    public void setProjectSubmoudle(String projectSubmoudle) {
        this.projectSubmoudle = projectSubmoudle;
    }

    public String getExceptionInfo() {
        return exceptionInfo;
    }

    public void setExceptionInfo(String exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getModifierName() {
        return modifierName;
    }

    public void setModifierName(String modifierName) {
        this.modifierName = modifierName;
    }

    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public LocalDateTime getGmtModify() {
        return gmtModify;
    }

    public void setGmtModify(LocalDateTime gmtModify) {
        this.gmtModify = gmtModify;
    }

    public String getAppManagerName() {
        return appManagerName;
    }

    public void setAppManagerName(String appManagerName) {
        this.appManagerName = appManagerName;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getAppTypeName() {
        return appTypeName;
    }

    public void setAppTypeName(String appTypeName) {
        this.appTypeName = appTypeName;
    }

    public String getUserAppKey() {
        return userAppKey;
    }

    public void setUserAppKey(String userAppKey) {
        this.userAppKey = userAppKey;
    }

    public String getEnvCode() {
        return envCode;
    }

    public void setEnvCode(String envCode) {
        this.envCode = envCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "AmdbApp{" +
        "id=" + id +
        ", appName=" + appName +
        ", appManager=" + appManager +
        ", projectName=" + projectName +
        ", projectVersion=" + projectVersion +
        ", gitUrl=" + gitUrl +
        ", publishPackageName=" + publishPackageName +
        ", projectSubmoudle=" + projectSubmoudle +
        ", exceptionInfo=" + exceptionInfo +
        ", remark=" + remark +
        ", ext=" + ext +
        ", flag=" + flag +
        ", creator=" + creator +
        ", creatorName=" + creatorName +
        ", modifier=" + modifier +
        ", modifierName=" + modifierName +
        ", gmtCreate=" + gmtCreate +
        ", gmtModify=" + gmtModify +
        ", appManagerName=" + appManagerName +
        ", tenant=" + tenant +
        ", appType=" + appType +
        ", appTypeName=" + appTypeName +
        ", userAppKey=" + userAppKey +
        ", envCode=" + envCode +
        ", userId=" + userId +
        "}";
    }
}
