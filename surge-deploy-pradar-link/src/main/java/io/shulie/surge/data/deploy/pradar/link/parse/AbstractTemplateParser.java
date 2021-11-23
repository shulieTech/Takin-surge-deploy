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

package io.shulie.surge.data.deploy.pradar.link.parse;

import com.pamirs.attach.plugin.dynamic.Converter.TemplateConverter.TemplateEnum;
import io.shulie.surge.data.deploy.pradar.link.model.TTrackClickhouseModel;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractTemplateParser implements TemplateParser {

    @Override
    public ShadowDatabaseParseResult parseTemplate(TTrackClickhouseModel traceModel, TemplateEnum templateEnum) {
        if (StringUtils.isBlank(TemplateParseHandler.detachAttachment(traceModel))) {
            return null;
        }
        return doParseTemplate(traceModel, templateEnum);
    }

    public abstract ShadowDatabaseParseResult doParseTemplate(TTrackClickhouseModel traceModel, TemplateEnum templateEnum);
}
