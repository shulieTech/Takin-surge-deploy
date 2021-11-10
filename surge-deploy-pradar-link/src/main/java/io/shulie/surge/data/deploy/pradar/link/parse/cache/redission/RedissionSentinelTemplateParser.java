package io.shulie.surge.data.deploy.pradar.link.parse.cache.redission;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import com.pamirs.attach.plugin.dynamic.Attachment;
import com.pamirs.attach.plugin.dynamic.Converter.TemplateConverter.TemplateEnum;
import com.pamirs.attach.plugin.dynamic.template.RedisTemplate.RedissionSentinelTemplate;
import io.shulie.surge.data.common.utils.Pair;
import io.shulie.surge.data.deploy.pradar.link.model.ShadowBizTableModel;
import io.shulie.surge.data.deploy.pradar.link.model.ShadowDatabaseModel;
import io.shulie.surge.data.deploy.pradar.link.model.TTrackClickhouseModel;
import io.shulie.surge.data.deploy.pradar.link.parse.AbstractTemplateParser;
import io.shulie.surge.data.deploy.pradar.link.parse.TemplateParseHandler;

public class RedissionSentinelTemplateParser extends AbstractTemplateParser {
    @Override
    public Class<?> supportTemplateClass() {
        return RedissionSentinelTemplate.class;
    }

    @Override
    public Pair<ShadowDatabaseModel, ShadowBizTableModel> doParseTemplate(TTrackClickhouseModel traceModel,
        TemplateEnum templateEnum) {
        Attachment<RedissionSentinelTemplate> attachment = JSON.parseObject(
            TemplateParseHandler.detachAttachment(traceModel),
            new TypeReference<Attachment<RedissionSentinelTemplate>>() {});
        RedissionSentinelTemplate template = attachment.getExt();
        ShadowDatabaseModel databaseModel = new ShadowDatabaseModel();
        databaseModel.setAppName(traceModel.getAppName());
        databaseModel.setDataSource("master:" + template.getMaster() + "\nnodes:" + template.getNodes());
        databaseModel.setDbName(traceModel.getMiddlewareName());
        databaseModel.setMiddlewareType(TemplateParseHandler.getMiddleware(templateEnum));
        databaseModel.setConnectionPool(TemplateParseHandler.formatterModuleId(attachment.getModuleId()));
        databaseModel.setAttachment(JSON.toJSONString(template));
        databaseModel.setExtInfo(TemplateParseHandler.getRedisClientModel(template.getModel()));
        databaseModel.setType(templateEnum.getKey());
        return new Pair<>(databaseModel, null);
    }
}
