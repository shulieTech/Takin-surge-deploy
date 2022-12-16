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
 * 配置表-服务的配置
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@TableName("t_config_server")
@ApiModel(value="ConfigServer对象", description="配置表-服务的配置")
public class ConfigServer implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "配置的 key")
    @TableField("`key`")
    private String key;

    @ApiModelProperty(value = "配置的值")
    @TableField("`value`")
    private String value;

    @ApiModelProperty(value = "租户id, -99 表示无")
    private Long tenantId;

    @ApiModelProperty(value = "环境")
    private String envCode;

    @ApiModelProperty(value = "租户key")
    private String tenantAppKey;

    @ApiModelProperty(value = "是否是住户使用, 1 是, 0 否")
    private Integer isTenant;

    @ApiModelProperty(value = "是否是全局的, 1 是, 0 否")
    private Integer isGlobal;

    @ApiModelProperty(value = "归属版本, 1 开源版, 2 企业版, 6 开源版和企业版")
    private Integer edition;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime gmtCreate;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime gmtUpdate;

    @ApiModelProperty(value = "逻辑删除字段, 0 未删除, 1 已删除")
    private Integer isDeleted;

    @ApiModelProperty(value = "用户id")
    private Long userId;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getEnvCode() {
        return envCode;
    }

    public void setEnvCode(String envCode) {
        this.envCode = envCode;
    }

    public String getTenantAppKey() {
        return tenantAppKey;
    }

    public void setTenantAppKey(String tenantAppKey) {
        this.tenantAppKey = tenantAppKey;
    }

    public Integer getIsTenant() {
        return isTenant;
    }

    public void setIsTenant(Integer isTenant) {
        this.isTenant = isTenant;
    }

    public Integer getIsGlobal() {
        return isGlobal;
    }

    public void setIsGlobal(Integer isGlobal) {
        this.isGlobal = isGlobal;
    }

    public Integer getEdition() {
        return edition;
    }

    public void setEdition(Integer edition) {
        this.edition = edition;
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

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "ConfigServer{" +
        "id=" + id +
        ", key=" + key +
        ", value=" + value +
        ", tenantId=" + tenantId +
        ", envCode=" + envCode +
        ", tenantAppKey=" + tenantAppKey +
        ", isTenant=" + isTenant +
        ", isGlobal=" + isGlobal +
        ", edition=" + edition +
        ", gmtCreate=" + gmtCreate +
        ", gmtUpdate=" + gmtUpdate +
        ", isDeleted=" + isDeleted +
        ", userId=" + userId +
        "}";
    }
}
