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
@TableName("t_amdb_app_instance")
@ApiModel(value="AmdbAppInstance对象", description="")
public class AmdbAppInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "实例id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String appName;

    @ApiModelProperty(value = "应用ID")
    private Long appId;

    @ApiModelProperty(value = "agentId")
    private String agentId;

    @ApiModelProperty(value = "ip")
    private String ip;

    @ApiModelProperty(value = "进程号")
    private String pid;

    @ApiModelProperty(value = "Agent 版本号")
    private String agentVersion;

    @ApiModelProperty(value = "MD5")
    private String md5;

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

    private String agentLanguage;

    @ApiModelProperty(value = "主机名称")
    private String hostname;

    @ApiModelProperty(value = "租户标示")
    private String tenant;

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

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getAgentVersion() {
        return agentVersion;
    }

    public void setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
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

    public String getAgentLanguage() {
        return agentLanguage;
    }

    public void setAgentLanguage(String agentLanguage) {
        this.agentLanguage = agentLanguage;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
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
        return "AmdbAppInstance{" +
        "id=" + id +
        ", appName=" + appName +
        ", appId=" + appId +
        ", agentId=" + agentId +
        ", ip=" + ip +
        ", pid=" + pid +
        ", agentVersion=" + agentVersion +
        ", md5=" + md5 +
        ", ext=" + ext +
        ", flag=" + flag +
        ", creator=" + creator +
        ", creatorName=" + creatorName +
        ", modifier=" + modifier +
        ", modifierName=" + modifierName +
        ", gmtCreate=" + gmtCreate +
        ", gmtModify=" + gmtModify +
        ", agentLanguage=" + agentLanguage +
        ", hostname=" + hostname +
        ", tenant=" + tenant +
        ", userAppKey=" + userAppKey +
        ", envCode=" + envCode +
        ", userId=" + userId +
        "}";
    }
}
