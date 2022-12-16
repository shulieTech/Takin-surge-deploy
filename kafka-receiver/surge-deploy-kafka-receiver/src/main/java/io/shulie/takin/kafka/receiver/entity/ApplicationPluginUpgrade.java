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
 * 应用升级单
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@TableName("t_application_plugin_upgrade")
@ApiModel(value="ApplicationPluginUpgrade对象", description="应用升级单")
public class ApplicationPluginUpgrade implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "应用id")
    private Long applicationId;

    @ApiModelProperty(value = "应用名")
    private String applicationName;

    @ApiModelProperty(value = "升级批次 根据升级内容生成MD5")
    private String upgradeBatch;

    @ApiModelProperty(value = "升级内容 格式 {pluginId,pluginId}")
    private String upgradeContext;

    @ApiModelProperty(value = "处理升级对应的agentId")
    private String upgradeAgentId;

    @ApiModelProperty(value = "下载地址")
    private String downloadPath;

    @ApiModelProperty(value = "升级状态 0 未升级 1升级成功 2升级失败 3已回滚")
    private Integer pluginUpgradeStatus;

    @ApiModelProperty(value = "节点数量")
    private Integer nodeNum;

    @ApiModelProperty(value = "升级失败信息")
    private String errorInfo;

    @ApiModelProperty(value = "升级单类型 0 agent上报，1 主动升级")
    @TableField("`type`")
    private Integer type;

    @ApiModelProperty(value = "备注")
    private String remark;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtUpdate;

    @ApiModelProperty(value = "操作人id")
    private Long userId;

    @ApiModelProperty(value = "操作人")
    private String userName;

    @ApiModelProperty(value = "环境标识")
    private String envCode;

    @ApiModelProperty(value = "租户 id, 默认 1")
    private Long tenantId;

    @ApiModelProperty(value = "是否有效 0:有效;1:无效")
    @TableField("IS_DELETED")
    private Integer isDeleted;

    @ApiModelProperty(value = "数据签名")
    private String sign;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getUpgradeBatch() {
        return upgradeBatch;
    }

    public void setUpgradeBatch(String upgradeBatch) {
        this.upgradeBatch = upgradeBatch;
    }

    public String getUpgradeContext() {
        return upgradeContext;
    }

    public void setUpgradeContext(String upgradeContext) {
        this.upgradeContext = upgradeContext;
    }

    public String getUpgradeAgentId() {
        return upgradeAgentId;
    }

    public void setUpgradeAgentId(String upgradeAgentId) {
        this.upgradeAgentId = upgradeAgentId;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public Integer getPluginUpgradeStatus() {
        return pluginUpgradeStatus;
    }

    public void setPluginUpgradeStatus(Integer pluginUpgradeStatus) {
        this.pluginUpgradeStatus = pluginUpgradeStatus;
    }

    public Integer getNodeNum() {
        return nodeNum;
    }

    public void setNodeNum(Integer nodeNum) {
        this.nodeNum = nodeNum;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public LocalDateTime getGmtUpdate() {
        return gmtUpdate;
    }

    public void setGmtUpdate(LocalDateTime gmtUpdate) {
        this.gmtUpdate = gmtUpdate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "ApplicationPluginUpgrade{" +
        "id=" + id +
        ", applicationId=" + applicationId +
        ", applicationName=" + applicationName +
        ", upgradeBatch=" + upgradeBatch +
        ", upgradeContext=" + upgradeContext +
        ", upgradeAgentId=" + upgradeAgentId +
        ", downloadPath=" + downloadPath +
        ", pluginUpgradeStatus=" + pluginUpgradeStatus +
        ", nodeNum=" + nodeNum +
        ", errorInfo=" + errorInfo +
        ", type=" + type +
        ", remark=" + remark +
        ", gmtCreate=" + gmtCreate +
        ", gmtUpdate=" + gmtUpdate +
        ", userId=" + userId +
        ", userName=" + userName +
        ", envCode=" + envCode +
        ", tenantId=" + tenantId +
        ", isDeleted=" + isDeleted +
        ", sign=" + sign +
        "}";
    }
}
