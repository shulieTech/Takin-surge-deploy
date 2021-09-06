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

package io.shulie.surge.data.deploy.pradar.parser.fs;

import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.deploy.pradar.parser.db.DBClientRpcBasedParser;
import io.shulie.surge.data.runtime.common.utils.ApiProcessor;

/**
 * @author xingchen
 */
public class FsRpcBasedParser extends DBClientRpcBasedParser {
    private static final String OSS = "OSS";

    @Override
    public String serviceParse(RpcBased rpcBased) {
        if (rpcBased.getMiddlewareName().equalsIgnoreCase(OSS)) {
            return ApiProcessor.formatUrl(rpcBased.getServiceName());
        }
        return super.serviceParse(rpcBased);
    }

    @Override
    public String appNameParse(RpcBased rpcBased) {
        if (rpcBased.getMiddlewareName().equalsIgnoreCase(OSS)) {
            String addr = rpcBased.getRemoteIp();
            String dbType = rpcBased.getMiddlewareName();
            String appName = dbType + " " + addr;
            return appName;
        }
        return super.appNameParse(rpcBased);
    }
}
