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

package io.shulie.surge.data.deploy.pradar.link;

import io.shulie.surge.data.deploy.pradar.parser.MiddlewareType;

public interface LinkSqlContants {
    String UNKNOW_APP = "UNKNOWN";

    String LINK_EDGE_INSERT_SQL = "insert ignore into t_amdb_pradar_link_edge(link_id,service,method,extend,app_name,trace_app_name,server_app_name,rpc_type,log_type,middleware_name,entrance_id,from_app_id,to_app_id,edge_id,gmt_modify) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,now())";

    String LINK_NODE_INSERT_SQL = "insert ignore into t_amdb_pradar_link_node(link_id,app_name,trace_app_name,middleware_name,extend,app_id,gmt_modify) values(?,?,?,?,?,?,now())";

    String LINK_NODE_DELETE_SQL = "delete from t_amdb_pradar_link_node where";

    String LINK_NODE_SELECT_SQL = "select id from t_amdb_pradar_link_node where app_name='UNKNOWN' and gmt_modify <= DATE_SUB(now(), INTERVAL 1 HOUR)";

    String LINK_EDGE_SELECT_SQL = "select id from t_amdb_pradar_link_edge where to_app_id in (select app_id from t_amdb_pradar_link_node where app_name='UNKNOWN' and gmt_modify <= DATE_SUB(now(), INTERVAL 1 HOUR) and link_id='%s')";

    String LINK_EDGE_DELETE_SQL = "delete from t_amdb_pradar_link_edge where";

    String LINK_EDGE_MQ_SELECT_SQL = "select * from t_amdb_pradar_link_edge where rpc_type=" + MiddlewareType.TYPE_MQ + " and link_id=";
}
