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

package io.shulie.surge.data.deploy.pradar.parser.db;


import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.db2.parser.DB2StatementParser;
import com.alibaba.druid.sql.dialect.db2.visitor.DB2SchemaStatVisitor;
import com.alibaba.druid.sql.dialect.hive.parser.HiveStatementParser;
import com.alibaba.druid.sql.dialect.hive.visitor.HiveSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleStatementParser;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.postgresql.parser.PGSQLStatementParser;
import com.alibaba.druid.sql.dialect.postgresql.visitor.PGSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerStatementParser;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerSchemaStatVisitor;
import com.alibaba.druid.sql.parser.SQLParserFeature;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class SqlParser {

    public String parse(RpcBased rpcBased) {

        String sql = rpcBased.getCallbackMsg();
        if (StringUtils.isBlank(sql)) {
            return "";
        }

        SQLStatementParser parser = null;

        if (StringUtils.equalsIgnoreCase(rpcBased.getMiddlewareName(), "mysql")) {
            parser = new MySqlStatementParser(sql, SQLParserFeature.KeepComments);
        } else if (StringUtils.equalsIgnoreCase(rpcBased.getMiddlewareName(), "oracle")) {
            parser = new OracleStatementParser(sql, SQLParserFeature.KeepComments);
        } else if (StringUtils.equalsIgnoreCase(rpcBased.getMiddlewareName(), "db2")) {
            parser = new DB2StatementParser(sql, SQLParserFeature.KeepComments);
        } else if (StringUtils.equalsIgnoreCase(rpcBased.getMiddlewareName(), "sqlserver")) {
            parser = new SQLServerStatementParser(sql, SQLParserFeature.KeepComments);
        } else if (StringUtils.equalsIgnoreCase(rpcBased.getMiddlewareName(), "postgresql")) {
            parser = new PGSQLStatementParser(sql, SQLParserFeature.KeepComments);
        } else if (StringUtils.equalsIgnoreCase(rpcBased.getMiddlewareName(), "hive")) {
            parser = new HiveStatementParser(sql, SQLParserFeature.KeepComments);
        }

        if (parser == null) {
            return "";
        }

        StringBuilder tableNameBuilder = new StringBuilder();
        try {
            final List<SQLStatement> sqlStatements = parser.parseStatementList();

            for (final SQLStatement sqlStatement : sqlStatements) {
                SchemaStatVisitor visitor = null;
                if (StringUtils.equalsIgnoreCase(rpcBased.getMiddlewareName(), "mysql")) {
                    visitor = new MySqlSchemaStatVisitor();
                } else if (StringUtils.equalsIgnoreCase(rpcBased.getMiddlewareName(), "oracle")) {
                    visitor = new OracleSchemaStatVisitor();
                } else if (StringUtils.equalsIgnoreCase(rpcBased.getMiddlewareName(), "db2")) {
                    visitor = new DB2SchemaStatVisitor();
                } else if (StringUtils.equalsIgnoreCase(rpcBased.getMiddlewareName(), "sqlserver")) {
                    visitor = new SQLServerSchemaStatVisitor();
                } else if (StringUtils.equalsIgnoreCase(rpcBased.getMiddlewareName(), "postgresql")) {
                    visitor = new PGSchemaStatVisitor();
                } else if (StringUtils.equalsIgnoreCase(rpcBased.getMiddlewareName(), "hive")) {
                    visitor = new HiveSchemaStatVisitor();
                }
                if (visitor == null) {
                    continue;
                }

                sqlStatement.accept(visitor);
                final Map<TableStat.Name, TableStat> tableMap = visitor.getTables();
                for (Map.Entry<TableStat.Name, TableStat> nameTableStatEntry : tableMap.entrySet()) {
                    tableNameBuilder.append(nameTableStatEntry.getKey().toString()).append(',');
                }
            }
        } catch (Exception e) {
        }
        if (tableNameBuilder.length() > 0) {
            tableNameBuilder.deleteCharAt(tableNameBuilder.length() - 1);
        }
        return tableNameBuilder.toString();
    }
}
