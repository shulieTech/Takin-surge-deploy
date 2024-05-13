package io.shulie.surge.data.deploy.pradar.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class ResourceModel implements Serializable {
    private Long pressureTime;
    private String resourceType;
    private String resourceName;
    private String resourceUrl;
    private Set<Long> sceneIds;

    public Long getPressureTime() {
        return pressureTime;
    }

    public void setPressureTime(Long pressureTime) {
        this.pressureTime = pressureTime;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public Set<Long> getSceneIds() {
        return sceneIds;
    }

    public void setSceneIds(Set<Long> sceneIds) {
        this.sceneIds = sceneIds;
    }
}
