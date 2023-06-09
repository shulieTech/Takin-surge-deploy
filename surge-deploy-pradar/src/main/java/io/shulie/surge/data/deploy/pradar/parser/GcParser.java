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

import com.pamirs.pradar.log.parser.ProtocolParserFactory;
import com.pamirs.pradar.log.parser.metrics.GcBased;
import io.shulie.surge.data.runtime.digest.DigestContext;
import io.shulie.surge.data.runtime.parser.DataParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class GcParser implements DataParser<String, GcBased> {

    private static final Logger logger = LoggerFactory.getLogger(GcParser.class);

    private static final String HEADER_DATA_VERSION = "dataVersion";
    private static final String HEADER_HOST_IP = "hostIp";

    /**
     * 创建数据处理上下文
     *
     * @param header
     * @param content
     * @return
     */
    @Override
    public DigestContext<GcBased> createContext(Map<String, Object> header, String content) {
        String dataVersion = String.valueOf(header.getOrDefault(HEADER_DATA_VERSION, "1.0"));
        String hostIp = String.valueOf(header.getOrDefault(HEADER_HOST_IP, ""));
        content += '|' + hostIp + '|' + dataVersion;
        long now = System.currentTimeMillis();
        GcBased gcBased = ProtocolParserFactory.getFactory().getGcProtocolParser(dataVersion).parse(hostIp, dataVersion, content);
        if (gcBased == null) {
            logger.warn("未解析到日志信息->" + content);
            return null;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("receive appName={},log source:{}", gcBased.getAppName(), gcBased.getLog());
        }
        gcBased.setLog(content);

        DigestContext<GcBased> context = new DigestContext<>();
        context.setContent(gcBased);
        context.setHeader(header);
        context.setProcessTime(now);
        context.setEventTime(gcBased.getLogTime());
        return context;
    }


}
