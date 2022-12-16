package io.shulie.takin.kafka.receiver.service.impl;

import cn.hutool.core.collection.CollUtil;
import io.shulie.takin.kafka.receiver.constant.cloud.CallbackType;
import io.shulie.takin.kafka.receiver.constant.cloud.FormulaSymbol;
import io.shulie.takin.kafka.receiver.constant.cloud.FormulaTarget;
import io.shulie.takin.kafka.receiver.dto.cloud.MetricsInfo;
import io.shulie.takin.kafka.receiver.dto.cloud.SlaDto;
import io.shulie.takin.kafka.receiver.entity.*;
import io.shulie.takin.kafka.receiver.dao.cloud.SlaMapper;
import io.shulie.takin.kafka.receiver.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * Service Level Agreement(服务等级协议) 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@Service
public class SlaServiceImpl extends ServiceImpl<SlaMapper, Sla> implements ISlaService {
    private static final Logger log = LoggerFactory.getLogger(SlaServiceImpl.class);
    @Resource
    private IPressureService iPressureService;
    @Resource
    private IPressureExampleService iPressureExampleService;
    @Resource
    private IResourceExampleService iResourceService;
    @Resource
    private ISlaEventService iSlaEventService;
    @Resource
    private JsonService jsonService;
    @Resource
    private ICallbackService callbackService;

    @Override
    public List<SlaEvent> check(Long pressureId, Long pressureExampleId, List<MetricsInfo> metricsInfoList) {
        // 业务结果
        List<SlaEvent> result = new ArrayList<>();
        // 获取条件
        List<Sla> slaEntityList = this.lambdaQuery().eq(Sla::getPressureId, pressureId).list();
        // 逐个数据判断
        for (MetricsInfo metricsInfo : metricsInfoList) {
            // 对应的条件列表
            List<Sla> conditionList = slaEntityList.stream()
                    .filter(t -> "".equals(t.getRef()) || t.getRef().equals(metricsInfo.getTransaction()))
                    .collect(Collectors.toList());
            // 逐个条件判断
            for (Sla condition : conditionList) {
                FormulaSymbol formulaSymbol = FormulaSymbol.of(condition.getFormulaSymbol());
                FormulaTarget formulaTarget = FormulaTarget.of(condition.getFormulaTarget());
                Double compareResult = compare(metricsInfo, formulaTarget, formulaSymbol, condition.getFormulaNumber());
                // 符合校验则添加到业务结果里面
                if (compareResult != null) {
                    SlaEvent slaEvent = new SlaEvent();
                    slaEvent.setPressureId(pressureId);
                    slaEvent.setNumber(compareResult);
                    slaEvent.setRef(metricsInfo.getTransaction());
                    slaEvent.setSlaId(condition.getId());
                    slaEvent.setPressureExampleId(pressureExampleId);
                    slaEvent.setAttach(condition.getAttach());
                    slaEvent.setFormulaNumber(condition.getFormulaNumber());
                    slaEvent.setFormulaTarget(condition.getFormulaTarget());
                    slaEvent.setFormulaSymbol(condition.getFormulaSymbol());
                    result.add(slaEvent);
                }
            }
        }
        return result;
    }

    @Override
    public void event(Long pressureId, Long pressureExampleId, List<SlaEvent> slaEventEntityList) {
        if (CollUtil.isEmpty(slaEventEntityList)) {return;}
        iSlaEventService.saveBatch(slaEventEntityList);
        Pressure pressureEntity = iPressureService.getById(pressureId);
        PressureExample pressureExampleEntity = iPressureExampleService.getById(pressureExampleId);
        ResourceExample resourceExampleEntity = iResourceService.getById(pressureExampleEntity.getResourceExampleId());
        List<SlaDto.SlaInfo> slaInfoList = slaEventEntityList.stream().map(t -> new SlaDto.SlaInfo()
                .setPressureId(pressureId)
                .setRef(t.getRef())
                .setNumber(t.getNumber())
                .setAttach(t.getAttach())
                .setPressureExampleId(pressureExampleId)
                .setFormulaNumber(t.getFormulaNumber())
                .setFormulaSymbol(t.getFormulaSymbol())
                .setFormulaTarget(t.getFormulaTarget())
                .setResourceExampleId(resourceExampleEntity.getId())
                .setResourceId(resourceExampleEntity.getResourceId())
        ).collect(Collectors.toList());
        SlaDto sla = new SlaDto();
        sla.setTime(new Date());
        sla.setCallbackTime(new Date());
        sla.setData(slaInfoList);
        String slaString = jsonService.writeValueAsString(sla);
        // 创建回调
        callbackService.create(pressureEntity.getCallbackUrl(), CallbackType.SLA, slaString);
        log.info("SLA触发：{}", slaString);
    }

    Double compare(MetricsInfo info, FormulaTarget target, FormulaSymbol symbol, double value) {
        Double targetValue = null;
        switch (target) {
            case RT:
                targetValue = info.getRt();
                break;
            case SA:
                targetValue = ((info.getSaCount() * 1.0) / info.getCount()) * 100;
                break;
            case TPS:
                targetValue = (info.getCount() * 1.0) / 5;
                break;
            case SUCCESS_RATE:
                targetValue = 100 - ((info.getFailCount() * 100.0) / info.getCount());
                break;
            default:
                return targetValue;
        }
        // 进行数值比较
        int compareResult = targetValue.compareTo(value);
        switch (symbol) {
            case EQUAL:
                targetValue = compareResult == 0 ? targetValue : null;
                break;
            case GREATER_THAN:
                targetValue = compareResult > 0 ? targetValue : null;
                break;
            case GREATER_THAN_OR_EQUAL_TO:
                targetValue = compareResult >= 0 ? targetValue : null;
                break;
            case LESS_THAN:
                targetValue = compareResult < 0 ? targetValue : null;
                break;
            case LESS_THAN_OR_EQUAL_TO:
                targetValue = compareResult <= 0 ? targetValue : null;
                break;
            default:
                return targetValue;
        }
        return targetValue;
    }
}
