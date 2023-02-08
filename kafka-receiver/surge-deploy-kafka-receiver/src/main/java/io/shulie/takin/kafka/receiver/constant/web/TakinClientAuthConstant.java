package io.shulie.takin.kafka.receiver.constant.web;

/**
 * @Author: fanxx
 * @Date: 2020/11/10 10:24 上午
 * @Description:
 */
public interface TakinClientAuthConstant {
    Boolean isExpire = Boolean.FALSE;

    /**
     * 万能验证码
     */
    String TOKEN = "OfOqAty85zpyuSNc";

    /**
     * 登录token
     */
    String HEADER_X_TOKEN = "x-token";

    String HEADER_X_USER_TOKEN = "x-user-type";
    String HEADER_X_EXPIRE = "x-expire";

    /**
     * userAppKey agent传递 老版本
     */
    String APP_KEY_HEADER_KEY = "userAppKey";

    /**
     * tenantAppKey agent传递 新版本
     */
    String TENANT_APP_KEY_HEADER_KEY = "tenantAppKey";

    /**
     * 租户code
     */
    String HEADER_TENANT_CODE = "tenant-code";

    /**
     * 环境code
     */
    String HEADER_ENV_CODE = "env-code";

    /**
     * agent的环境code
     */
    String AGENT_HEADER_ENV_CODE = "envCode";

    /**
     * agent拓展字段
     */
    String AGENT_HEADER_EXPAND = "agentExpand";

    /**
     * 用户id
     */
    String HEADER_USER_ID = "user-id";

    /**
     * agent用户id
     */
    String AGENT_HEADER_USER_ID = "userId";

    /**
     * 用户key
     */
    String KEY_USER_MAP_REDIS_KEY = "takin:web:key2user:%s";

    /**
     * 大盘
     */
    String DASHBOARD_MENU =  "dashboard";

    /**
     * 系统信息
     */
    String SYSTEM_INFO =  "system_info";

    /**
     * 权限管理path
     */
    String AUTH_MANAGE_PATH = "/authorityManage";

    /**
     * 部门级别名称菜单，租户自有菜单path前缀
     */
    String DEPT_AUTH_PARH_PRO = "/configCenter/authTree/";

    /**
     * 部门权限key的前缀
     */
    String DEPT_AUTH_KEY_PRO = "configCenter_authTree_";

    /**
     * 部门层级列表uri
     */
    String DEPT_LEVEL_LIST_URI = "[\"/api/dept/level\"]";

    /**
     * 部门层级列表操作按钮,不做操作权限设置
     */
    String DEPT_LEVEL_LIST_ACTION = "[]";

    /**
     * 当前是联通使用的token，可以从中解析出来用户信息
     */
    String HEADER_ACCESS_TOKEN = "accessToken";

    /**
     * 最高级部门code
     */
    String HEADER_DEPT_HIGHEST = "deptHighest";

    /**
     * 最低级部门code
     */
    String HEADER_DEPT_LOWEST = "deptLowest";

    /**
     * 第三方id
     */
    String HEADER_EXTERNAL_ID = "externalId";
    /**
     */
    String SOURCE =  "SOURCE";

}
