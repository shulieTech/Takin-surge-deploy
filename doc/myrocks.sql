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

CREATE TABLE default.t_trace(`appName` String,`entranceId` Nullable(String),`entranceNodeId` Nullable(String),`traceId` String,`level` Nullable(Int8),`parentIndex` Nullable(Int8),`index` Nullable(Int8),`rpcId` String,`rpcType` Nullable(Int8),`logType` Nullable(Int8),`traceAppName` Nullable(String),`upAppName` Nullable(String),`startTime` Int64,`cost` Int16,`middlewareName` Nullable(String),`serviceName` Nullable(String),`methodName` Nullable(String),`remoteIp` Nullable(String),`port` Nullable(Int8),`resultCode` Nullable(String),`requestSize` Nullable(String),`responseSize` Nullable(String),`request` Nullable(String),`response` Nullable(String),`clusterTest` Nullable(String),`callbackMsg` Nullable(String),`samplingInterval` Nullable(String),`localId` Nullable(String),`attributes` Nullable(String),`localAttributes` Nullable(String),`async` Nullable(String),`version` Nullable(String),`hostIp` Nullable(String),`agentId` Nullable(String),`startDate` DateTime,`create_date` Date DEFAULT toDate(now()),dateToMin Nullable(Int64),sqlUniqueId Nullable(String),extend01 Nullable(String),extend02 Nullable(String),extend03 Nullable(String),extend04 Nullable(String),extend05 Nullable(String),extend06 Nullable(String),extend07 Nullable(String),extend08 Nullable(String),extend09 Nullable(String),extend10 Nullable(String),extend11 Nullable(String),extend12 Nullable(String),extend13 Nullable(String),extend14 Nullable(String))ENGINE = MergeTree PARTITION BY toYYYYMMDD(startDate) ORDER BY (traceId, appName, rpcId) TTL startDate + toIntervalMonth(1) SETTINGS index_granularity = 8192