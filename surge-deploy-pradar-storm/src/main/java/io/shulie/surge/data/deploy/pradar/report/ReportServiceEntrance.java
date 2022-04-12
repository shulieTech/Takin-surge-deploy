/*
 * Copyright 2021 Shulie Technology, Co.Ltd
 * Email: shulie@shulie.io
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.shulie.surge.data.deploy.pradar.report;

public class ReportServiceEntrance extends ReportActivityEntrance {

    private String entranceAppName;
    private String entranceServiceName;
    private String entranceMethodName;
    private String entranceRpcType;

    public ReportServiceEntrance() {
    }

    public ReportServiceEntrance(String reportId, String appName, String serviceName, String methodName, String rpcType) {
        super(reportId, appName, serviceName, methodName, rpcType);
    }

    public String getEntranceAppName() {
        return entranceAppName;
    }

    public void setEntranceAppName(String entranceAppName) {
        this.entranceAppName = entranceAppName;
    }

    public String getEntranceServiceName() {
        return entranceServiceName;
    }

    public void setEntranceServiceName(String entranceServiceName) {
        this.entranceServiceName = entranceServiceName;
    }

    public String getEntranceMethodName() {
        return entranceMethodName;
    }

    public void setEntranceMethodName(String entranceMethodName) {
        this.entranceMethodName = entranceMethodName;
    }

    public String getEntranceRpcType() {
        return entranceRpcType;
    }

    public void setEntranceRpcType(String entranceRpcType) {
        this.entranceRpcType = entranceRpcType;
    }

    public String hashKey() {
        return parenHashKey() + "@" + super.hashKey();
    }

    public String parenHashKey() {
        return entranceAppName + "@" + entranceServiceName + "@" + entranceMethodName + "@" + entranceRpcType;
    }
}
