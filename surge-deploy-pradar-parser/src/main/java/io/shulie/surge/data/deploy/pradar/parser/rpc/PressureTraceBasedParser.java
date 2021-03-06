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
import io.shulie.surge.data.runtime.common.utils.ApiProcessor;

/**
 * @author sunshiyu
 */
public class PressureTraceBasedParser extends DefaultRpcBasedParser {
    /**
     * 服务解析器
     *
     * @param rpcBased
     * @return
     */
    @Override
    public String serviceParse(RpcBased rpcBased) {
        //临时放开调用tro,控制台需要userAppKey和envCode
        //return rpcBased.getServiceName();
        return ApiProcessor.matchBusinessActivity(rpcBased.getTaskId(), rpcBased.getServiceName(), rpcBased.getMethodName());
    }
}
