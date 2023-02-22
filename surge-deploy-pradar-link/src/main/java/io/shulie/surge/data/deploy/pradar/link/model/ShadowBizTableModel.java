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

public class ShadowBizTableModel extends LinkPublicModel {
    // 应用名称
    private String appName;
    // 业务数据源
    private String dataSource;
    // 业务数据库
    private String bizDatabase;
    // 数据源用户名称
    private String tableUser;
    // 业务表名称
    private String tableName;
    // 唯一键(md5(appName,dataSource,tableUser,tableUser))
    private String uniqueKey;

    private int canRead;

    private int canWrite;

    public static String getCols() {
        return "(app_name,data_source,biz_database,table_user,table_name,unique_key,gmt_create,gmt_modify,user_app_key,env_code)";
    }

    public static String getParamCols() {
        return "(?,?,?,?,?,?,now(),now(),?,?)";
    }

    public Object[] getValues() {
        return new Object[] {appName, dataSource, bizDatabase, tableUser, tableName, uniqueKey, getUserAppKey(), getEnvCode()};
    }

    // 唯一索引
    public String generateUniqueIndex() {
        return getAppName() + "#" + getDataSource() + "#" + getTableUser() + "#" + getTableName();
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getBizDatabase() {
        return bizDatabase;
    }

    public void setBizDatabase(String bizDatabase) {
        this.bizDatabase = bizDatabase;
    }

    public String getTableUser() {
        return tableUser;
    }

    public void setTableUser(String tableUser) {
        this.tableUser = tableUser;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getCanRead() {
        return canRead;
    }

    public void setCanRead(int canRead) {
        this.canRead = canRead;
    }

    public int getCanWrite() {
        return canWrite;
    }

    public void setCanWrite(int canWrite) {
        this.canWrite = canWrite;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }
}
