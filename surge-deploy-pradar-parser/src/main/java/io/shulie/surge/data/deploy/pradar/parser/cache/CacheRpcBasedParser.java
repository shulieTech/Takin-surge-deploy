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

package io.shulie.surge.data.deploy.pradar.parser.cache;

import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.deploy.pradar.parser.db.DBClientRpcBasedParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * cache
 *
 * @author vincent
 */
public class CacheRpcBasedParser extends DBClientRpcBasedParser {
    /**
     * 方法解析器
     *
     * @param rpcBased
     * @return
     */
    @Override
    public String methodParse(RpcBased rpcBased) {
        //如果中间件类型是redis,则从serviceName中解析方法名称,其他缓存如google-guava则还是取其methodname
        if ("redis".equals(rpcBased.getMiddlewareName())) {
            String serviceName = rpcBased.getServiceName();
            if (StringUtils.isNotBlank(serviceName) && serviceName.contains(":")) {
                // 旧版本的日志的库名跟方法是放到一起的，按:分开解析出来
                return serviceName.substring(serviceName.indexOf(":") + 1);
            }
            //这里不能直接取methodname,agent出现过把对象地址打印到methodName的情况,导致redis边爆增
            /**
             * ─serviceName─┬─methodName──┬─middlewareName─┬─parsedMiddlewareName─┐
             * │ del         │ [[B@9f2e43b │ redis          │ REDIS                │
             */
            return serviceName;
        }
        return rpcBased.getMethodName();
    }

    @Override
    public String serviceParse(RpcBased rpcBased) {
        String serviceName = rpcBased.getServiceName();
        if ("redis".equals(rpcBased.getMiddlewareName())) {
            // 1、旧版本的日志的库名跟方法是放到一起的，按:分开解析出来
//            if (StringUtils.isNotBlank(serviceName) && serviceName.contains(":")) {
//                return serviceName.substring(0, serviceName.indexOf(":"));
//            }
            // 2、新版本没有库名了。默认返回0
            // 库名现在统一给0
            return "0";
        }
        //如果是其他缓存,类似google-guava,则取实际的serviceName;
        return serviceName;
    }

    /**
     * @param rpcBased
     * @return
     */
    @Override
    public String appNameParse(RpcBased rpcBased) {
        String addr = rpcBased.getRemoteIp();
        int port = NumberUtils.toInt(rpcBased.getPort(), 0);
        String dbType = rpcBased.getMiddlewareName();
        String serviceName = serviceParse(rpcBased);
        String appName = dbType + " " + addr + ":" + port + ":" + serviceName;
        // 本地缓存的这类。没有remoteIp。直接返回dbType
        if (StringUtils.isBlank(addr)) {
            return dbType;
        }
        return appName;
    }
}
