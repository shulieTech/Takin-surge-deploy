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
    public String serviceParse(RpcBased rpcBased) {
        if (StringUtils.equalsIgnoreCase("ons", rpcBased.getMiddlewareName())) {
            String serviceName = rpcBased.getServiceName();
            serviceName = serviceName.indexOf('%') >= 0 ? StringUtils.split(serviceName, '%')[1] : serviceName;
            return serviceName;
        }
        return super.serviceParse(rpcBased);
    }

    /**
     * @param rpcBased
     * @return
     */
    @Override
    public String appNameParse(RpcBased rpcBased) {
        if (StringUtils.equalsIgnoreCase("ons", rpcBased.getMiddlewareName())) {
            String methodName = rpcBased.getMethodName();
            String appName = "";
            if (StringUtils.isNotBlank(methodName)) {
                appName = methodName.substring(0, methodName.indexOf(':'));
                int indexOf = methodName.indexOf('%');
                if (indexOf >= 0) {
                    appName = methodName.substring(0, indexOf);
                }
            }
            return appName;
        } else {
            String addr = sortAddr(rpcBased.getRemoteIp());
            int port = NumberUtils.toInt(rpcBased.getPort(), 0);
            String dbType = rpcBased.getMiddlewareName();
            String appName = dbType + "" + addr + ":" + port;
            if (StringUtils.isNotBlank(addr) && addr.indexOf(':') >= 0) {
                appName = dbType + "" + addr;
            }
            return appName;
        }
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
        if (StringUtils.equalsIgnoreCase("ons", rpcBased.getMiddlewareName())) {
            String methodTmp = rpcBased.getMethodName();
            String methodName = "";
            if (StringUtils.isNotBlank(methodTmp)) {
                int index = methodTmp.indexOf('%');
                if (index >= 0) {
                    methodName = methodTmp.substring(index + 1);
                }
                int indexOf = methodName.indexOf(':');
                if (indexOf >= 0) {
                    methodName = methodName.substring(0, indexOf);
                }
            }
            return methodName;
        }
        String methodName = rpcBased.getMethodName();
        int indexOf = methodName.indexOf(':');
        if (indexOf >= 0) {
            methodName = methodName.substring(0, indexOf);
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
