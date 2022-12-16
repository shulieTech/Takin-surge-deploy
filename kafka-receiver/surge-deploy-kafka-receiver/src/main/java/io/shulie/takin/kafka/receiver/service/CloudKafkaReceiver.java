package io.shulie.takin.kafka.receiver.service;

import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson.JSONObject;
import io.shulie.takin.kafka.receiver.dto.cloud.MetricsInfo;
import io.shulie.takin.sdk.kafka.MessageReceiveCallBack;
import io.shulie.takin.sdk.kafka.MessageReceiveService;
import io.shulie.takin.sdk.kafka.entity.MessageEntity;
import io.shulie.takin.sdk.kafka.impl.KafkaSendServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Executors;

@Component
public class CloudKafkaReceiver implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(CloudKafkaReceiver.class);

    @Resource
    private IPressureService iPressureService;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("cloud开始监听stress-test-pressure-metrics-upload-old的数据");
        MessageReceiveService messageReceiveService = new KafkaSendServiceFactory().getKafkaMessageReceiveInstance();
        log.info("初始化完成，开始监听");
        List<String> topics = ListUtil.of("stress-test-pressure-metrics-upload-old");
        Executors.newCachedThreadPool().execute(()-> {
            messageReceiveService.receive(topics, new MessageReceiveCallBack() {
                @Override
                public void success(MessageEntity messageEntity) {
                    Object data = messageEntity.getBody().get("data");
                    Object jobId = messageEntity.getBody().get("jobId");
                    String dataString = JSONObject.toJSONString(data);
                    List<MetricsInfo> metricsInfos = JSONObject.parseArray(dataString, MetricsInfo.class);
                    iPressureService.upload(metricsInfos, Long.parseLong(jobId.toString()));
                }

                @Override
                public void fail(String errorMessage) {
                    log.error("接收kafka消息失败:{}", errorMessage);
                }
            });
        });
    }
}
