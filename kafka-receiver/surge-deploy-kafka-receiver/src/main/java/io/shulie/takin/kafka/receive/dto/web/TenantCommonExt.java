package io.shulie.takin.kafka.receive.dto.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author by: hezhongqi
 * @Package io.shulie.takin.web.ext.entity.tenant
 * @ClassName: TenantCommonExt
 * @Date: 2021/9/28 10:00
 */
public class TenantCommonExt {
    private Long tenantId;

    private String tenantAppKey;

    private String envCode;

    private String tenantCode;

    /**
     * 记录来源 哪里赋值
     */
    private Integer source;

    private Long userId;

    private Long deptId;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantAppKey() {
        return tenantAppKey;
    }

    public void setTenantAppKey(String tenantAppKey) {
        this.tenantAppKey = tenantAppKey;
    }

    public String getEnvCode() {
        return envCode;
    }

    public void setEnvCode(String envCode) {
        this.envCode = envCode;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }
}
