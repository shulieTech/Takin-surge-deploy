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

import java.math.BigDecimal;

/**
 * 场景业务活动入口统计
 */
public class ReportActivityModel {

    private String reportId;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * rpcType
     */
    private String rpcType;

    /**
     * 最小耗时
     */
    private Long minCost;

    /**
     * 最大耗时
     */
    private Long maxCost;

    /**
     * 总耗时
     */
    private Long sumCost;

    /**
     * 请求数
     */
    private Long reqCnt;

    /**
     * 平均耗时
     */
    private BigDecimal avgCost;

    public static String getCols() {
        return " (report_id, app_name, service_name, method_name, rpc_type, req_cnt, max_cost, min_cost, sum_cost"
            + ", avg_cost, gmt_create)";
    }

    public static String getParamCols() {
        return "(?,?,?,?,?,?,?,?,?,?,now())";
    }

    public static final String INSERT_SQL = "INSERT INTO t_amdb_report_activity " + getCols() + " VALUES " + getParamCols();

    public Object[] getValues() {
        return new Object[] {reportId, appName, serviceName, methodName, rpcType, reqCnt, maxCost, minCost, sumCost, avgCost};
    }

    public static final String EXISTS_SQL = "select id from t_amdb_report_activity where report_id = '%s' limit 1";

    public static final String UPDATE_STATE_SQL = "update t_amdb_report_activity set state = '1', gmt_update = now() where report_id = ? "
        + " and app_name = ? and service_name = ? and method_name = ? and rpc_type = ? ";

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getRpcType() {
        return rpcType;
    }

    public void setRpcType(String rpcType) {
        this.rpcType = rpcType;
    }

    public Long getMinCost() {
        return minCost;
    }

    public void setMinCost(Long minCost) {
        this.minCost = minCost;
    }

    public Long getMaxCost() {
        return maxCost;
    }

    public void setMaxCost(Long maxCost) {
        this.maxCost = maxCost;
    }

    public Long getSumCost() {
        return sumCost;
    }

    public void setSumCost(Long sumCost) {
        this.sumCost = sumCost;
    }

    public Long getReqCnt() {
        return reqCnt;
    }

    public void setReqCnt(Long reqCnt) {
        this.reqCnt = reqCnt;
    }

    public BigDecimal getAvgCost() {
        return avgCost;
    }

    public void setAvgCost(BigDecimal avgCost) {
        this.avgCost = avgCost;
    }

    public String hashKey() {
        return appName + "@" + serviceName + "@" + methodName + "@" + rpcType;
    }
}