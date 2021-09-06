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

import io.shulie.surge.data.deploy.pradar.parser.utils.Md5Utils;
import org.apache.commons.beanutils.BeanUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LinkEntranceModel {
    String entranceId;
    String appName;
    String serviceName;
    String methodName;
    String middlewareName;
    Integer rpcType;
    String extend;
    Date gmtModify;
    String linkType;
    String upAppName;

    public static String getCols() {
        return "(entrance_id,app_name,service_name,method_name,middleware_name,rpc_type,extend,link_type,up_app_name,gmt_modify)";
    }

    public static String getParamCols() {
        return "(?,?,?,?,?,?,?,?,?,now())";
    }

    public Object[] getValues() {
        return new Object[]{entranceId, appName, serviceName, methodName, middlewareName, rpcType, extend, linkType, upAppName};
    }

    public static String getOnDuplicateCols() {
        return "ON DUPLICATE KEY UPDATE service_name=VALUES(service_name),method_name=VALUES(method_name),middleware_name=VALUES(middleware_name),link_type=VALUES(link_type),up_app_name=VALUES(up_app_name),gmt_modify=VALUES(gmt_modify)";
    }

    public static LinkEntranceModel parseFromDataMap(Map<String, Object> dataMap) {
        LinkEntranceModel linkEntranceModel = new LinkEntranceModel();
        try {
            BeanUtils.populate(linkEntranceModel, dataMap);

            StringBuilder sb = new StringBuilder();
            List<String> calConditionList = Arrays.asList(new String[]{"serviceName", "appName", "rpcType", "methodName", "middlewareName", "extend", "linkType", "upAppName"});

            //如果是出口日志,并且中间件类型是HTTP,不区分methodName,相同path的接口只保存一条
            if ("1".equals(dataMap.get("linkType")) && "HTTP".equals(dataMap.get("middlewareName"))) {
                calConditionList = Arrays.asList(new String[]{"serviceName", "appName", "rpcType", "middlewareName", "extend", "linkType"});
            }

            for (String key : calConditionList) {
                sb.append(dataMap.get(key)).append('|');
            }
            sb.deleteCharAt(sb.length() - 1);
            linkEntranceModel.setEntranceId(Md5Utils.md5(sb.toString()));
            linkEntranceModel.setGmtModify(new Date());
            return linkEntranceModel;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getEntranceId() {
        return entranceId;
    }

    public void setEntranceId(String entranceId) {
        this.entranceId = entranceId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMiddlewareName() {
        return middlewareName;
    }

    public void setMiddlewareName(String middlewareName) {
        this.middlewareName = middlewareName;
    }

    public Integer getRpcType() {
        return rpcType;
    }

    public void setRpcType(Integer rpcType) {
        this.rpcType = rpcType;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public String getUpAppName() {
        return upAppName;
    }

    public void setUpAppName(String upAppName) {
        this.upAppName = upAppName;
    }

    public Date getGmtModify() {
        return new Date();
    }

    public void setGmtModify(Date gmtModify) {
        this.gmtModify = gmtModify;
    }

    @Override
    public String toString() {
        return "LinkEntranceModel{" +
                "entranceId='" + entranceId + '\'' +
                ", appName='" + appName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", middlewareName='" + middlewareName + '\'' +
                ", rpcType=" + rpcType +
                ", extend='" + extend + '\'' +
                ", linkType='" + linkType + '\'' +
                ", upAppName='" + upAppName + '\'' +
                '}';
    }
}
