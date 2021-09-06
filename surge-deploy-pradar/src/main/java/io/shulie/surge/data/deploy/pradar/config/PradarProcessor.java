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

package io.shulie.surge.data.deploy.pradar.config;


import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.pamirs.pradar.logger.Logger;
import com.pamirs.pradar.logger.LoggerFactory;
import io.shulie.surge.data.deploy.pradar.parser.PradarPaserFactory;
import io.shulie.surge.data.runtime.digest.DigestContext;
import io.shulie.surge.data.runtime.parser.DataParser;
import io.shulie.surge.data.runtime.processor.DefaultProcessor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * pradar 默认执行器
 */
public class PradarProcessor extends DefaultProcessor<String, DigestContext> {
    private static final Logger logger = LoggerFactory.getLogger(PradarProcessor.class);
    private static final long MAX_LOG_PROCESS_TIME_BEFORE = TimeUnit.HOURS.toMillis(1);


    public static final String DATA_TYPE = "dataType";

    /**
     * 获取解析器
     *
     * @param header
     * @return
     */
    @Override
    protected DataParser<String, DigestContext> getDataParser(Map<String, Object> header) {
        Byte dataType = (Byte) header.get(DATA_TYPE);
        return PradarPaserFactory.getParser(dataType);
    }

    @Override
    public List<String> splitLog(String content) {
//        logger.info("recevie log {}", content);
        Iterable<String> iterator = Splitter.on("\r\n").omitEmptyStrings().split(content);
        return Lists.newArrayList(iterator);
    }


    /**
     * 移除延迟数据
     *
     * @param eventTime
     * @return
     */
    @Override
    public boolean removeDelay(Map<String, Object> header, long eventTime, long processTime, String log) {
        //return false;
        if (System.currentTimeMillis() - eventTime > MAX_LOG_PROCESS_TIME_BEFORE) {
            logger.error("Receive log delay over {} mils,removeDelay,hostIp:{},eventTime:{},processTime:{}/n,log content:{}", MAX_LOG_PROCESS_TIME_BEFORE, header.get("hostIp"), eventTime, processTime, log);
            return true;
        }
        return false;
    }

    @Override
    public boolean checkDelay(Map<String, Object> header, long eventTime, long processTime, String log) {
        /*if (processTime - eventTime > MAX_LOG_PROCESS_DELAY) {
            logger.error("Receive log delay over {} mils,hostIp:{},eventTime:{},processTime:{}/n,log content:{}", MAX_LOG_PROCESS_DELAY, header.get("hostIp"), eventTime, processTime, log);
        }*/
        return false;
    }


}
