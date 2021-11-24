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

package io.shulie.surge.data.deploy.pradar.link.parse.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import com.pamirs.attach.plugin.dynamic.Attachment;
import com.pamirs.attach.plugin.dynamic.Converter.TemplateConverter.TemplateEnum;
import com.pamirs.attach.plugin.dynamic.template.DruidTemplate;
import io.shulie.surge.data.deploy.pradar.link.model.ShadowBizTableModel;
import io.shulie.surge.data.deploy.pradar.link.model.ShadowDatabaseModel;
import io.shulie.surge.data.deploy.pradar.link.model.TTrackClickhouseModel;
import io.shulie.surge.data.deploy.pradar.link.parse.AbstractTemplateParser;
import io.shulie.surge.data.deploy.pradar.link.parse.ShadowDatabaseParseResult;
import io.shulie.surge.data.deploy.pradar.link.parse.TemplateParseHandler;
import io.shulie.surge.data.deploy.pradar.link.util.SqlMetadataParser;
import io.shulie.surge.data.deploy.pradar.link.util.SqlMetadataParser.SqlMetaData;
import org.apache.commons.lang3.StringUtils;

public class DruidTemplateParser extends AbstractTemplateParser {
    @Override
    public Class<?> supportTemplateClass() {
        return DruidTemplate.class;
    }

    @Override
    public ShadowDatabaseParseResult doParseTemplate(TTrackClickhouseModel traceModel,
        TemplateEnum templateEnum) {
        Attachment<DruidTemplate> attachment = JSON.parseObject(TemplateParseHandler.detachAttachment(traceModel),
            new TypeReference<Attachment<DruidTemplate>>() {});
        DruidTemplate template = attachment.getExt();
        ShadowDatabaseModel databaseModel = new ShadowDatabaseModel();
        databaseModel.setAppName(traceModel.getAppName());
        String dataSource = TemplateParseHandler.removeUrlQueryString(template.getUrl());
        databaseModel.setDataSource(dataSource);
        databaseModel.setDbName(traceModel.getMiddlewareName());
        databaseModel.setTableUser(template.getUsername());
        databaseModel.setPassword(template.getPassword());
        databaseModel.setMiddlewareType(TemplateParseHandler.getMiddleware(templateEnum));
        databaseModel.setConnectionPool(TemplateParseHandler.formatterModuleId(attachment.getModuleId()));
        databaseModel.setAttachment(JSON.toJSONString(template));
        databaseModel.setType(templateEnum.getKey());

        SqlMetaData sqlMetadata = SqlMetadataParser.parse(dataSource);
        databaseModel.setShadowDataSource(sqlMetadata.getShadowUrl());
        String parsedMethod = traceModel.getParsedMethod();
        List<ShadowBizTableModel> tableModelList = null;
        if (StringUtils.isNotBlank(parsedMethod)) {
            Set<String> uniqueTableName = new HashSet<>(new ArrayList<>(Arrays.asList(parsedMethod.split(","))));
            tableModelList = new ArrayList<>(uniqueTableName.size());
            for (String tableName : uniqueTableName) {
                if (StringUtils.isNotBlank(tableName)) {
                    ShadowBizTableModel bizTableModel = new ShadowBizTableModel();
                    bizTableModel.setAppName(databaseModel.getAppName());
                    bizTableModel.setDataSource(databaseModel.getDataSource());
                    bizTableModel.setBizDatabase(sqlMetadata.getDbName());
                    bizTableModel.setTableUser(databaseModel.getTableUser());
                    bizTableModel.setTableName(tableName);
                    tableModelList.add(bizTableModel);
                }
            }
        }
        return new ShadowDatabaseParseResult(databaseModel, tableModelList);
    }
}
