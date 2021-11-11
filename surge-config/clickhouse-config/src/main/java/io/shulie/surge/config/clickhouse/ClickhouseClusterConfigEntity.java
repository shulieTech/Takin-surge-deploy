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

package io.shulie.surge.config.clickhouse;

import java.io.Serializable;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import static io.shulie.surge.config.clickhouse.ClickhouseTemplateManager.PROD_ENV;

/**
 * 租户存储方案配置
 */
@Data
public class ClickhouseClusterConfigEntity implements Serializable {
    private String userAppKey;      // 租户标识
    private String tenantName;      // 租户名称
    private String envCode;         // 环境名称
    private String clusterName;     // 集群名称
    private String clusterDesc;     // 租户说明
    private String clusterAddress;  // 租户地址
    private String userName;        // 帐号
    private String password;        // 密码
    private String ttl;             // 数据有效期
    private Integer batchCount;     // 执行批数

    public String getTtl() {
        return StringUtils.isBlank(ttl) ? "3" : ttl;
    }

    public boolean isProd() {
        return PROD_ENV.equals(envCode);
    }
}