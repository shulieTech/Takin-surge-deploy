package io.shulie.surge.data.deploy.pradar.link.model;

public class LinkPublicModel {
    /**
     * userAppKey
     */
    private String userAppKey;

    /**
     * tenantCode
     */
    private String tenantCode;

    /**
     * envCode
     */
    private String envCode;

    /**
     * userId
     */
    private String userId;

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
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
        return "LinkPublicModel{" +
                "userAppKey='" + userAppKey + '\'' +
                ", envCode='" + envCode + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
