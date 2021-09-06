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

package io.shulie.surge.data.deploy.pradar.link.model;

import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Map;

public class LinkEdgeModel {
    String linkId;
    String service;
    String method;
    String extend;
    String appName;
    String traceAppName;
    String serverAppName;
    String rpcType;
    String logType;
    String middlewareName;
    String entranceId;
    String fromAppId;
    String toAppId;
    String edgeId;

    public static String getCols() {
        return "(link_id,service,method,extend,app_name,trace_app_name,server_app_name,rpc_type,log_type,middleware_name,entrance_id,from_app_id,to_app_id,edge_id,gmt_modify)";
    }

    public static String getParamCols() {
        return "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,now())";
    }

    public static String getOnDuplicateCols() {
        return " ON DUPLICATE KEY UPDATE service=VALUES(service),method=VALUES(method),extend=VALUES(extend),app_name=VALUES(app_name),trace_app_name=VALUES(trace_app_name),server_app_name=VALUES(server_app_name),rpc_type=VALUES(rpc_type),log_type=VALUES(log_type),middleware_name=VALUES(middleware_name),entrance_id=VALUES(entrance_id),from_app_id=VALUES(from_app_id),to_app_id=VALUES(to_app_id),gmt_modify=VALUES(gmt_modify)";
    }

    public Object[] getValues() {
        return new Object[]{linkId, service, method, extend, appName, traceAppName, serverAppName, rpcType, logType, middlewareName, entranceId, fromAppId, toAppId, edgeId};
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTraceAppName() {
        return traceAppName;
    }

    public void setTraceAppName(String traceAppName) {
        this.traceAppName = traceAppName;
    }

    public String getServerAppName() {
        return serverAppName;
    }

    public void setServerAppName(String serverAppName) {
        this.serverAppName = serverAppName;
    }

    public String getRpcType() {
        return rpcType;
    }

    public void setRpcType(String rpcType) {
        this.rpcType = rpcType;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getMiddlewareName() {
        return middlewareName;
    }

    public void setMiddlewareName(String middlewareName) {
        this.middlewareName = middlewareName;
    }

    public String getEntranceId() {
        return entranceId;
    }

    public void setEntranceId(String entranceId) {
        this.entranceId = entranceId;
    }

    public String getFromAppId() {
        return fromAppId;
    }

    public void setFromAppId(String fromAppId) {
        this.fromAppId = fromAppId;
    }

    public String getToAppId() {
        return toAppId;
    }

    public void setToAppId(String toAppId) {
        this.toAppId = toAppId;
    }

    public String getEdgeId() {
        return edgeId;
    }

    public void setEdgeId(String edgeId) {
        this.edgeId = edgeId;
    }

    @Override
    public int hashCode() {
        return (linkId + edgeId).hashCode();
    }

    public boolean equals(Object obj) {
        if (obj != null && obj instanceof LinkEdgeModel) {
            if (hashCode() == obj.hashCode()) {
                return true;
            }
        }
        return false;
    }

    public static LinkEdgeModel parseFromDataMap(Map<String, Object> dataMap) {
        LinkEdgeModel linkEdgeModel = new LinkEdgeModel();
        try {
            BeanUtils.populate(linkEdgeModel, dataMap);
            return linkEdgeModel;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
