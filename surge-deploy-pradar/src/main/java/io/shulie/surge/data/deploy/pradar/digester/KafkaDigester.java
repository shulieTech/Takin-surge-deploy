
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

package io.shulie.surge.data.deploy.pradar.digester;

import com.alibaba.fastjson.JSON;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.pamirs.pradar.log.parser.constant.TenantConstants;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.deploy.pradar.common.PradarUtils;
import io.shulie.surge.data.runtime.common.remote.DefaultValue;
import io.shulie.surge.data.runtime.common.remote.Remote;
import io.shulie.surge.data.runtime.common.utils.ApiProcessor;
import io.shulie.surge.data.runtime.digest.DataDigester;
import io.shulie.surge.data.runtime.digest.DigestContext;
import io.shulie.surge.data.sink.kafka.KafkaSupport;
import io.shulie.surge.data.sink.rocketmq.RocketMQSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志原文按插入顺序直接存储-写入RocketMQ
 *
 * @author pamirs
 */
@Singleton
public class KafkaDigester implements DataDigester<RpcBased> {
    private static final Logger logger = LoggerFactory.getLogger(KafkaDigester.class);

    @Inject
    private KafkaSupport kafkaSupport;

    @Inject
    @DefaultValue("true")
    @Named("/pradar/config/rt/kafkaDisable")
    private Remote<Boolean> kafkaDisable;

    @Inject
    @DefaultValue("1")
    @Named("/pradar/config/rt/clickhouseSampling")
    private Remote<Integer> clickhouseSampling;

    @Inject
    @Named("config.trace.topic")
    private String topic;

    @Override
    public void digest(DigestContext<RpcBased> context) {
        if (kafkaDisable.get()) {
            return;
        }
        RpcBased rpcBased = context.getContent();
        try {
            if (rpcBased == null) {
                return;
            }
            if (!PradarUtils.isTraceSampleAccepted(rpcBased, clickhouseSampling.get())) {
                return;
            }

            rpcBased.setDataLogTime(context.getProcessTime());
            if (context.getHeader().containsKey("uploadTime")) {
                rpcBased.setUploadTime((Long) context.getHeader().get("uploadTime"));
            }
            if (context.getHeader().containsKey("receiveHttpTime")) {
                rpcBased.setReceiveHttpTime((Long) context.getHeader().get("receiveHttpTime"));
            }
            //对于1.6以及之前的老版本探针,没有租户相关字段,根据应用名称获取租户配置,没有设默认值
            if (StringUtils.isBlank(rpcBased.getUserAppKey()) || TenantConstants.DEFAULT_USER_APP_KEY.equals(rpcBased.getUserAppKey())) {
                rpcBased.setUserAppKey(ApiProcessor.getTenantConfigByAppName(rpcBased.getAppName()).get("tenantAppKey"));
            }
            if (StringUtils.isBlank(rpcBased.getEnvCode())) {
                rpcBased.setEnvCode(ApiProcessor.getTenantConfigByAppName(rpcBased.getAppName()).get("envCode"));
            }

            String jsonString = JSON.toJSONString(rpcBased);
            kafkaSupport.sendMq(topic, rpcBased.getTraceId(), jsonString);
        } catch (Throwable e) {
            logger.warn("fail to write rocketmq, log: " + rpcBased.getLog() + ", error:" + ExceptionUtils.getStackTrace(e));
        }
    }


    @Override
    public int threadCount() {
        return 1;
    }

    @Override
    public void stop() {
        try {
            this.kafkaSupport.stop();
        } catch (Throwable e) {
            logger.error("clickhouse stop fail");
        }
    }

    public Remote<Integer> getClickhouseSampling() {
        return clickhouseSampling;
    }
}
