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
 * dubbo和job接口上传收集表
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@TableName("t_upload_interface_data")
@ApiModel(value="UploadInterfaceData对象", description="dubbo和job接口上传收集表")
public class UploadInterfaceData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "抽数表id")
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "APP名")
    @TableField("APP_NAME")
    private String appName;

    @ApiModelProperty(value = "接口值")
    @TableField("INTERFACE_VALUE")
    private String interfaceValue;

    @ApiModelProperty(value = "上传数据类型 查看字典  暂时 1 dubbo 2 job")
    @TableField("INTERFACE_TYPE")
    private Integer interfaceType;

    @ApiModelProperty(value = "插入时间")
    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "租户id")
    private Long tenantId;

    @ApiModelProperty(value = "环境变量")
    private String envCode;


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

    public String getInterfaceValue() {
        return interfaceValue;
    }

    public void setInterfaceValue(String interfaceValue) {
        this.interfaceValue = interfaceValue;
    }

    public Integer getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(Integer interfaceType) {
        this.interfaceType = interfaceType;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
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

    @Override
    public String toString() {
        return "UploadInterfaceData{" +
        "id=" + id +
        ", appName=" + appName +
        ", interfaceValue=" + interfaceValue +
        ", interfaceType=" + interfaceType +
        ", createTime=" + createTime +
        ", tenantId=" + tenantId +
        ", envCode=" + envCode +
        "}";
    }
}
