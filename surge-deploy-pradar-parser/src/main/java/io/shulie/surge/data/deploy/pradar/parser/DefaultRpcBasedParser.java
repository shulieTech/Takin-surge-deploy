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

package io.shulie.surge.data.deploy.pradar.parser;

import com.google.common.collect.Maps;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.deploy.pradar.parser.utils.Md5Utils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author vincent
 */
public class DefaultRpcBasedParser implements RpcBasedParser {
    @Override
    public String edgeId(String linkId, RpcBased rpcBased) {
        Map<String, Object> map = edgeTags(linkId, rpcBased);
        if (map == null || map.isEmpty()) {
            return "";
        }
        return mapToKey(map);
    }

    /**
     * 边tags
     *
     * @param linkId
     * @param rpcBased
     * @return
     */
    @Override
    public Map<String, Object> edgeTags(String linkId, RpcBased rpcBased) {
        String service = serviceParse(rpcBased);
        String method = methodParse(rpcBased);
        String extend = extendParse(rpcBased);
        String appName = appNameParse(rpcBased);
        String serverAppName = serverAppNameParse(rpcBased);
        Integer rpcType = rpcBased.getRpcType();
        Integer logType = rpcBased.getLogType();
        String middlewareName = rpcBased.getMiddlewareName();
        String entranceId = rpcBased.getEntranceNodeId();
        if (StringUtils.isBlank(entranceId)) {
            entranceId = "";
        }
        Map<String, Object> tags = Maps.newLinkedHashMap();
        tags.put("linkId", linkId);
        tags.put("service", service);
        tags.put("method", method);
        tags.put("extend", extend);
        tags.put("appName", appName);
        tags.put("traceAppName", "");
        tags.put("serverAppName", serverAppName);
        tags.put("rpcType", rpcType);
        tags.put("logType", logType);
        tags.put("middlewareName", middlewareName);
        tags.put("entranceId", entranceId);
        return tags;
    }

    @Override
    public String fromAppId(String linkId, RpcBased rpcBased) {
        return mapToKey(fromAppTags(linkId, rpcBased));
    }

    @Override
    public Map<String, Object> fromAppTags(String linkId, RpcBased rpcBased) {
        String extend = extendParse(rpcBased);
        String appName = serverAppNameParse(rpcBased);
        Map<String, Object> tags = Maps.newLinkedHashMap();
        tags.put("linkId", linkId);
        tags.put("appName", appName);
        tags.put("extend", extend);
        return tags;
    }

    @Override
    public String toAppId(String linkId, RpcBased rpcBased) {
        return mapToKey(toAppTags(linkId, rpcBased));
    }

    @Override
    public Map<String, Object> toAppTags(String linkId, RpcBased rpcBased) {
        String extend = extendParse(rpcBased);
        String appName = appNameParse(rpcBased);
        Map<String, Object> tags = Maps.newLinkedHashMap();
        tags.put("linkId", linkId);
        tags.put("appName", appName);
        tags.put("extend", extend);
        return tags;
    }


    @Override
    public String linkId(RpcBased rpcBased) {
        Map<String, Object> map = linkTags(rpcBased);
        return mapToKey(map);
    }

    @Override
    public String linkId(Map<String, Object> map) {
        return mapToKey(linkTags(map));
    }

    @Override
    public Map<String, Object> linkTags(RpcBased rpcBased) {
        String service = serviceParse(rpcBased);
        String method = methodParse(rpcBased);
        String extend = extendParse(rpcBased);
        String appName = appNameParse(rpcBased);
        Integer rpcType = rpcBased.getRpcType();

        Map<String, Object> tags = Maps.newLinkedHashMap();
        tags.put("service", service);
        tags.put("method", method);
        tags.put("appName", appName);
        tags.put("rpcType", rpcType);
        tags.put("extend", extend);

        return tags;
    }

    @Override
    public Map<String, Object> linkTags(Map<String, Object> map) {
        String service = String.valueOf(map.get("service"));
        String method = String.valueOf(map.get("method"));
        String extend = String.valueOf(map.get("extend"));
        String appName = String.valueOf(map.get("appName"));
        String rpcType = String.valueOf(map.get("rpcType"));

        Map<String, Object> tags = Maps.newLinkedHashMap();
        tags.put("service", service);
        tags.put("method", method);
        tags.put("appName", appName);
        tags.put("rpcType", rpcType);
        tags.put("extend", extend);
        return tags;
    }

    protected String mapToKey(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        for (Object object : map.values()) {
            sb.append(object).append('|');
        }
        sb.deleteCharAt(sb.length() - 1);
        return Md5Utils.md5(sb.toString());
    }

    /**
     * 服务解析器
     *
     * @param rpcBased
     * @return
     */
    @Override
    public String serviceParse(RpcBased rpcBased) {
        return rpcBased.getServiceName();
    }

    /**
     * 方法解析器
     *
     * @return
     */
    @Override
    public String methodParse(RpcBased rpcBased) {
        return rpcBased.getMethodName();
    }

    /**
     * 其他信息解析器
     *
     * @param rpcBased
     */
    @Override
    public String extendParse(RpcBased rpcBased) {
        return "";
    }

    /**
     * @param rpcBased
     * @return
     */
    @Override
    public String appNameParse(RpcBased rpcBased) {
        return rpcBased.getAppName();
    }

    /**
     * @param rpcBased
     * @return
     */
    @Override
    public String serverAppNameParse(RpcBased rpcBased) {
        return rpcBased.getUpAppName();
    }
}
