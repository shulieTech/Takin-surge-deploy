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

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;

public class AmdbAppRelationModel {
    String fromAppName;
    String toAppName;

    public AmdbAppRelationModel() {
    }

    public AmdbAppRelationModel(String fromAppName, String toAppName) {
        this.fromAppName = fromAppName;
        this.toAppName = toAppName;
    }

    public static String getCols() {
        return "(from_app_name,to_app_name)";
    }

    public static String getParamCols() {
        return "(?,?)";
    }

    public Object[] getValues() {
        return new Object[]{fromAppName, toAppName};
    }

    public String getFromAppName() {
        return fromAppName;
    }

    public void setFromAppName(String fromAppName) {
        this.fromAppName = fromAppName;
    }

    public String getToAppName() {
        return toAppName;
    }

    public void setToAppName(String toAppName) {
        this.toAppName = toAppName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromAppName, toAppName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AmdbAppRelationModel) {
            if (hashCode() == obj.hashCode()) {
                return true;
            }
        }
        return false;
    }

    public static AmdbAppRelationModel parseFromDataMap(Map<String, Object> dataMap) {
        AmdbAppRelationModel amdbAppRelationModel = new AmdbAppRelationModel();
        try {
            BeanUtils.populate(amdbAppRelationModel, dataMap);
            return amdbAppRelationModel;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
