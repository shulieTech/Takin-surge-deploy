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

package io.shulie.surge.data.deploy.pradar.link;

import com.google.common.collect.Maps;
import io.shulie.surge.data.common.dataSource.DataSourceSupport;
import io.shulie.surge.data.deploy.pradar.parser.DefaultRpcBasedParser;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 链路缓存
 *
 * @author vincent
 */
public abstract class AbstractLinkCache {
    private static final Logger logger = LoggerFactory.getLogger(AbstractLinkCache.class);

    private static final String LINKID_CONFIG_TABLENAME = "t_amdb_pradar_link_config";

    private LinkedHashMap<String, Map<String, Object>> linkConfig = Maps.newLinkedHashMap();

    /**
     * 增加链路时低延时更新队列
     */
    private HashMap<String, LinkedBlockingQueue<String>> directlyQueueMap = new HashMap<String, LinkedBlockingQueue<String>>();


    /**
     * 自动刷新hbase数据
     */
    public void autoRefresh(MysqlSupport mysqlSupport) {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                refresh(mysqlSupport);
                //System.out.println("cost:" + (System.currentTimeMillis() - start));
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    protected void refresh(MysqlSupport mysqlSupport) {
        try {
            LinkedHashMap<String, Map<String, Object>> tmpLinkConfig = Maps.newLinkedHashMap();
            DefaultRpcBasedParser defaultRpcBasedParser = new DefaultRpcBasedParser();
            List<Map<String, Object>> linkConfigModels = mysqlSupport.queryForList("select * from " + LINKID_CONFIG_TABLENAME + " limit 9999");
            for (Map<String, Object> linkConf : linkConfigModels) {
                Map<String, Object> linkConfMap = Maps.newHashMap();
                for (String key : linkConf.keySet()) {
                    linkConfMap.put(lineToHump(key), linkConf.get(key));
                }
                if (linkConf == null || linkConf.isEmpty()) {
                    continue;
                }
                String linkId = defaultRpcBasedParser.linkId(linkConfMap);
                tmpLinkConfig.put(linkId, defaultRpcBasedParser.linkTags(linkConfMap));
            }
            linkConfig = tmpLinkConfig;
        } catch (Exception e) {
            logger.error("Query linkId configuration faild.", e);
        }
    }

    public abstract void save(String linkId, LinkedBlockingQueue<String> linkedBlockingQueue);

    public LinkedHashMap<String, Map<String, Object>> getLinkConfig() {
        return linkConfig;
    }

    private static Pattern linePattern = Pattern.compile("_(\\w)");

    /**
     * 下划线转驼峰
     */
    public static String lineToHump(String str) {
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
