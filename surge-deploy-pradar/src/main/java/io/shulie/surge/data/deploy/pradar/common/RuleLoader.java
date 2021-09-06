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

package io.shulie.surge.data.deploy.pradar.common;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author: xingchen
 * @ClassName: RuleUtils
 * @Package: io.shulie.surge.data.deploy.pradar.common
 * @Date: 2021/3/3122:13
 * @Description:
 */
@Singleton
public class RuleLoader {
    private static Logger logger = LoggerFactory.getLogger(RuleLoader.class);

    public static List<Map<String, Object>> flagRules = Lists.newArrayList();

    @Inject
    @Named(value = "config.rule.enable")
    public Boolean enableRule;

    @Inject
    private MysqlSupport mysqlSupport;

    private static final String RULESQL = "select rule,tips from t_amdb_pradar_rule where rule_type=1 order by priority";

    public void init() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("RULE-REFRESSH-%d").build());
        service.scheduleAtFixedRate(() -> {
            if (enableRule) {
                try {
                    flagRules = mysqlSupport.queryForList(RULESQL);
                } catch (Throwable e) {
                    logger.error("init rule error" + ExceptionUtils.getStackTrace(e));
                }
            }
        }, 0, 5, TimeUnit.MINUTES);
    }
}
