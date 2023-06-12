package io.shulie.surge.data.suppliers.grpc.remoting;

/**
 * 中间件类型枚举类
 * @author vincent
 */
public enum MiddlewareType {
    //web服务器
    TYPE_WEB_SERVER(0), //httpclient类型
    TYPE_RPC(1),//RPC类型
    TYPE_HTTP_CLIENT(2), //httpclient类型
    TYPE_MQ(3),//MQ类型
    TYPE_DB(4),//DB类型
    TYPE_CACHE(5),//CACHE类型
    TYPE_SEARCH(6), //搜索引擎
    TYPE_JOB(7), //JOB类型
    TYPE_FS(8),//文件系统
    TYPE_LOCAL(9),  //本地方法
    TYPE_CUSTOMER(10), //用户自定义切点
    TYPE_UNKNOW(-1);

    private int type;

    MiddlewareType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    /**
     * 根据类型转换对象
     * @param type 类型
     * @return 中间件类型
     */
    public static MiddlewareType valueOf(int type) {
        MiddlewareType middlewareType = MiddlewareType.TYPE_UNKNOW;
        switch (type) {
            case 0:
                middlewareType = TYPE_WEB_SERVER;
                break;
            case 1:
                middlewareType = TYPE_RPC;
                break;
            case 2:
                middlewareType = TYPE_HTTP_CLIENT;
                break;
            case 3:
                middlewareType = TYPE_MQ;
                break;
            case 4:
                middlewareType = TYPE_DB;
                break;
            case 5:
                middlewareType = TYPE_CACHE;
                break;
            case 6:
                middlewareType = TYPE_SEARCH;
                break;
            case 7:
                middlewareType = TYPE_JOB;
                break;
            case 8:
                middlewareType = TYPE_FS;
                break;
            case 9:
                middlewareType = TYPE_LOCAL;
                break;
            case 10:
                middlewareType = TYPE_CUSTOMER;
                break;
            default:
                middlewareType = TYPE_UNKNOW;
                break;
        }
        return middlewareType;
    }
}
