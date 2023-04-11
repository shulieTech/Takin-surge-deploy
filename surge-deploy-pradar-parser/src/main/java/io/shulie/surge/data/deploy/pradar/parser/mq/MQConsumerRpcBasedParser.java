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

package io.shulie.surge.data.deploy.pradar.parser.mq;

import com.google.common.collect.Maps;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.deploy.pradar.parser.DefaultRpcBasedParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Map;

/**
 * @author vincent
 */
public class MQConsumerRpcBasedParser extends DefaultRpcBasedParser {
    /**
     * @param rpcBased
     * @return
     */
    @Override
    public String serverAppNameParse(RpcBased rpcBased) {
        String addr = sortAddr(rpcBased.getRemoteIp());
        int port = NumberUtils.toInt(rpcBased.getPort(), 0);
        String dbType = rpcBased.getMiddlewareName();
        String serverAppName = dbType + "" + addr + ":" + port;
        if (StringUtils.isNotBlank(addr) && addr.contains(":")) {
            serverAppName = dbType + "" + addr;
        }
        if ("ons".equalsIgnoreCase(rpcBased.getMiddlewareName())) {
            String methodName = rpcBased.getMethodName();
            serverAppName = "";
            if (StringUtils.isNotBlank(methodName)) {
                if (methodName.contains("%")) {
                    serverAppName = methodName.substring(0, methodName.indexOf("%"));
                }
                if (serverAppName.contains(":")) {
                    serverAppName = serverAppName.substring(serverAppName.indexOf(":") + 1);
                }
            }
        }
        return serverAppName;
    }

    /**
     * 服务解析器
     *
     * @param rpcBased
     * @return
     */
    @Override
    public String serviceParse(RpcBased rpcBased) {
        return super.serviceParse(rpcBased);
    }

    /**
     * 方法解析器
     *
     * @return
     */
    @Override
    public String methodParse(RpcBased rpcBased) {
        if (StringUtils.equalsIgnoreCase("ons", rpcBased.getMiddlewareName())) {
            String methodName = rpcBased.getMethodName();
            String methodTmp = methodName;
            if (StringUtils.isNotBlank(methodTmp)) {
                int indexOf = methodTmp.indexOf('%');
                if (indexOf >= 0) {
                    //修复bug
                    methodTmp = methodTmp.substring(indexOf + 1);
                }
                int index = methodTmp.indexOf(':');
                if (index >= 0) {
                    methodTmp = methodTmp.substring(0, index);
                }
            }
            return "".equals(methodTmp) ? methodName : methodTmp;
        }
        return super.methodParse(rpcBased);
    }

    @Override
    public Map<String, Object> fromAppTags(String linkId, RpcBased rpcBased) {
        String extend = extendParse(rpcBased);
        String appName = serverAppNameParse(rpcBased);
        Map<String, Object> tags = Maps.newLinkedHashMap();
        tags.put("linkId", linkId);
        tags.put("appName", appName);
        tags.put("middlewareName", rpcBased.getMiddlewareName());
        tags.put("extend", extend);
        return tags;
    }
}
