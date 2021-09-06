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

import com.pamirs.pradar.log.parser.trace.RpcBased;

import java.util.Map;

/**
 * 定义解析的模板类
 *
 * @ClassName: RpcBasedParser
 * @Date: 2021/7/3117:06
 * @Description:
 */
public interface RpcBasedParser {
    /**
     * 边编号
     *
     * @param linkId
     * @param rpcBased
     * @return
     */
    String edgeId(String linkId, RpcBased rpcBased);

    /**
     * 边tags
     *
     * @param linkId
     * @param rpcBased
     * @return
     */
    Map<String, Object> edgeTags(String linkId, RpcBased rpcBased);

    /**
     * 链路tags
     *
     * @param rpcBased
     * @return
     */
    Map<String, Object> linkTags(RpcBased rpcBased);

    /**
     * 应用编号
     *
     * @param linkId
     * @param rpcBased
     * @return
     */
    String fromAppId(String linkId, RpcBased rpcBased);

    /**
     * 应用tags
     *
     * @param rpcBased
     * @return
     */
    Map<String, Object> fromAppTags(String linkId, RpcBased rpcBased);

    /**
     * 应用编号
     *
     * @param linkId
     * @param rpcBased
     * @return
     */
    String toAppId(String linkId, RpcBased rpcBased);

    /**
     * 应用tags
     *
     * @param rpcBased
     * @return
     */
    Map<String, Object> toAppTags(String linkId, RpcBased rpcBased);

    /**
     * 链路编号
     *
     * @param rpcBased
     * @return
     */
    String linkId(RpcBased rpcBased);

    /**
     * @param map
     * @return
     */
    String linkId(Map<String, Object> map);

    /**
     * 服务解析器
     *
     * @return
     */
    String serviceParse(RpcBased rpcBased);

    /**
     * 方法解析器
     *
     * @return
     */
    String methodParse(RpcBased rpcBased);

    /**
     * 其他信息解析器
     */
    String extendParse(RpcBased rpcBased);

    /**
     * @return
     */
    String appNameParse(RpcBased rpcBased);

    /**
     * @return
     */
    String serverAppNameParse(RpcBased rpcBased);

    /**
     * @param map
     * @return
     */
    Map<String, Object> linkTags(Map<String, Object> map);
}
