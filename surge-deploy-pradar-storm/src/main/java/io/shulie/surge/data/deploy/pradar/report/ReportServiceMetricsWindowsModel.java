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

package io.shulie.surge.data.deploy.pradar.report;

import java.util.Date;

/**
 * 场景业务活动接口窗口内统计
 */
public class ReportServiceMetricsWindowsModel extends ReportServiceMetricsModel {

    /**
     * 时间窗口
     */
    private Date timeWindow;

    /**
     * 采样后调用次数
     */
    private Long countAfterSimp;

    /**
     * 子耗时
     */
    private Long downstreamCost;

    /**
     * rpcId集合
     */
    private String rpcIds;

    public static String getCols() {
        return " (report_id, app_name, service_name, method_name, rpc_type, req_cnt, avg_cost, max_cost, min_cost, "
            + " sum_cost, gmt_create, entrance_app_name, entrance_service_name, entrance_method_name, entrance_rpc_type,"
            + " count_after_simp, down_stream_cost, time_window, rpc_ids)";
    }

    public static String getParamCols() {
        return "(?,?,?,?,?,?,?,?,?,?,now(),?,?,?,?,?,?,?,?)";
    }

    public static final String INSERT_SQL = "INSERT INTO t_amdb_interface_metrics " + getCols() + " VALUES " + getParamCols();

    public Object[] getValues() {
        return new Object[] {getReportId(), getAppName(), getServiceName(), getMethodName(), getRpcType(), getReqCnt(),
            getAvgCost(), getMaxCost(), getMinCost(), getSumCost(), getEntranceAppName(), getEntranceServiceName(),
            getEntranceMethodName(), getEntranceRpcType(), getCountAfterSimp(), getDownstreamCost(), getTimeWindow(),
            getRpcIds()};
    }

    public static final String SELF_COST_SQL = "select avg(avg_cost) avgCost from t_amdb_interface_metrics "
        + " where report_id = '%s' and app_name= '%s' and service_name = '%s' and method_name = '%s' and rpc_type = '%s' "
        + " and entrance_app_name = '%s' and entrance_service_name = '%s' and entrance_method_name = '%s' and entrance_rpc_type = '%s'";

    public Date getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(Date timeWindow) {
        this.timeWindow = timeWindow;
    }

    public Long getCountAfterSimp() {
        return countAfterSimp;
    }

    public void setCountAfterSimp(Long countAfterSimp) {
        this.countAfterSimp = countAfterSimp;
    }

    public Long getDownstreamCost() {
        return downstreamCost;
    }

    public void setDownstreamCost(Long downstreamCost) {
        this.downstreamCost = downstreamCost;
    }

    public String getRpcIds() {
        return rpcIds;
    }

    public void setRpcIds(String rpcIds) {
        this.rpcIds = rpcIds;
    }
}
