package io.shulie.surge.data.deploy.pradar.link.parse;

import com.pamirs.attach.plugin.dynamic.Converter.TemplateConverter.TemplateEnum;
import io.shulie.surge.data.common.utils.Pair;
import io.shulie.surge.data.deploy.pradar.link.model.ShadowBizTableModel;
import io.shulie.surge.data.deploy.pradar.link.model.ShadowDatabaseModel;
import io.shulie.surge.data.deploy.pradar.link.model.TTrackClickhouseModel;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractTemplateParser implements TemplateParser {

    @Override
    public Pair<ShadowDatabaseModel, ShadowBizTableModel> parseTemplate(TTrackClickhouseModel traceModel,
        TemplateEnum templateEnum) {
        if (StringUtils.isBlank(TemplateParseHandler.detachAttachment(traceModel))) {
            return null;
        }
        return doParseTemplate(traceModel, templateEnum);
    }

    public abstract Pair<ShadowDatabaseModel, ShadowBizTableModel> doParseTemplate(TTrackClickhouseModel traceModel,
        TemplateEnum templateEnum);
}
