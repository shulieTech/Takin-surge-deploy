package io.shulie.takin.kafka.receiver.service;

import cn.hutool.core.thread.NamedThreadFactory;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import io.shulie.takin.sdk.kafka.MessageReceiveCallBack;
import io.shulie.takin.sdk.kafka.MessageReceiveService;
import io.shulie.takin.sdk.kafka.entity.MessageEntity;
import io.shulie.takin.sdk.kafka.impl.KafkaSendServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class AmdbKafkaReceiver implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AmdbKafkaReceiver.class);
    @Resource
    private IAmdbAppInstanceService iAmdbAppInstanceService;
    @Resource
    private IAmdbAppInstanceStatusService iAmdbAppInstanceStatusService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ScheduledExecutorService kafkaReceivePool = Executors.newScheduledThreadPool(4, new NamedThreadFactory("amdb_kafka_receive", true));

        log.info("amdb开始kafka监听");
        kafkaReceivePool.execute(() -> {
            MessageReceiveService messageReceiveService = new KafkaSendServiceFactory().getKafkaMessageReceiveInstance();
            log.info("开始准备监听config-log-pradar-client的消息，开始设置topic");
            messageReceiveService.receive(Lists.newArrayList("stress-test-config-log-pradar-client"), new MessageReceiveCallBack() {
                @Override
                public void success(MessageEntity messageEntity) {
                    log.info("收到config-log-pradar-client的消息，" + JSON.toJSONString(messageEntity));
                    Map entityBody = messageEntity.getBody();
                    iAmdbAppInstanceService.dealPradarClientMessage(entityBody);
                }

                @Override
                public void fail(String errorMessage) {
                    log.error("节点client信息接收kafka消息出现异常，errorMessage:{}", errorMessage);
                }
            });
        });

        kafkaReceivePool.execute(() -> {
            MessageReceiveService messageReceiveService = new KafkaSendServiceFactory().getKafkaMessageReceiveInstance();
            messageReceiveService.receive(Lists.newArrayList("stress-test-config-log-pradar-status"), new MessageReceiveCallBack() {
                @Override
                public void success(MessageEntity messageEntity) {
                    Map entityBody = messageEntity.getBody();
                    log.info("接收到stress-test-config-log-pradar-status消息:{}", JSON.toJSONString(entityBody));
                    iAmdbAppInstanceStatusService.dealPradarStatusMessage(entityBody);
                }

                @Override
                public void fail(String errorMessage) {
                    log.error("节点status信息接收kafka消息出现异常，errorMessage:{}", errorMessage);
                }
            });
        });
    }
}
