package io.shulie.surge.data.suppliers.grpc.remoting.converter.trace.middleware;

import com.google.common.collect.Sets;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.shulie.surge.data.suppliers.grpc.remoting.converter.ConvertUtils;
import io.shulie.surge.data.suppliers.grpc.remoting.trace.TraceProtoBean;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author vincent
 * @date 2022/07/02 15:42
 **/
public class DatabaseMiddlewareConverter implements MiddlewareConverter {

    private static final String DB_SYSTEM = "db.system";
    private static final String DB_CONNECTION_STRING = "db.connection_string";
    private static final String DB_USER = "db.user";
    private static final String DB_NAME = "db.name";
    private static final String DB_STATEMENT = "db.statement";
    private static final String DB_OPERATION = "db.operation";
    private static final String DB_REDIS_DATABSE_INDEX = "db.redis.database_index";
    private static final String DB_MONGODB_COLLECTION = "db.mongodb.collection";
    private static final String DB_SQL_TABLE = "db.sql.table";
    private static final Set<String> DATABASES = Sets.newHashSet();

    static {
        DATABASES.add("mssql");
        DATABASES.add("sqlserver");
        DATABASES.add("mongodb");
        DATABASES.add("mysql");
        DATABASES.add("oracle");
        DATABASES.add("db2");
        DATABASES.add("postgresql");
        DATABASES.add("redshift");
        DATABASES.add("hive");
        DATABASES.add("cloudscape");
        DATABASES.add("hsqldb");
        DATABASES.add("progress");
        DATABASES.add("maxdb");
        DATABASES.add("hanadb");
        DATABASES.add("ingres");
        DATABASES.add("firstsql");
        DATABASES.add("edb");
        DATABASES.add("adabas");
        DATABASES.add("firebird");
        DATABASES.add("derby");
        DATABASES.add("informix");
        DATABASES.add("instantdb");
        DATABASES.add("interbase");
        DATABASES.add("mariadb");
        DATABASES.add("pervasive");
        DATABASES.add("pointbase");
        DATABASES.add("sqlite");
        DATABASES.add("sybase");
        DATABASES.add("teradata");
        DATABASES.add("h2");
        DATABASES.add("cassandra");
    }

    /**
     * 属性转换
     *
     * @param span
     * @param traceBean
     */
    @Override
    public void convert(Span span, TraceProtoBean traceBean) {
        /**
         * traceBean.setAttributes("");
         * traceBean.setMethod();
         * traceBean.setService();
         * traceBean.setResultCode();
         * traceBean.setComment();
         */
        List<KeyValue> keyValues = span.getAttributesList();
        Map<String, String> attributes = ConvertUtils.attributeToMap(keyValues);
        String dbSystem = attributes.get(DB_SYSTEM);
        if (attributes.containsKey(DB_CONNECTION_STRING)) {
            if (StringUtils.isNotBlank(dbSystem) && DATABASES.contains(dbSystem)) {
                String url = attributes.get(DB_CONNECTION_STRING);
                if (-1 != StringUtils.indexOf(url, "?")) {
                    url = StringUtils.substring(url, 0, url.indexOf("?"));
                }
                traceBean.setService(url);
            }
        } else {
            if (StringUtils.isNotBlank(dbSystem) && DATABASES.contains(dbSystem)) {
                throw new IllegalArgumentException(DB_CONNECTION_STRING + " is null" + span);
            }
        }
        if (attributes.containsKey(DB_STATEMENT)) {
            traceBean.setComment(attributes.get(DB_STATEMENT));
        } else {
            if (StringUtils.isNotBlank(dbSystem) && !DATABASES.contains(dbSystem)) {
                throw new IllegalArgumentException(DB_STATEMENT + " is null" + span);
            }
        }
        if (attributes.containsKey(DB_OPERATION)) {
            if (StringUtils.isNotBlank(dbSystem) && !DATABASES.contains(dbSystem)) {
                traceBean.setMethod(attributes.get(DB_OPERATION));
            }
        } else {
            if (StringUtils.isNotBlank(dbSystem) && !DATABASES.contains(dbSystem)) {
                throw new IllegalArgumentException(DB_OPERATION + " is null" + span);
            }
        }
        if (attributes.containsKey(DB_REDIS_DATABSE_INDEX)) {
            if (StringUtils.isNotBlank(dbSystem) && !DATABASES.contains(dbSystem)) {
                traceBean.setService(attributes.get(DB_REDIS_DATABSE_INDEX));
            }
        } else if ("elasticsearch".equals(dbSystem)) {
            // es
            if (attributes.containsKey(DB_OPERATION)) {
                // 用操作的
                traceBean.setService(attributes.get(DB_OPERATION));
            } else {
                throw new IllegalArgumentException(DB_OPERATION + " is null" + span);
            }
        } else {
            if (StringUtils.isNotBlank(dbSystem) && !DATABASES.contains(dbSystem)) {
                throw new IllegalArgumentException(DB_REDIS_DATABSE_INDEX + " is null" + span);
            }
        }

        if (attributes.containsKey(DB_SQL_TABLE)) {
            if (StringUtils.isNotBlank(dbSystem) && DATABASES.contains(dbSystem)) {
                traceBean.setMethod(attributes.get(DB_SQL_TABLE));
            }
            // es
            if ("elasticsearch".equals(dbSystem)) {
                traceBean.setMethod(attributes.get(DB_SQL_TABLE));
            }
        } else {
            if (StringUtils.isNotBlank(dbSystem) && DATABASES.contains(dbSystem)) {
                throw new IllegalArgumentException(DB_OPERATION + " is null" + span);
            }
        }

    }

}
