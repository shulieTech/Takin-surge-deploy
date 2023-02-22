package io.shulie.takin.kafka.receiver.dto.web;

import lombok.Data;

@Data
public class ApplicationDsWarnVO {

    private String agentId;
    private String appName;
    private int checkInterval;
    private long checkTime;
    private String configKey;
    private String configType;
    private String envCode;
    private String errorMsg;
    private String hostIp;
    private boolean success;
    private String tenantCode;

}
