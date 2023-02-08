package io.shulie.takin.kafka.receiver.constant.cloud;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 回调类型
 *
 * @author <a href="mailto:472546172@qq.com">张天赐</a>
 */
@Getter
@AllArgsConstructor
public enum CallbackType {
    /**
     * 消除警告
     */
    // 资源实例相关
    RESOURCE_EXAMPLE_HEARTBEAT(100, "资源实例(Pod)心跳"),
    RESOURCE_EXAMPLE_START(101, "资源实例启动"),
    RESOURCE_EXAMPLE_STOP(102, "资源实例停止"),
    RESOURCE_EXAMPLE_ERROR(103, "资源实例异常"),
    // 施压任务实例相关
    PRESSURE_EXAMPLE_HEARTBEAT(200, "施压任务实例心跳"),
    PRESSURE_EXAMPLE_START(201, "施压任务实例启动"),
    PRESSURE_EXAMPLE_STOP(202, "施压任务实例停止"),
    PRESSURE_EXAMPLE_ERROR(203, "施压任务实例异常"),
    PRESSURE_EXAMPLE_SUCCESSFUL(204, "施压任务实例正常停止"),
    // 过程数据[sla/csv用量]
    SLA(301, "触发SLA"),
    CALIBRATION(302, "数据校准任务"),
    FILE_USAGE(303, "文件用量"),
    // 文件资源
    FILE_RESOURCE_PROGRESS(400, "文件资源进度"),
    SCRIPT_RESULT(500, "脚本校验结果"),
    // 格式化用
    ;
    @JsonValue
    private final int code;
    private final String description;

    private static final Map<Integer, CallbackType> EXAMPLE_MAP = new HashMap<>(8);

    static {
        Arrays.stream(values()).forEach(t -> EXAMPLE_MAP.put(t.getCode(), t));
    }

    @JsonCreator
    public static CallbackType of(Integer code) {
        return EXAMPLE_MAP.get(code);
    }
}
