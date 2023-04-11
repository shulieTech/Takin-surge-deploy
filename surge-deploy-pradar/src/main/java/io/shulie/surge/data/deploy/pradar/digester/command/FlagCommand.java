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

import com.google.common.collect.Sets;
import com.pamirs.pradar.log.parser.trace.AttachmentBased;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.deploy.pradar.common.TraceFlagEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class FlagCommand implements ClickhouseCommand {
    private static Logger logger = LoggerFactory.getLogger(FlagCommand.class);
    private static ThreadLocal<LinkedHashMap<String, Object>> mapGetter = ThreadLocal.withInitial(() -> new LinkedHashMap<>(200));

    private LinkedHashSet<String> meta;

    public FlagCommand() {
        meta = Sets.newLinkedHashSet();
        meta.add("flag");
        meta.add("flagMessage");
    }

    @Override
    public LinkedHashSet<String> meta() {
        return meta;
    }

    @Override
    public LinkedHashMap<String, Object> action(RpcBased rpcBased) {
        LinkedHashMap<String, Object> map = mapGetter.get();
        map.put("flag", TraceFlagEnum.LOG_OK.getCode());

        //放入attachment
        AttachmentBased attachmentBased = rpcBased.getAttachmentBased();
        if (attachmentBased != null) {
            map.put("flagMessage", attachmentBased.getTemplateId() + "@##" + attachmentBased.getExt());
        } else {
            map.put("flagMessage", "");
        }
        return map;
    }
}
