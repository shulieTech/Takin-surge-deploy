package io.shulie.takin.kafka.receiver.service.impl;

import io.shulie.takin.kafka.receiver.constant.cloud.CallbackType;
import io.shulie.takin.kafka.receiver.constant.cloud.NotifyEventType;
import io.shulie.takin.kafka.receiver.dto.cloud.PressureExampleDto;
import io.shulie.takin.kafka.receiver.dto.cloud.PressureExampleHeartbeat;
import io.shulie.takin.kafka.receiver.entity.Pressure;
import io.shulie.takin.kafka.receiver.entity.PressureExample;
import io.shulie.takin.kafka.receiver.dao.cloud.PressureExampleMapper;
import io.shulie.takin.kafka.receiver.entity.PressureExampleEvent;
import io.shulie.takin.kafka.receiver.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 任务实例 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@Service
public class PressureExampleServiceImpl extends ServiceImpl<PressureExampleMapper, PressureExample> implements IPressureExampleService {

    @Resource
    private IPressureExampleEventService iPressureExampleEventService;
    @Resource
    private ICallbackService iCallbackService;
    @Resource
    private IPressureService iPressureService;
    @Resource
    private JsonService jsonService;

    @Override
    public void onHeartbeat(Long pressureExampleId) {
        // 基础信息准备
        StringBuilder callbackUrl = new StringBuilder();
        PressureExampleHeartbeat context = new PressureExampleHeartbeat();
        context.setData(getCallbackData(pressureExampleId, callbackUrl));
        // 创建回调
        iCallbackService.create(callbackUrl.toString(), CallbackType.PRESSURE_EXAMPLE_HEARTBEAT, jsonService.writeValueAsString(context));
        // 记录事件
        PressureExampleEvent pressureExampleEvent = new PressureExampleEvent();
        pressureExampleEvent.setContext("{}");
        pressureExampleEvent.setPressureExampleId(pressureExampleId);
        pressureExampleEvent.setType(NotifyEventType.PRESSURE_EXAMPLE_HEARTBEAT.getCode());
        iPressureExampleEventService.save(pressureExampleEvent);
    }

    public PressureExampleDto getCallbackData(long pressureExampleId, StringBuilder callbackUrl) {
        PressureExample pressureExampleEntity = this.getById(pressureExampleId);
        Pressure pressureEntity = iPressureService.getById(pressureExampleEntity.getPressureId());
        callbackUrl.append(pressureEntity.getCallbackUrl());
        return new PressureExampleDto()
                .setPressureId(pressureEntity.getId())
                .setResourceId(pressureEntity.getResourceId())
                .setPressureExampleId(pressureExampleEntity.getId())
                .setResourceExampleId(pressureExampleEntity.getResourceExampleId());
    }
}
