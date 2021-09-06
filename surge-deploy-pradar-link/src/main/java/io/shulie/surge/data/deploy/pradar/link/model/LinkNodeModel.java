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

public class LinkNodeModel {
    String linkId;
    String appName;
    String traceAppName;
    String middlewareName;
    String extend;
    String appId;

    public static String getCols() {
        return "(link_id,app_name,trace_app_name,middleware_name,extend,app_id,gmt_modify)";
    }

    public static String getParamCols() {
        return "(?,?,?,?,?,?,now())";
    }

    public static String getOnDuplicateCols() {
        return "ON DUPLICATE KEY UPDATE app_name=VALUES(app_name),trace_app_name=VALUES(trace_app_name),middleware_name=VALUES(middleware_name),extend=VALUES(extend),gmt_modify=VALUES(gmt_modify)";
    }

    public Object[] getValues() {
        return new Object[]{linkId, appName, traceAppName, middlewareName, extend, appId};
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
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

    public String getMiddlewareName() {
        return middlewareName;
    }

    public void setMiddlewareName(String middlewareName) {
        this.middlewareName = middlewareName;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Override
    public int hashCode() {
        return (linkId + appId).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof LinkNodeModel) {
            if (hashCode() == obj.hashCode()) {
                return true;
            }
        }
        return false;
    }

    public static LinkNodeModel parseFromDataMap(Map<String, Object> dataMap) {
        LinkNodeModel linkNodeModel = new LinkNodeModel();
        try {
            BeanUtils.populate(linkNodeModel, dataMap);
            return linkNodeModel;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
