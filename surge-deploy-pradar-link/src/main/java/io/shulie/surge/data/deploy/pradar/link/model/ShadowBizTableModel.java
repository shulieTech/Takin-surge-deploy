package io.shulie.surge.data.deploy.pradar.link.model;

public class ShadowBizTableModel {
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

    public static String getCols() {
        return "(app_name,data_source,biz_database,table_user,table_name,unique_key,gmt_create,gmt_modify)";
    }

    public static String getParamCols() {
        return "(?,?,?,?,?,?,now(),now())";
    }

    public Object[] getValues() {
        return new Object[] {appName, dataSource, bizDatabase, tableUser, tableName, uniqueKey};
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

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }
}
