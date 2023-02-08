package io.shulie.takin.kafka.receiver.constant.cloud;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 算式目标
 * <p>(RT、TPS、SA、成功率)</p>
 *
 * @author <a href="mailto:472546172@qq.com">张天赐</a>
 */
@Getter
@AllArgsConstructor
public enum FormulaTarget {
    /**
     * 消除警告
     */
    RT(0, "接口响应时间"),
    TPS(1, "每秒吞吐量"),
    SUCCESS_RATE(2, "成功率"),
    SA(3, "符合RT标准的比例"),
    // 格式化用
    ;
    @Getter
    @JsonValue
    private final Integer code;
    private final String description;

    @Override
    public String toString() {return code + ":" + description;}

    private static final Map<Integer, FormulaTarget> EXAMPLE_MAP = new HashMap<>(6);

    static {
        Arrays.stream(values()).forEach(t -> EXAMPLE_MAP.put(t.getCode(), t));
    }
    @JsonCreator
    public static FormulaTarget of(Integer code) {
        return EXAMPLE_MAP.get(code);
    }

}
