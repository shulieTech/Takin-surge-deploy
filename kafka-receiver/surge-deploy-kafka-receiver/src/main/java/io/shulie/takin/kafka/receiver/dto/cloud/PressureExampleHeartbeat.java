package io.shulie.takin.kafka.receiver.dto.cloud;

import io.shulie.takin.kafka.receiver.constant.cloud.CallbackType;
import io.shulie.takin.kafka.receiver.entity.PressureExample;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 施压任务实例心跳
 *
 * @author <a href="mailto:472546172@qq.com">张天赐</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PressureExampleHeartbeat extends Base<PressureExampleDto> {
    private CallbackType type = CallbackType.PRESSURE_EXAMPLE_HEARTBEAT;
}
