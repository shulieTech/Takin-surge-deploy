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

import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.deploy.pradar.parser.DefaultRpcBasedParser;
import io.shulie.surge.data.deploy.pradar.parser.MiddlewareType;
import io.shulie.surge.data.runtime.common.utils.ApiProcessor;

/**
 * rpc client
 *
 * @author sunshiyu
 */
public class RpcClientRpcBasedParser extends DefaultRpcBasedParser {

    /**
     * 服务解析器
     *
     * @param rpcBased
     * @return
     */
    @Override
    public String serviceParse(RpcBased rpcBased) {
        //对于下游接不了探针的远程调用,如oss,第三方接口,由于接不了探针,可以在入口规则处配置客户端应用的出口规则,也可用于适配
        if ((rpcBased.getRpcType() == MiddlewareType.TYPE_WEB_SERVER) || (rpcBased.getRpcType() == MiddlewareType.TYPE_RPC && (rpcBased.getServiceName() != null && rpcBased.getServiceName().contains("/")))) {
            String formatUrl = ApiProcessor.merge(rpcBased.getUserAppKey() + "#" + rpcBased.getEnvCode() + "#" + rpcBased.getAppName(), rpcBased.getServiceName(), rpcBased.getMethodName());
            return formatUrl;
        }
        return super.serviceParse(rpcBased);
    }
}
