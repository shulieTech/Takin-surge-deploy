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

public interface TemplateParser {

    Class<?> supportTemplateClass();

    /**
     * 解析trace，得到对应的影子库/表 及 业务表数据
     *
     * @param traceModel   trace实体
     * @param templateEnum {@link TemplateEnum}
     * @return left-影子库/表实体， right-业务表实体(不需要解析时，设置为null)，解析失败返回 null
     */
    ShadowDatabaseParseResult parseTemplate(TTrackClickhouseModel traceModel, TemplateEnum templateEnum);
}
