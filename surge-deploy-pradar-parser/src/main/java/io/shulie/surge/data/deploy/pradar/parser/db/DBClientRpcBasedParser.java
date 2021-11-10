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

package io.shulie.surge.data.deploy.pradar.parser.db;

import com.google.common.collect.Maps;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.deploy.pradar.parser.DefaultRpcBasedParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Map;

/**
 * DB
 *
 * @author vincent
 */
public class DBClientRpcBasedParser extends DefaultRpcBasedParser {
    private SqlParser sqlParser;

    public DBClientRpcBasedParser() {
        sqlParser = new SqlParser();
    }

    @Override
    public String methodParse(RpcBased rpcBased) {
        if ("hbase".equalsIgnoreCase(rpcBased.getMiddlewareName())) {
            String serviceName = rpcBased.getServiceName();
            //解析出表名
            //serviceName 2种格式  1:namespace:tableName 2: tableName
            serviceName = serviceName.contains(":") ? serviceName.split(":")[1] : serviceName;
            return serviceName;
        }
        // 兼容新版本，1.6版本的日志，method为表名，低版本为空,低版本解析表名放到method中
        if (StringUtils.isBlank(rpcBased.getMethodName())) {
            return sqlParser.parse(rpcBased);
        }
        return super.methodParse(rpcBased);
    }

    @Override
    public String serviceParse(RpcBased rpcBased) {
        String serviceName = rpcBased.getServiceName();
        if ("hbase".equalsIgnoreCase(rpcBased.getMiddlewareName())) {
            //serviceName 2种格式  1:namespace:tableName 2: tableName
            return serviceName.contains(":") ? serviceName.split(":")[0] : "default";
        }
        if (StringUtils.isNotBlank(serviceName)) {
            if (serviceName.contains("?")) {
                return serviceName.substring(0, serviceName.indexOf("?"));
            }
        }
        return super.serviceParse(rpcBased);
    }

    /**
     * @param rpcBased
     * @return
     */
    @Override
    public String appNameParse(RpcBased rpcBased) {
        String addr = rpcBased.getRemoteIp();
        int port = NumberUtils.toInt(rpcBased.getPort(), 0);
        String dbType = rpcBased.getMiddlewareName();
        String serviceName = serviceParse(rpcBased);
        String appName = dbType + " " + addr + ":" + port + ":" + serviceName;
        if (StringUtils.isBlank(addr)) {
            return dbType;
        }
        return appName;
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

    /**
     * @param rpcBased
     * @return
     */
    @Override
    public String serverAppNameParse(RpcBased rpcBased) {
        return rpcBased.getAppName();
    }
}
