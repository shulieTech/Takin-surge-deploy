package io.shulie.surge.data.deploy.pradar.link.util;

import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.deploy.pradar.parser.MiddlewareType;
import io.shulie.surge.data.deploy.pradar.parser.PradarLogType;
import io.shulie.surge.data.runtime.common.utils.ApiProcessor;

/**
 * @author xingchen
 * @description: TODO
 * @date 2022/9/16 5:17 PM
 */
public class ServiceUtils {
    public static boolean isHttpClient(RpcBased rpcBased) {
        return rpcBased.getLogType() == PradarLogType.LOG_TYPE_RPC_CLIENT
                && rpcBased.getRpcType() == MiddlewareType.TYPE_WEB_SERVER
                && !rpcBased.getMiddlewareName().toUpperCase().contains("FEIGN");
    }

    public static boolean isRpcClient(RpcBased rpcBased) {
        return rpcBased.getLogType() == PradarLogType.LOG_TYPE_RPC_CLIENT
                && rpcBased.getRpcType() == MiddlewareType.TYPE_RPC
                && !rpcBased.getServiceName().toLowerCase().contains("oss")
                && !rpcBased.getMiddlewareName().toUpperCase().contains("FEIGN");
    }

    public static String formatService(String serviceName, int rpcType) {
        if (rpcType == MiddlewareType.TYPE_WEB_SERVER) {
            return ApiProcessor.parsePath(serviceName);
        }
        return serviceName;
    }
}
