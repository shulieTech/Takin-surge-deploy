package io.shulie.takin.kafka.receiver.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.thread.NamedThreadFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.shulie.takin.kafka.receiver.constant.web.ContextSourceEnum;
import io.shulie.takin.kafka.receiver.constant.web.TakinClientAuthConstant;
import io.shulie.takin.kafka.receiver.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receiver.entity.TenantInfo;
import io.shulie.takin.kafka.receiver.entity.TroDept;
import io.shulie.takin.sdk.kafka.MessageReceiveCallBack;
import io.shulie.takin.sdk.kafka.MessageReceiveService;
import io.shulie.takin.sdk.kafka.entity.MessageEntity;
import io.shulie.takin.sdk.kafka.impl.KafkaSendServiceFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class WebKafkaReceiver implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(WebKafkaReceiver.class);

    @Resource
    private IApplicationMntService iApplicationMntService;
    @Resource
    private IUploadInterfaceDataService iUploadInterfaceDataService;
    @Resource
    private IApplicationApiManageService iApplicationApiManageService;
    @Resource
    private IShadowJobConfigService iShadowJobConfigService;
    @Resource
    private IAppAgentConfigReportService iAppAgentConfigReportService;
    @Resource
    private IApplicationMiddlewareService iApplicationMiddlewareService;
    @Resource
    private IAgentReportService iAgentReportService;
    @Resource
    private ITenantInfoService iTenantInfoService;
    @Resource
    private ITroDeptService iTroDeptService;
    @Resource
    private IPerformanceThreadDataService iPerformanceThreadDataService;

    private Map<String, TenantInfo> tenantAppKeyMap = new HashMap<>();
    private Map<String, TroDept> deptMap = new HashMap<>();

    private TenantCommonExt dealHeader(Map<String, Object> headers) {
        Object tenantAppKey = headers.get("tenantAppKey");
        Object userId = headers.get("userId");
        Object envCode = headers.get("envCode");
        String agentExpand = headers.get("agentExpand") == null ? null : headers.get("agentExpand").toString();

        TenantCommonExt tenantCommonExt = new TenantCommonExt();
        tenantCommonExt.setTenantAppKey(tenantAppKey == null ? "" : tenantAppKey.toString());
        tenantCommonExt.setEnvCode(envCode == null ? "test" : envCode.toString());
        tenantCommonExt.setSource(ContextSourceEnum.AGENT.getCode());
        tenantCommonExt.setUserId(userId == null ? null : Long.parseLong(userId.toString()));

        //设置租户信息
        if (tenantCommonExt.getTenantAppKey() != null) {
            if (tenantAppKeyMap.containsKey(tenantCommonExt.getTenantAppKey())) {
                TenantInfo tenantInfo = tenantAppKeyMap.get(tenantCommonExt.getTenantAppKey());
                tenantCommonExt.setTenantCode(tenantInfo.getCode());
                tenantCommonExt.setTenantId(tenantInfo.getId());
            } else {
                QueryWrapper<TenantInfo> tenantInfoQueryWrapper = new QueryWrapper<>();
                tenantInfoQueryWrapper.lambda().eq(TenantInfo::getKey, tenantCommonExt.getTenantAppKey());
                tenantInfoQueryWrapper.lambda().eq(TenantInfo::getIsDeleted, 0);
                List<TenantInfo> tenantInfos = iTenantInfoService.list(tenantInfoQueryWrapper);
                if (CollectionUtil.isNotEmpty(tenantInfos)) {
                    TenantInfo tenantInfo = tenantInfos.get(0);
                    tenantCommonExt.setTenantCode(tenantInfo.getCode());
                    tenantCommonExt.setTenantId(tenantInfo.getId());
                    tenantAppKeyMap.put(tenantCommonExt.getTenantAppKey(), tenantInfo);
                }
            }
        }
        //设置部门信息
        if (StringUtils.isNotBlank(agentExpand) && tenantCommonExt.getTenantId() != null) {
            try {
                JSONObject jsonObject = JSONObject.parseObject(agentExpand);
                String deptLowest = jsonObject.getString(TakinClientAuthConstant.HEADER_DEPT_LOWEST);
                if (deptMap.containsKey(deptLowest)) {
                    TroDept troDept = deptMap.get(deptLowest);
                    tenantCommonExt.setDeptId(troDept.getId());
                } else {
                    QueryWrapper<TroDept> troDeptQueryWrapper = new QueryWrapper<>();
                    troDeptQueryWrapper.lambda().eq(TroDept::getCode, deptLowest);
                    troDeptQueryWrapper.lambda().eq(TroDept::getIsDeleted, 0);
                    troDeptQueryWrapper.lambda().eq(TroDept::getTenantId, tenantCommonExt.getTenantId());
                    List<TroDept> deptList = iTroDeptService.list(troDeptQueryWrapper);
                    if (CollectionUtil.isNotEmpty(deptList)) {
                        TroDept troDept = deptList.get(0);
                        tenantCommonExt.setDeptId(troDept.getId());
                        deptMap.put(deptLowest, troDept);
                    }
                }
            } catch (Exception e) {
                log.warn("部门信息解析异常:" + e.getMessage());
            }
        }
        return tenantCommonExt;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ScheduledExecutorService kafkaReceivePool = Executors.newScheduledThreadPool(10, new NamedThreadFactory("web_kafka_receive", true));

        log.info("web开始kafka消息监听");
        kafkaReceivePool.execute(() -> {
            MessageReceiveService messageReceiveService = new KafkaSendServiceFactory().getKafkaMessageReceiveInstance();
            messageReceiveService.receive(ListUtil.of("stress-test-agent-performance-basedata"), new MessageReceiveCallBack() {
                @Override
                public void success(MessageEntity messageEntity) {
                    String string = JSONObject.toJSONString(messageEntity.getBody());
                    iPerformanceThreadDataService.dealMessage(string, dealHeader(messageEntity.getHeaders()));
                }

                @Override
                public void fail(String errorMessage) {
                    log.error("性能分析接口，接收kafka消息失败:{}", errorMessage);
                }
            });
        });

        kafkaReceivePool.execute(() -> {
            MessageReceiveService messageReceiveService = new KafkaSendServiceFactory().getKafkaMessageReceiveInstance();
            messageReceiveService.receive(ListUtil.of("stress-test-confcenter-interface-add-interfaceData"), new MessageReceiveCallBack() {
                @Override
                public void success(MessageEntity messageEntity) {
                    iUploadInterfaceDataService.dealMessage(JSONObject.toJSONString(messageEntity.getBody()), dealHeader(messageEntity.getHeaders()));
                }

                @Override
                public void fail(String errorMessage) {
                    log.error("应用上报接口，接收kafka消息失败:{}", errorMessage);
                }
            });
        });

        kafkaReceivePool.execute(() -> {
            MessageReceiveService messageReceiveService = new KafkaSendServiceFactory().getKafkaMessageReceiveInstance();
            messageReceiveService.receive(ListUtil.of("stress-test-agent-api-register"), new MessageReceiveCallBack() {
                @Override
                public void success(MessageEntity messageEntity) {
                    iApplicationApiManageService.dealMessage(messageEntity.getBody(), dealHeader(messageEntity.getHeaders()));
                }

                @Override
                public void fail(String errorMessage) {
                    log.error("入口规则接口，接收kafka消息失败:{}", errorMessage);
                }
            });
        });

        kafkaReceivePool.execute(() -> {
            MessageReceiveService messageReceiveService = new KafkaSendServiceFactory().getKafkaMessageReceiveInstance();
            messageReceiveService.receive(ListUtil.of("stress-test-application-agent-access-status"), new MessageReceiveCallBack() {
                @Override
                public void success(MessageEntity messageEntity) {
                    String body = JSONObject.toJSONString(messageEntity.getBody());
                }

                @Override
                public void fail(String errorMessage) {
                    log.error("定时上报接口状态接口，接收kafka消息失败:{}", errorMessage);
                }
            });
        });

        kafkaReceivePool.execute(() -> {
            MessageReceiveService messageReceiveService = new KafkaSendServiceFactory().getKafkaMessageReceiveInstance();
            messageReceiveService.receive(ListUtil.of("stress-test-shadow-job-update"), new MessageReceiveCallBack() {
                @Override
                public void success(MessageEntity messageEntity) {
                    String body = JSONObject.toJSONString(messageEntity.getBody());
                    iShadowJobConfigService.dealMessage(body, dealHeader(messageEntity.getHeaders()));
                }

                @Override
                public void fail(String errorMessage) {
                    log.error("上报影子job接口，接收kafka消息失败:{}", errorMessage);
                }
            });
        });

        kafkaReceivePool.execute(() -> {
            MessageReceiveService messageReceiveService = new KafkaSendServiceFactory().getKafkaMessageReceiveInstance();
            messageReceiveService.receive(ListUtil.of("stress-test-agent-push-application-config"), new MessageReceiveCallBack() {
                @Override
                public void success(MessageEntity messageEntity) {
                    String body = JSONObject.toJSONString(messageEntity.getBody());
                    iAppAgentConfigReportService.dealMessage(body, dealHeader(messageEntity.getHeaders()));
                }

                @Override
                public void fail(String errorMessage) {
                    log.error("配置信息上报接口，接收kafka消息失败:{}", errorMessage);
                }
            });
        });

        kafkaReceivePool.execute(() -> {
            MessageReceiveService messageReceiveService = new KafkaSendServiceFactory().getKafkaMessageReceiveInstance();
            messageReceiveService.receive(ListUtil.of("stress-test-agent-push-application-middleware"), new MessageReceiveCallBack() {
                @Override
                public void success(MessageEntity messageEntity) {
                    String body = JSONObject.toJSONString(messageEntity.getBody());
                    iApplicationMiddlewareService.dealMessage(body, dealHeader(messageEntity.getHeaders()));
                }

                @Override
                public void fail(String errorMessage) {
                    log.error("中间件信息上报接口，接收kafka消息失败:{}", errorMessage);
                }
            });
        });

        kafkaReceivePool.execute(() -> {
            MessageReceiveService messageReceiveService = new KafkaSendServiceFactory().getKafkaMessageReceiveInstance();
            messageReceiveService.receive(ListUtil.of("stress-test-confcenter-applicationmnt-update-applicationagent"), new MessageReceiveCallBack() {
                @Override
                public void success(MessageEntity messageEntity) {
                    Map body = messageEntity.getBody();
                    iApplicationMntService.dealAgentVersionMessage(body.get("appName") == null ? null : body.get("appName").toString(),
                            body.get("agentVersion") == null ? null : body.get("agentVersion").toString(),
                            body.get("pradarVersion") == null ? null : body.get("pradarVersion").toString(), dealHeader(messageEntity.getHeaders()));
                }

                @Override
                public void fail(String errorMessage) {
                    log.error("agent版本信息上报接口，接收kafka消息失败:{}", errorMessage);
                }
            });
        });

        kafkaReceivePool.execute(() -> {
            MessageReceiveService messageReceiveService = new KafkaSendServiceFactory().getKafkaMessageReceiveInstance();
            messageReceiveService.receive(ListUtil.of("stress-test-application-center-app-info"), new MessageReceiveCallBack() {
                @Override
                public void success(MessageEntity messageEntity) {
                    iApplicationMntService.dealAddApplicationMessage(JSON.toJSONString(messageEntity.getBody()), dealHeader(messageEntity.getHeaders()));
                }

                @Override
                public void fail(String errorMessage) {
                    log.error("agent添加应用接口，接收kafka消息失败:{}", errorMessage);
                }
            });
        });

        kafkaReceivePool.execute(() -> {
            MessageReceiveService messageReceiveService = new KafkaSendServiceFactory().getKafkaMessageReceiveInstance();
            messageReceiveService.receive(ListUtil.of("stress-test-api-agent-heartbeat"), new MessageReceiveCallBack() {
                @Override
                public void success(MessageEntity messageEntity) {
                    iAgentReportService.dealMessage(JSON.toJSONString(messageEntity.getBody()), dealHeader(messageEntity.getHeaders()));
                }

                @Override
                public void fail(String errorMessage) {
                    log.error("agent应用接口，接收kafka消息失败:{}", errorMessage);
                }
            });
        });
    }
}
