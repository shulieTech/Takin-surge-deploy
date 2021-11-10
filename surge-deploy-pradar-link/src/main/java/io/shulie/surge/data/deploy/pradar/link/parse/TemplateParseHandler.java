package io.shulie.surge.data.deploy.pradar.link.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pamirs.attach.plugin.dynamic.Converter.TemplateConverter;
import com.pamirs.attach.plugin.dynamic.Converter.TemplateConverter.TemplateEnum;
import com.pamirs.attach.plugin.dynamic.Type;
import com.pamirs.attach.plugin.dynamic.Type.MiddleWareType;
import com.pamirs.attach.plugin.dynamic.template.Info;
import com.pamirs.attach.plugin.dynamic.template.RedisTemplate.MODEL;
import io.shulie.surge.data.common.utils.Pair;
import io.shulie.surge.data.deploy.pradar.link.model.ShadowBizTableModel;
import io.shulie.surge.data.deploy.pradar.link.model.ShadowDatabaseModel;
import io.shulie.surge.data.deploy.pradar.link.model.TTrackClickhouseModel;
import io.shulie.surge.data.deploy.pradar.link.parse.cache.jedis.JedisClusterTemplateParser;
import io.shulie.surge.data.deploy.pradar.link.parse.cache.jedis.JedisMasterSlaveTemplateParser;
import io.shulie.surge.data.deploy.pradar.link.parse.cache.jedis.JedisSentinelTemplateParser;
import io.shulie.surge.data.deploy.pradar.link.parse.cache.jedis.JedisSingleTemplateParser;
import io.shulie.surge.data.deploy.pradar.link.parse.cache.lettuce.LettuceClusterTemplateParser;
import io.shulie.surge.data.deploy.pradar.link.parse.cache.lettuce.LettuceMasterSlaveTemplateParser;
import io.shulie.surge.data.deploy.pradar.link.parse.cache.lettuce.LettuceSentinelTemplateParser;
import io.shulie.surge.data.deploy.pradar.link.parse.cache.lettuce.LettuceSingleTemplateParser;
import io.shulie.surge.data.deploy.pradar.link.parse.cache.redission.RedissionClusterTemplateParser;
import io.shulie.surge.data.deploy.pradar.link.parse.cache.redission.RedissionSentinelTemplateParser;
import io.shulie.surge.data.deploy.pradar.link.parse.cache.redission.RedissionSingleTemplateParser;
import io.shulie.surge.data.deploy.pradar.link.parse.cache.redission.RedissonMasterSlaveTemplateParser;
import io.shulie.surge.data.deploy.pradar.link.parse.db.C3p0TemplateParser;
import io.shulie.surge.data.deploy.pradar.link.parse.db.DbcpTemplateParser;
import io.shulie.surge.data.deploy.pradar.link.parse.db.DruidTemplateParser;
import io.shulie.surge.data.deploy.pradar.link.parse.db.HikariTemplateParser;
import io.shulie.surge.data.deploy.pradar.link.parse.db.ProxoolTemplateParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateParseHandler {

    private static final Logger logger = LoggerFactory.getLogger(TemplateParseHandler.class);

    private static final String DELIMITER = TemplateConverter.SPLITTER;

    private static final String QUERY_PREFIX = "?";

    /**
     * key：TemplateParser#supportTemplateClass 的全路径
     * value：TemplateParser具体实现
     */
    private static final Map<String, TemplateParser> PARSER_HOLDER = new HashMap<>(32);

    private static final List<String> FORMATTER_TEMPLATE = new ArrayList<>(10);

    static {
        List<Class<? extends TemplateParser>> parserClassHolder = new ArrayList<>(16);
        parserClassHolder.add(C3p0TemplateParser.class);
        parserClassHolder.add(DruidTemplateParser.class);
        parserClassHolder.add(HikariTemplateParser.class);
        parserClassHolder.add(DbcpTemplateParser.class);
        parserClassHolder.add(JedisClusterTemplateParser.class);
        parserClassHolder.add(JedisSentinelTemplateParser.class);
        parserClassHolder.add(JedisSingleTemplateParser.class);
        parserClassHolder.add(JedisMasterSlaveTemplateParser.class);
        parserClassHolder.add(LettuceClusterTemplateParser.class);
        parserClassHolder.add(LettuceSentinelTemplateParser.class);
        parserClassHolder.add(LettuceSingleTemplateParser.class);
        parserClassHolder.add(LettuceMasterSlaveTemplateParser.class);
        parserClassHolder.add(RedissionClusterTemplateParser.class);
        parserClassHolder.add(RedissionSentinelTemplateParser.class);
        parserClassHolder.add(RedissionSingleTemplateParser.class);
        parserClassHolder.add(RedissonMasterSlaveTemplateParser.class);
        parserClassHolder.add(ProxoolTemplateParser.class);

        for (Class<? extends TemplateParser> parser : parserClassHolder) {
            try {
                TemplateParser templateParser = parser.newInstance();
                PARSER_HOLDER.put(templateParser.supportTemplateClass().getName(), templateParser);
            } catch (Exception ignore) {
            }
        }
        initFormatterTemplate();
    }

    public static Pair<ShadowDatabaseModel, ShadowBizTableModel> analysisTraceModel(TTrackClickhouseModel model) {
        String flagMessage = model.getFlagMessage();
        String type = StringUtils.substringBefore(flagMessage, DELIMITER);
        TemplateEnum templateEnum = TemplateConverter.ofKey(type);
        if (templateEnum != TemplateEnum._default) {
            String className = templateEnum.getaClass().getName();
            if (PARSER_HOLDER.containsKey(className)) {
                try {
                    return PARSER_HOLDER.get(className).parseTemplate(model, templateEnum);
                } catch (Exception e) {
                    logger.error("parse template error. flagMessage=[{}]", flagMessage, e);
                }
            }
        }
        return null;
    }

    public static String detachAttachment(TTrackClickhouseModel traceModel) {
        return StringUtils.substringAfter(traceModel.getFlagMessage(), DELIMITER);
    }

    // 获取中间件类型名称
    public static String getMiddleware(TemplateEnum templateEnum) {
        Type type = templateEnum.getType();
        if (type instanceof MiddleWareType) {
            return ((MiddleWareType)type).value();
        }
        return null;
    }

    // 获取 redis model 的中文描述
    public static String getRedisClientModel(MODEL model) {
        try {
            Info info = MODEL.class.getDeclaredField(model.toString()).getAnnotation(Info.class);
            if (info != null) {
                return info.describe();
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    // 移除url上的 query string
    public static String removeUrlQueryString(String url) {
        return StringUtils.substringBefore(url, QUERY_PREFIX);
    }

    private static void initFormatterTemplate() {
        FORMATTER_TEMPLATE.add("dubbo");
        FORMATTER_TEMPLATE.add("jedis");
        FORMATTER_TEMPLATE.add("lettuce");
        FORMATTER_TEMPLATE.add("redisson");
        FORMATTER_TEMPLATE.add("druid");
    }

    public static String formatterModuleId(String moduleId) {
        if (StringUtils.isBlank(moduleId)) { return moduleId; }
        return FORMATTER_TEMPLATE.stream().filter(moduleId::contains).findFirst().orElse(moduleId);
    }
}
