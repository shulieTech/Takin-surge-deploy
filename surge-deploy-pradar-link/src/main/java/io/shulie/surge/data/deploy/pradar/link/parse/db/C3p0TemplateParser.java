package io.shulie.surge.data.deploy.pradar.link.parse.db;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import com.pamirs.attach.plugin.dynamic.Attachment;
import com.pamirs.attach.plugin.dynamic.Converter.TemplateConverter.TemplateEnum;
import com.pamirs.attach.plugin.dynamic.template.C3p0Template;
import io.shulie.surge.data.common.utils.Pair;
import io.shulie.surge.data.deploy.pradar.link.model.ShadowBizTableModel;
import io.shulie.surge.data.deploy.pradar.link.model.ShadowDatabaseModel;
import io.shulie.surge.data.deploy.pradar.link.model.TTrackClickhouseModel;
import io.shulie.surge.data.deploy.pradar.link.parse.AbstractTemplateParser;
import io.shulie.surge.data.deploy.pradar.link.parse.TemplateParseHandler;
import io.shulie.surge.data.deploy.pradar.link.util.SqlMetadataParser;
import io.shulie.surge.data.deploy.pradar.link.util.SqlMetadataParser.SqlMetaData;
import org.apache.commons.lang3.StringUtils;

public class C3p0TemplateParser extends AbstractTemplateParser {

    @Override
    public Class<?> supportTemplateClass() {
        return C3p0Template.class;
    }

    @Override
    public Pair<ShadowDatabaseModel, ShadowBizTableModel> doParseTemplate(TTrackClickhouseModel traceModel,
        TemplateEnum templateEnum) {
        Attachment<C3p0Template> attachment = JSON.parseObject(TemplateParseHandler.detachAttachment(traceModel),
            new TypeReference<Attachment<C3p0Template>>() {});
        C3p0Template template = attachment.getExt();
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
        ShadowBizTableModel bizTableModel = null;
        String parsedMethod = traceModel.getParsedMethod();
        if (StringUtils.isNotBlank(parsedMethod)) {
            bizTableModel = new ShadowBizTableModel();
            bizTableModel.setAppName(databaseModel.getAppName());
            bizTableModel.setDataSource(databaseModel.getDataSource());
            bizTableModel.setBizDatabase(sqlMetadata.getDbName());
            bizTableModel.setTableUser(databaseModel.getTableUser());
            bizTableModel.setTableName(parsedMethod);
        }
        return new Pair<>(databaseModel, bizTableModel);
    }
}
