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
        //针对 restful 接口,如果rpcType = 1的时候,里面也含有http的日志,也需要做url格式化,否则在出入口处理的时候distinct将会不生效
        if ((rpcBased.getRpcType() == MiddlewareType.TYPE_WEB_SERVER) || (rpcBased.getRpcType() == MiddlewareType.TYPE_RPC && (rpcBased.getMiddlewareName() != null && rpcBased.getMiddlewareName().contains("http")))) {
            String formatUrl = ApiProcessor.merge(rpcBased.getAppName(), rpcBased.getServiceName(), rpcBased.getMethodName());
            return formatUrl;
        }
        return super.serviceParse(rpcBased);
    }
}
