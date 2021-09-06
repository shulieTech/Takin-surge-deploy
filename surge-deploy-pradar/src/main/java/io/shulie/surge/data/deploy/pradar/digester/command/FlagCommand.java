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
import io.shulie.surge.data.deploy.pradar.common.RuleLoader;
import io.shulie.surge.data.deploy.pradar.model.RuleModel;
import io.shulie.surge.data.deploy.pradar.common.TraceFlagEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.beans.BeanMap;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;

public class FlagCommand implements ClickhouseCommand {
    private static Logger logger = LoggerFactory.getLogger(FlagCommand.class);

    @Override
    public LinkedHashSet<String> meta() {
        LinkedHashSet<String> linkedHashSet = Sets.newLinkedHashSet();
        linkedHashSet.add("flag");
        linkedHashSet.add("flagMessage");
        return linkedHashSet;
    }

    @Override
    public LinkedHashMap<String, Object> action(RpcBased rpcBased) {
        LinkedHashMap<String, Object> map = Maps.newLinkedHashMap();
        map.put("flag", TraceFlagEnum.LOG_OK.getCode());
        map.put("flagMessage", "");
        /*if (CollectionUtils.isNotEmpty(RuleLoader.flagRules)) {
            Map<String, Object> ctx = BeanMap.create(rpcBased);
            StringBuilder flagMessage = new StringBuilder();
            for (int i = 0; i < RuleLoader.flagRules.size(); i++) {
                try {
                    Map<String, Object> ruleModel = RuleLoader.flagRules.get(i);
                    if (StringUtils.isBlank(Objects.toString(ruleModel.get("rule")))) {
                        continue;
                    }
                    String result = Objects.toString(RuleFactory.getTinyElEvalService().eval(ctx, Objects.toString(ruleModel.get("rule"))));
                    if (Boolean.TRUE.equals(result)) {
                        flagMessage = flagMessage.append(ruleModel.get("tips") + ",");
                    }
                } catch (Throwable e) {
                    logger.error(ExceptionUtils.getStackTrace(e));
                    break;
                }
            }
            if (StringUtils.isNotBlank(flagMessage.toString())) {
                map.put("flag", TraceFlagEnum.LOG_ERROR.getCode());
                map.put("flagMessage", flagMessage.deleteCharAt(flagMessage.length() - 1));
            }
        }*/
        return map;
    }
}
