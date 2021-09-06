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

package io.shulie.surge.data.deploy.pradar.parser.rpc;

import com.google.common.collect.Maps;
import com.pamirs.pradar.log.parser.trace.RpcBased;

import java.util.Map;

/**
 * rpc server
 *
 * @author vincent
 */
public class TraceRpcBasedParser extends RpcServerRpcBasedParser {
    @Override
    public Map<String, Object> fromAppTags(String linkId, RpcBased rpcBased) {
        String extend = extendParse(rpcBased);
        String appName = serverAppNameParse(rpcBased);
        Map<String, Object> tags = Maps.newLinkedHashMap();
        tags.put("linkId", linkId);
        tags.put("appName", appName + "-Virtual");
        tags.put("middlewareName", "virtual");
        tags.put("extend", extend);
        return tags;
    }
}
