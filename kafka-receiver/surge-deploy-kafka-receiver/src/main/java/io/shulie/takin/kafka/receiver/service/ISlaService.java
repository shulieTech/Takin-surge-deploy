package io.shulie.takin.kafka.receiver.service;

import io.shulie.takin.kafka.receiver.dto.cloud.MetricsInfo;
import io.shulie.takin.kafka.receiver.entity.Sla;
import com.baomidou.mybatisplus.extension.service.IService;
import io.shulie.takin.kafka.receiver.entity.SlaEvent;

import java.util.List;

/**
 * <p>
 * Service Level Agreement(服务等级协议) 服务类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
public interface ISlaService extends IService<Sla> {

    List<SlaEvent> check(Long pressureId, Long pressureExampleId, List<MetricsInfo> metricsInfos);

    void event(Long pressureId, Long pressureExampleId, List<SlaEvent> check);
}
