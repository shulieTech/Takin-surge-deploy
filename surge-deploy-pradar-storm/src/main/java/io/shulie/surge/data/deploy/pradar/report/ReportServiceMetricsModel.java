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
 * 场景业务活动接口统计
 */
public class ReportServiceMetricsModel {

    private String reportId;
    /**
     * 应用名称
     */
    private String appName;
    /**
     * 接口名称
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
     * 入口应用名称
     */
    private String entranceAppName;
    /**
     * 入口接口名称
     */
    private String entranceServiceName;
    /**
     * 入口方法名称
     */
    private String entranceMethodName;
    /**
     * 入口rpcType
     */
    private String entranceRpcType;
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
     * 平均自耗时
     */
    private BigDecimal avgCost;
    /**
     * 业务活动平均耗时
     */
    private BigDecimal serviceAvgCost;

    public static String getCols() {
        return " (report_id, app_name, service_name, method_name, rpc_type, req_cnt, max_cost, min_cost, sum_cost, "
            + "gmt_create, entrance_app_name, entrance_service_name, entrance_method_name, entrance_rpc_type, service_avg_cost)";
    }

    public static String getParamCols() {
        return "(?,?,?,?,?,?,?,?,?,now(),?,?,?,?,?)";
    }

    public static final String INSERT_SQL = "INSERT INTO t_amdb_report_interface " + getCols() + " VALUES " + getParamCols();

    public Object[] getValues() {
        return new Object[] {reportId, appName, serviceName, methodName, rpcType, reqCnt, maxCost, minCost, sumCost
            , entranceAppName, entranceServiceName, entranceMethodName, entranceRpcType, serviceAvgCost};
    }

    /**
     * 更新service处理状态
     */
    public static final String UPDATE_SERVICE_STATUS = "update t_amdb_report_interface set state = '1', gmt_update = now() where report_id = ?"
        + " and app_name= ? and service_name = ? and method_name = ? and rpc_type = ? and entrance_app_name = ? "
        + " and entrance_service_name = ? and entrance_method_name = ? and entrance_rpc_type = ?";

    /**
     * 更新service的平均自耗时和耗时占比
     */
    public static final String UPDATE_SERVICE_SELF_COST = "update t_amdb_report_interface "
        + " set avg_cost = ?, cost_percent = (avg_cost * 100 / service_avg_cost) "
        + " where report_id = ? and app_name= ? and service_name = ? and method_name = ? and rpc_type = ? "
        + " and entrance_app_name = ? and entrance_service_name = ? and entrance_method_name = ? and entrance_rpc_type = ?";


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

    public String getEntranceAppName() {
        return entranceAppName;
    }

    public void setEntranceAppName(String entranceAppName) {
        this.entranceAppName = entranceAppName;
    }

    public String getEntranceServiceName() {
        return entranceServiceName;
    }

    public void setEntranceServiceName(String entranceServiceName) {
        this.entranceServiceName = entranceServiceName;
    }

    public String getEntranceMethodName() {
        return entranceMethodName;
    }

    public void setEntranceMethodName(String entranceMethodName) {
        this.entranceMethodName = entranceMethodName;
    }

    public String getEntranceRpcType() {
        return entranceRpcType;
    }

    public void setEntranceRpcType(String entranceRpcType) {
        this.entranceRpcType = entranceRpcType;
    }

    public BigDecimal getServiceAvgCost() {
        return serviceAvgCost;
    }

    public void setServiceAvgCost(BigDecimal serviceAvgCost) {
        this.serviceAvgCost = serviceAvgCost;
    }
}