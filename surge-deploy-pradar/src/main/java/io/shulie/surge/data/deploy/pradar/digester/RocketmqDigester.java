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
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.pamirs.pradar.log.parser.constant.TenantConstants;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.common.batch.CountRotationPolicy;
import io.shulie.surge.data.common.batch.RotationBatch;
import io.shulie.surge.data.common.batch.TimedRotationPolicy;
import io.shulie.surge.data.runtime.common.remote.DefaultValue;
import io.shulie.surge.data.runtime.common.remote.Remote;
import io.shulie.surge.data.runtime.common.utils.ApiProcessor;
import io.shulie.surge.data.runtime.digest.DataDigester;
import io.shulie.surge.data.runtime.digest.DigestContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 日志原文按插入顺序直接存储-写入ClickHouse
 *
 * @author pamirs
 */
@Singleton
public class RocketmqDigester implements DataDigester<RpcBased> {
    private static final Logger logger = LoggerFactory.getLogger(RocketmqDigester.class);

    @Inject
    @Named("config.rocketmq.nameserver")
    private String nameserverAddr = "";
    @Inject
    @Named("config.rocketmq.topic")
    private String topic = "";

    @Inject
    @Named("config.rocketmq.batchSize")
    private Integer batchSize = 200;

    @Inject
    @DefaultValue("false")
    @Named("/pradar/config/rt/rocketmqDisable")
    private Remote<Boolean> rocketmqDisable;


    private transient AtomicBoolean isRunning = new AtomicBoolean(false);


    private DefaultMQProducer producer;

    private RotationBatch rotationBatch;

    public void init() {
        if (StringUtils.isNotBlank(nameserverAddr) && StringUtils.isNotBlank(nameserverAddr)) {

            //Instantiate with a producer group name.
            producer = new DefaultMQProducer(topic + "|producer");
            // Specify name server addresses.
            producer.setNamesrvAddr(nameserverAddr);
            // set msg timeout
            producer.setSendMsgTimeout(15000);

            //Launch the instance.
            try {
                producer.start();
            } catch (MQClientException e) {
                logger.error("Init mq client failed.", e);
                producer = null;
            }

            rotationBatch = new RotationBatch(new CountRotationPolicy(batchSize), new TimedRotationPolicy(2, TimeUnit.SECONDS));
            rotationBatch.batchSaver(new RotationBatch.BatchSaver<Message>() {
                @Override
                public boolean saveBatch(LinkedBlockingQueue<Message> batchMessage) {

                    if (CollectionUtils.isNotEmpty(batchMessage)) {
                        //发送MQ
                        ListSplitter splitter = new ListSplitter(Lists.newArrayList(batchMessage));
                        while (splitter.hasNext()) {
                            //安装4m切割消息
                            List<Message> listItem = splitter.next();
                            //发送消息
                            try {
                                SendResult sendResult = producer.send(listItem);
                            } catch (Exception e) {
                                logger.error("Producer message error.", e);
                            }
                        }
                    }
                    return true;
                }

                @Override
                public boolean shardSaveBatch(String key, LinkedBlockingQueue<Message> ObjectBatch) {
                    return false;
                }
            });
            rotationBatch.start();
        }
    }

    @Override
    public void digest(DigestContext<RpcBased> context) {
        if (rocketmqDisable.get()) {
            return;
        }
        if (isRunning.compareAndSet(false, true)) {
            init();
        }
        if (producer == null) {
            return;
        }
        RpcBased rpcBased = context.getContent();
        if (rpcBased == null) {
            return;
        }

        rpcBased.setDataLogTime(context.getProcessTime());
        if (context.getHeader().containsKey("uploadTime")) {
            rpcBased.setUploadTime((Long) context.getHeader().get("uploadTime"));
        }
        rpcBased.setReceiveHttpTime((Long) context.getHeader().get("receiveHttpTime"));
        //对于1.6以及之前的老版本探针,没有租户相关字段,根据应用名称获取租户配置,没有设默认值
        if (StringUtils.isBlank(rpcBased.getUserAppKey()) || TenantConstants.DEFAULT_USER_APP_KEY.equals(rpcBased.getUserAppKey())) {
            rpcBased.setUserAppKey(ApiProcessor.getTenantConfigByAppName(rpcBased.getAppName()).get("tenantAppKey"));
        }
        if (StringUtils.isBlank(rpcBased.getEnvCode())) {
            rpcBased.setEnvCode(ApiProcessor.getTenantConfigByAppName(rpcBased.getAppName()).get("envCode"));
        }

        try {
            if (rpcBased == null) {
                logger.warn("message is null");
            }
            Message message = new Message(topic, rpcBased.getUserAppKey(), JSON.toJSONBytes(rpcBased));
            //设置traceId+invoke_id作为rocketmq的消息唯一标识,用于排查问题
            message.setKeys(
                    new StringBuilder(rpcBased.getUserAppKey()).append("_").append(rpcBased.getTraceId()).append("_")
                            .append(rpcBased.getInvokeId()).toString());
            rotationBatch.addBatch(message);
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
            this.producer.shutdown();
        } catch (Throwable e) {
            logger.error("producer stop fail");
        }
    }

    public Remote<Boolean> getRocketmqDisable() {
        return rocketmqDisable;
    }

    public void setRocketmqDisable(Remote<Boolean> rocketmqDisable) {
        this.rocketmqDisable = rocketmqDisable;
    }
}

@Slf4j
class ListSplitter implements Iterator<List<Message>> {
    private int SIZE_LIMIT = 1024 * 1024 * 3 + 512;
    private final List<Message> messages;
    private int currIndex;

    public ListSplitter(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public boolean hasNext() {
        return currIndex < messages.size();
    }

    @Override
    public List<Message> next() {
        int startIndex = getStartIndex();
        int nextIndex = startIndex;
        int totalSize = 0;
        for (; nextIndex < messages.size(); nextIndex++) {
            Message message = messages.get(nextIndex);
            int tmpSize = calcMessageSize(message);
            if (tmpSize + totalSize > SIZE_LIMIT) {
                break;
            } else {
                totalSize += tmpSize;
            }
        }
        List<Message> subList = messages.subList(startIndex, nextIndex);
        currIndex = nextIndex;
        // // 输出大小
        // int sum = subList.stream().mapToInt(this::calcMessageSize).sum();
        // log.info("大小：{}",sum);
        return subList;
    }

    private int getStartIndex() {
        Message currMessage = messages.get(currIndex);
        int tmpSize = calcMessageSize(currMessage);
        while (tmpSize > SIZE_LIMIT) {
            currIndex += 1;
            Message message = messages.get(currIndex);
            tmpSize = calcMessageSize(message);
        }
        return currIndex;
    }

    private int calcMessageSize(Message message) {
        byte[] bytes = MessageDecoder.encodeMessage(message);
        return bytes.length + 20;
    }
}


