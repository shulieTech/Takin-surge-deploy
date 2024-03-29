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

package io.shulie.surge.data.deploy.pradar.digester.command;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.deploy.pradar.common.MiddlewareTypeEnum;
import io.shulie.surge.data.deploy.pradar.parser.PradarLogType;
import io.shulie.surge.data.deploy.pradar.parser.RpcBasedParser;
import io.shulie.surge.data.deploy.pradar.parser.RpcBasedParserFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class LinkCommand implements ClickhouseCommand {
    @Override
    public LinkedHashSet<String> meta() {
        LinkedHashSet<String> linkedHashSet = Sets.newLinkedHashSet();
        linkedHashSet.add("timeMin");
        linkedHashSet.add("dateToMin");
        linkedHashSet.add("parsedServiceName");
        linkedHashSet.add("parsedMethod");
        linkedHashSet.add("parsedAppName");
        linkedHashSet.add("parsedExtend");
        linkedHashSet.add("parsedMiddlewareName");
        linkedHashSet.add("entranceServiceType");
        return linkedHashSet;
    }

    @Override
    public LinkedHashMap<String, Object> action(RpcBased rpcBased) {
        RpcBasedParser rpcBasedParser = RpcBasedParserFactory.getInstance(rpcBased.getLogType(), rpcBased.getRpcType());
        LinkedHashMap<String, Object> map = Maps.newLinkedHashMap();
        map.put("timeMin", rpcBased.getStartTime() / 1000 / 60);
        map.put("dateToMin", rpcBased.getStartTime() / 1000 / 60 / 60 / 24);
        if (rpcBasedParser != null) {
            parse0(rpcBased, rpcBasedParser, map);
        } else {
            parse1(rpcBased, map);
        }
        // TODO 去掉
        map.put("entranceServiceType", "");
        return map;
    }

    private void parse1(RpcBased rpcBased, LinkedHashMap<String, Object> map) {
        //如果是压测引擎日志,且非http调用,如dubbo等rpc调用,赋值parsedService,parsedMethod
        if (rpcBased.getLogType() == PradarLogType.LOG_TYPE_FLOW_ENGINE) {
            map.put("parsedServiceName", rpcBased.getServiceName());
            map.put("parsedMethod", rpcBased.getMethodName());
            map.put("parsedAppName", rpcBased.getAppName());
        } else {
            map.put("parsedServiceName", "");
            map.put("parsedMethod", "");
            map.put("parsedAppName", "");
        }
        map.put("parsedExtend", "");
        map.put("parsedMiddlewareName", "");
    }

    private void parse0(RpcBased rpcBased, RpcBasedParser rpcBasedParser, LinkedHashMap<String, Object> map) {
        map.put("parsedServiceName", StringUtils.defaultString(rpcBasedParser.serviceParse(rpcBased), ""));
        map.put("parsedMethod", StringUtils.defaultString(rpcBasedParser.methodParse(rpcBased), ""));
        map.put("parsedAppName", StringUtils.defaultString(rpcBasedParser.appNameParse(rpcBased), ""));
        map.put("parsedExtend", StringUtils.defaultString(rpcBasedParser.extendParse(rpcBased), ""));
        map.put("parsedMiddlewareName", MiddlewareTypeEnum.getNodeType(rpcBased.getMiddlewareName()).getType());
    }
}
