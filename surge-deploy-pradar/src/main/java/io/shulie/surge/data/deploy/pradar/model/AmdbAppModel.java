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

package io.shulie.surge.data.deploy.pradar.model;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;

public class AmdbAppModel {
    String appName;
    String appType;

    public AmdbAppModel() {
    }

    public AmdbAppModel(String appName, String appType) {
        this.appName = appName;
        this.appType = appType;
    }

    public static String getCols() {
        return "(app_name,app_type)";
    }

    public static String getParamCols() {
        return "(?,?)";
    }

    public Object[] getValues() {
        return new Object[]{appName, this.getAppType()};
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppType() {
        if (StringUtils.isBlank(this.appType)) {
            return "APP";
        }
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, appType);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AmdbAppModel) {
            if (hashCode() == obj.hashCode()) {
                return true;
            }
        }
        return false;
    }

    public static AmdbAppModel parseFromDataMap(Map<String, Object> dataMap) {
        AmdbAppModel appModel = new AmdbAppModel();
        try {
            BeanUtils.populate(appModel, dataMap);
            return appModel;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
