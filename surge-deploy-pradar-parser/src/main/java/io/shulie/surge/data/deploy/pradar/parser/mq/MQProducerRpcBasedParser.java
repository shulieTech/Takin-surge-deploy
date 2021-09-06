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

import java.util.Map;

/**
 * MQ
 *
 * @author vincent
 */
public class MQProducerRpcBasedParser extends DefaultRpcBasedParser {
    /**
     * @param rpcBased
     * @return
     */
    @Override
    public String appNameParse(RpcBased rpcBased) {
        String addr = rpcBased.getRemoteIp();
        String port = rpcBased.getPort();
        String dbType = rpcBased.getMiddlewareName();
        String appName = dbType + "" + addr + ":" + port;
        if (StringUtils.isNotBlank(addr) && addr.contains(":")) {
            appName = dbType + "" + addr;
        }
        if ("ons".equalsIgnoreCase(rpcBased.getMiddlewareName())) {
            String methodName = rpcBased.getMethodName();
            appName = "";
            if (StringUtils.isNotBlank(methodName)) {
                appName = methodName.substring(0, methodName.indexOf(":"));
                if (methodName.contains("%")) {
                    appName = methodName.substring(0, methodName.indexOf("%"));
                }
            }
        }
        return appName;
    }

    /**
     * @param rpcBased
     * @return
     */
    @Override
    public String serverAppNameParse(RpcBased rpcBased) {
        return super.appNameParse(rpcBased);
    }

    /**
     * 方法解析器
     *
     * @param rpcBased
     * @return
     */
    @Override
    public String methodParse(RpcBased rpcBased) {
        if ("ons".equalsIgnoreCase(rpcBased.getMiddlewareName())) {
            String methodTmp = rpcBased.getMethodName();
            String methodName = "";
            if (StringUtils.isNotBlank(methodTmp)) {
                if (methodTmp.contains("%")) {
                    methodName = methodTmp.substring(methodTmp.indexOf("%") + 1);
                }
                if (methodName.contains(":")) {
                    methodName = methodName.substring(0, methodName.indexOf(":"));
                }
            }
            return methodName;
        }
        String methodName = rpcBased.getMethodName();
        if (methodName.contains(":")) {
            methodName = methodName.substring(0, methodName.indexOf(":"));
            return methodName;
        }
        return super.methodParse(rpcBased);
    }

    @Override
    public Map<String, Object> toAppTags(String linkId, RpcBased rpcBased) {
        String extend = extendParse(rpcBased);
        String appName = appNameParse(rpcBased);
        String middlewareName = rpcBased.getMiddlewareName();

        Map<String, Object> tags = Maps.newLinkedHashMap();
        tags.put("linkId", linkId);
        tags.put("appName", appName);
        tags.put("middlewareName", middlewareName);
        tags.put("extend", extend);

        return tags;
    }
}
