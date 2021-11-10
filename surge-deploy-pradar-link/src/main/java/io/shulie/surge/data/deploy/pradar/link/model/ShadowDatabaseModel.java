package io.shulie.surge.data.deploy.pradar.link.model;

public class ShadowDatabaseModel {
    // 应用名称
    private String appName;
    // 业务数据源
    private String dataSource;
    // 影子数据源
    private String shadowDataSource;
    //  数据库名称
    private String dbName;
    // 数据源用户名称
    private String tableUser;
    // 用户密码
    private String password;
    // 中间件类型
    private String middlewareType;
    // 连接池名称
    private String connectionPool;
    // 附加信息
    private String extInfo;
    // 类型
    private String type;
    // 原始携带配置
    private String attachment;
    // 唯一键(md5(appName, dataSource,tableUser))
    private String uniqueKey;

    public static String getCols() {
        return "(app_name,type,data_source,shadow_data_source,db_name,table_user,password,middleware_type,connection_pool,ext_info,attachment,"
            + "unique_key,gmt_create,gmt_modify)";
    }

    public static String getParamCols() {
        return "(?,?,?,?,?,?,?,?,?,?,?,?,now(),now())";
    }

    public Object[] getValues() {
        return new Object[] {appName, type, dataSource, shadowDataSource, dbName, getTableUser(), password, middlewareType, connectionPool,
            extInfo, attachment, uniqueKey};
    }

    // 唯一索引
    public String generateUniqueIndex() {
        return getAppName() + "#" + getDataSource() + "#" + getTableUser() + "#" + getType();
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

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTableUser() {
        return tableUser == null ? "" : tableUser;
    }

    public void setTableUser(String tableUser) {
        this.tableUser = tableUser;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMiddlewareType() {
        return middlewareType;
    }

    public void setMiddlewareType(String middlewareType) {
        this.middlewareType = middlewareType;
    }

    public String getConnectionPool() {
        return connectionPool;
    }

    public void setConnectionPool(String connectionPool) {
        this.connectionPool = connectionPool;
    }

    public String getExtInfo() {
        return extInfo;
    }

    public void setExtInfo(String extInfo) {
        this.extInfo = extInfo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public String getShadowDataSource() {
        return shadowDataSource;
    }

    public void setShadowDataSource(String shadowDataSource) {
        this.shadowDataSource = shadowDataSource;
    }
}
