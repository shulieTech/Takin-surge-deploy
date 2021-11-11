package io.shulie.surge.data.deploy.pradar.link.constants;

/**
 * @author sunshiyu
 * @description
 * @datetime 2021-08-31 11:52 下午
 */
public class SqlConstants {
    public static final String BRACKETS_LEFT = "(";
    public static final String BRACKETS_RIGHT = ")";
    public static final String UNION_ALL = "union all ";

    //入口查询
    //入口日志无需保存upAppName,只有服务端日志需要保存upAppName
    public static final String QUERY_ENTRANCE_SQL = "select distinct parsedServiceName as serviceName,appName,rpcType,parsedMethod as methodName,parsedMiddlewareName,parsedExtend as extend,'0' as linkType,case when logType = '3' then upAppName else '' end as upAppName,middlewareName as middlewareDetail,'' as downAppName,'' as defaultWhiteInfo from %s where ";

    //出口查询,下面三个
    public static final String QUERY_EXIT_SQL = "select distinct parsedServiceName as serviceName,appName,rpcType,parsedMethod as methodName,parsedMiddlewareName,parsedExtend as extend,'1' as linkType,'' as upAppName,middlewareName as middlewareDetail,'' as downAppName,'' as defaultWhiteInfo from %s where ";
    public static final String QUERY_DEFAULT_WHITE_SQL = "select distinct parsedServiceName as serviceName,appName,rpcType,parsedMethod as methodName,parsedMiddlewareName,parsedExtend as extend,'1' as linkType,'' as upAppName,middlewareName as middlewareDetail,'' as downAppName,flagMessage as defaultWhiteInfo from %s where ";
    //这里不能查询parsedService和method,如果服务端匹配上了入口规则,则会导致误匹配
    public static final String QUERY_ALL_SQL = "select distinct serviceName,appName,rpcType,methodName,parsedMiddlewareName,parsedExtend as extend,case when logType = 3 then 'server' else 'client' end as linkType,upAppName,middlewareName as middlewareDetail,'' as downAppName,parsedServiceName as defaultWhiteInfo from %s where ";
}
