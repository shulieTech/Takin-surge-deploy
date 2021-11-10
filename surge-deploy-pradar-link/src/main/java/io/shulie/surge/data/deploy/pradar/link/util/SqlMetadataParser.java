package io.shulie.surge.data.deploy.pradar.link.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class SqlMetadataParser {

    private static final String SHADOW_PREFIX = "PT_";

    public static SqlMetaData parse(String url) {
        DbType dbType = DbType.guessDbType(url);
        return dbType == null ? new SqlMetaData() : Optional.ofNullable(dbType.readMetaData(url)).orElseGet(SqlMetaData::new);
    }

    public static class SqlMetaData {
        private String prefix;
        private String mainUrl;
        private DbType dbType;
        private String dbName;
        private boolean ignoreShadowPrefix = false;

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getMainUrl() {
            return mainUrl;
        }

        public void setMainUrl(String mainUrl) {
            this.mainUrl = mainUrl;
        }

        public String getDbName() {
            return dbName;
        }

        public void setDbName(String dbName) {
            this.dbName = dbName;
        }

        public DbType getDbType() {
            return dbType;
        }

        public void setDbType(DbType dbType) {
            this.dbType = dbType;
        }

        public boolean isIgnoreShadowPrefix() {
            return ignoreShadowPrefix;
        }

        public void setIgnoreShadowPrefix(boolean ignoreShadowPrefix) {
            this.ignoreShadowPrefix = ignoreShadowPrefix;
        }

        public String getShadowUrl() {
            if (StringUtils.isBlank(dbName)) { return null; }
            return prefix + mainUrl.replace(dbName, ignoreShadowPrefix ? dbName : SHADOW_PREFIX + dbName);
        }
    }

    enum DbType {
        MYSQL("jdbc:mysql://", "jdbc:mysql:loadbalance://", "jdbc:mysql:replication://") {
            @Override
            public SqlMetaData readMetaData(String url) {
                String mainUrl = getMainUrl(url);
                SqlMetaData sqlMetaData = new SqlMetaData();
                sqlMetaData.setMainUrl(mainUrl);
                sqlMetaData.setPrefix(getMatchedPrefix(url));
                sqlMetaData.setDbType(MYSQL);
                sqlMetaData.setDbName(StringUtils.substringAfter(mainUrl, "/"));
                return sqlMetaData;
            }
        },
        OCEANBASE("jdbc:oceanbase://") {
            @Override
            public SqlMetaData readMetaData(String url) {
                String mainUrl = getMainUrl(url);
                SqlMetaData sqlMetaData = new SqlMetaData();
                sqlMetaData.setMainUrl(mainUrl);
                sqlMetaData.setPrefix(getMatchedPrefix(url));
                sqlMetaData.setDbType(OCEANBASE);
                sqlMetaData.setDbName(StringUtils.substringAfter(mainUrl, "/"));
                return sqlMetaData;
            }
        },
        ORACLE("jdbc:oracle:thin:@", "jdbc:oracle:thin:@//") {
            @Override
            public SqlMetaData readMetaData(String url) {
                String mainUrl = getMainUrl(url);
                SqlMetaData sqlMetaData;
                if (mainUrl.charAt(0) == '(') {
                    sqlMetaData = parseServiceConfigOracleUrl(mainUrl);
                } else {
                    sqlMetaData = parseCommonOracleUrl(mainUrl);
                }
                sqlMetaData.setMainUrl(mainUrl);
                sqlMetaData.setPrefix(getMatchedPrefix(url));
                sqlMetaData.setIgnoreShadowPrefix(true);
                return sqlMetaData;
            }

            private SqlMetaData parseServiceConfigOracleUrl(String mainUrl) {
                Map<String, String> keyValues = parseKeyValues(mainUrl);
                SqlMetaData sqlMetaData = new SqlMetaData();
                sqlMetaData.setDbType(ORACLE);
                sqlMetaData.setDbName(StringUtils.trim(keyValues.get("service_name")));
                return sqlMetaData;
            }

            private Map<String, String> parseKeyValues(String str) {
                Map<String, String> keyValues = new HashMap<>();
                int lastLeft = -1;
                for (int i = 0, len = str.length(); i < len; i++) {
                    if (str.charAt(i) == '(') {
                        lastLeft = i;
                    } else if (str.charAt(i) == ')' && lastLeft != -1) {
                        String keyValueStr = str.substring(lastLeft + 1, i);
                        if (keyValueStr.indexOf('=') != -1) {
                            String[] pair = StringUtils.split(keyValueStr, '=');
                            if (pair.length == 2) {
                                keyValues.put(StringUtils.trim(StringUtils.lowerCase(pair[0])),
                                    StringUtils.trim(pair[1]));
                            }
                            lastLeft = -1;
                        }
                    }
                }
                return keyValues;
            }

            private SqlMetaData parseCommonOracleUrl(String mainUrl) {
                int last1 = mainUrl.lastIndexOf('/');
                int last2 = mainUrl.lastIndexOf(':');
                int lastIndex = Math.max(last1, last2);
                SqlMetaData sqlMetaData = new SqlMetaData();
                sqlMetaData.setDbType(ORACLE);
                sqlMetaData.setDbName(mainUrl.substring(lastIndex + 1));
                return sqlMetaData;
            }
        },
        SQLSERVER("jdbc:microsoft:sqlserver://", "jdbc:sqlserver://", "jdbc:jtds:sqlserver://") {
            @Override
            public SqlMetaData readMetaData(String url) {
                String mainUrl = getMainUrl(url);
                SqlMetaData sqlMetaData = new SqlMetaData();
                sqlMetaData.setDbType(SQLSERVER);
                sqlMetaData.setMainUrl(mainUrl);
                sqlMetaData.setPrefix(getMatchedPrefix(url));
                try {
                    mainUrl = StringUtils.substringAfter(mainUrl, ":");
                    if (mainUrl.charAt(0) != ';' && mainUrl.indexOf(';') != -1) {
                        mainUrl = StringUtils.substringAfter(mainUrl, ";");
                    } else if (mainUrl.contains("/")) {
                        mainUrl = StringUtils.substringAfter(mainUrl, "/");
                    }
                    String[] parameters = StringUtils.split(mainUrl, ';');
                    if (ArrayUtils.isEmpty(parameters)) {
                        return sqlMetaData;
                    }
                    Map<String, String> parameterMap = new HashMap<>();
                    for (String parameter : parameters) {
                        if (StringUtils.isBlank(parameter) || parameter.indexOf('=') == -1) {
                            continue;
                        }
                        String[] pair = StringUtils.split(parameter, '=');
                        if (pair.length != 2) {
                            continue;
                        }
                        parameterMap.put(StringUtils.trim(StringUtils.lowerCase(pair[0])), StringUtils.trim(pair[1]));
                    }
                    String dbName = null;
                    if (parameterMap.size() > 0) {
                        if (parameterMap.containsKey("databasename")) {
                            dbName = parameterMap.get("databasename");
                        } else if (parameterMap.containsKey("instancename")) {
                            dbName = parameterMap.get("instancename");
                        } else if (!parameterMap.containsKey("instancename")) {
                            dbName = parameters[0];
                        }
                    }
                    sqlMetaData.setDbName(dbName);
                    return sqlMetaData;
                } catch (Throwable e) {
                    return null;
                }
            }
        },
        DB2("jdbc:db2://") {
            @Override
            public SqlMetaData readMetaData(String url) {
                String mainUrl = getMainUrl(url);
                SqlMetaData sqlMetaData = new SqlMetaData();
                sqlMetaData.setDbType(DB2);
                sqlMetaData.setMainUrl(mainUrl);
                sqlMetaData.setPrefix(getMatchedPrefix(url));
                int index;
                if ((index = mainUrl.indexOf(':')) != -1) {
                    mainUrl = mainUrl.substring(index + 1);
                    sqlMetaData.setDbName(StringUtils.substringAfter(mainUrl, "/"));
                } else {
                    sqlMetaData.setDbName(mainUrl);
                }
                return sqlMetaData;
            }
        },
        POSTGRESQL("jdbc:postgresql://") {
            @Override
            SqlMetaData readMetaData(String url) {
                String mainUrl = getMainUrl(url);
                SqlMetaData sqlMetaData = new SqlMetaData();
                sqlMetaData.setDbType(POSTGRESQL);
                sqlMetaData.setMainUrl(mainUrl);
                sqlMetaData.setPrefix(getMatchedPrefix(url));
                int index;
                if ((index = mainUrl.indexOf(':')) != -1) {
                    mainUrl = mainUrl.substring(index + 1);
                    sqlMetaData.setDbName(StringUtils.substringAfter(mainUrl, "/"));
                } else {
                    sqlMetaData.setDbName(StringUtils.lowerCase(mainUrl));
                }
                return sqlMetaData;
            }
        },
        POLARDB("jdbc:polardb://") {
            @Override
            SqlMetaData readMetaData(String url) {
                String mainUrl = getMainUrl(url);
                SqlMetaData sqlMetaData = new SqlMetaData();
                sqlMetaData.setDbType(POLARDB);
                sqlMetaData.setDbName(StringUtils.substringAfter(mainUrl, "/"));
                sqlMetaData.setPrefix(getMatchedPrefix(url));
                sqlMetaData.setMainUrl(mainUrl);
                return sqlMetaData;
            }
        },
        HIVE("jdbc:hive2://") {
            @Override
            SqlMetaData readMetaData(String url) {
                String mainUrl = getMainUrl(url);
                SqlMetaData sqlMetaData = new SqlMetaData();
                sqlMetaData.setDbType(HIVE);
                sqlMetaData.setMainUrl(mainUrl);
                sqlMetaData.setPrefix(getMatchedPrefix(url));
                int index;
                if ((index = mainUrl.indexOf(':')) != -1) {
                    mainUrl = mainUrl.substring(index + 1);
                    sqlMetaData.setDbName(StringUtils.substringAfter(mainUrl, "/"));
                } else {
                    sqlMetaData.setDbName(mainUrl);
                }
                return sqlMetaData;
            }
        },
        ;

        protected final String[] prefixs;

        DbType(String... prefixs) {
            if (ArrayUtils.isEmpty(prefixs)) {
                this.prefixs = new String[0];
            } else {
                this.prefixs = prefixs;
            }
        }

        abstract SqlMetaData readMetaData(String url);

        public boolean isThis(String url) {
            return Arrays.stream(prefixs).anyMatch(prefix -> StringUtils.startsWith(url, prefix));

        }

        public String getMainUrl(String url) {
            String simpleURL = getSimpleURL(url);
            return cutParameters(simpleURL);
        }

        private String cutParameters(String url) {
            return StringUtils.substringBefore(url, "?");
        }

        public String getSimpleURL(String url) {
            for (String prefix : prefixs) {
                if (StringUtils.startsWith(url, prefix)) {
                    return url.substring(prefix.length());
                }
            }
            return url;
        }

        public String getMatchedPrefix(String url) {
            return Arrays.stream(prefixs).filter(prefix -> StringUtils.startsWith(url, prefix)).findFirst().orElse(url);
        }

        public static DbType guessDbType(String url) {
            for (DbType dbType : values()) {
                if (dbType.isThis(url)) {
                    return dbType;
                }
            }
            return null;
        }
    }
}
