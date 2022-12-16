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

package io.shulie.takin.kafka.receiver.dto.amdb;


import lombok.Data;

/**
 * agent实例状态模型
 *
 * @author vincent
 */
@Data
public class InstanceStatusModel{

    //agent编号
    private String agentId;
    //地址
    private String address;
    //主机名
    private String host;
    //名称
    private String name;
    //agent版本
    private String agentVersion;
    //agent语言
    private String agentLanguage;
    //进程编号
    private String pid;
    //错误编码
    private String errorCode;
    //错误信息
    private String errorMsg;
    //探针版本
    private String simulatorVersion;
    //探针状态
    private String agentStatus;
    //agent 应用jvm启动参数，是一个list的json字符串
    private String jvmArgs;
    //应用服务器的jdk版本
    private String jdk;
    //应用java home下的tools.jar包路径，如果不存在，是个空字符串
    private String toolsJarPath;
    // 租户标识
    private String tenantAppKey;
    // 环境标识
    private String envCode;
    // 用户标识
    private String userId;

    //
    private String cacheKey;

    private String appName;

}

