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
import io.shulie.surge.data.deploy.pradar.parser.cache.CacheRpcBasedParser;
import io.shulie.surge.data.deploy.pradar.parser.db.DBClientRpcBasedParser;
import io.shulie.surge.data.deploy.pradar.parser.fs.FsRpcBasedParser;
import io.shulie.surge.data.deploy.pradar.parser.job.JobRpcBasedParser;
import io.shulie.surge.data.deploy.pradar.parser.mq.MQConsumerRpcBasedParser;
import io.shulie.surge.data.deploy.pradar.parser.mq.MQProducerRpcBasedParser;
import io.shulie.surge.data.deploy.pradar.parser.rpc.RpcClientRpcBasedParser;
import io.shulie.surge.data.deploy.pradar.parser.rpc.RpcServerRpcBasedParser;
import io.shulie.surge.data.deploy.pradar.parser.rpc.TraceRpcBasedParser;
import io.shulie.surge.data.deploy.pradar.parser.search.SearchRpcBasedParser;
import io.shulie.surge.deploy.pradar.spi.ServiceLoaderExtension;

import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author vincent
 * @desc 解析工产类
 */
public class RpcBasedParserFactory {
    private static final String Separator = "~";

    private static RpcBasedParserFactory INSTANCE = new RpcBasedParserFactory();
    private Map<String, RpcBasedParser> rpcBasedParserMap = Maps.newHashMap();

    public RpcBasedParserFactory() {
        ServiceLoaderExtension<RpcBasedParser> serviceLoaderExtension = ServiceLoaderExtension.getExtensionLoader(RpcBasedParser.class);
        Map<String, Class<?>> classes = serviceLoaderExtension.getExtensionClasses();
        classes.entrySet().forEach(clazzMap -> {
            rpcBasedParserMap.put(clazzMap.getKey(), serviceLoaderExtension.createExtension(clazzMap.getValue()));
        });
    }

    /**
     * 获取解析类
     *
     * @param logType 客户端日志还是服务端日志
     * @param rpcType 服务端端类型
     * @return
     */
    public static RpcBasedParser getInstance(int logType, int rpcType) {
        return INSTANCE.rpcBasedParserMap.get(logType + Separator + rpcType);
    }
}
