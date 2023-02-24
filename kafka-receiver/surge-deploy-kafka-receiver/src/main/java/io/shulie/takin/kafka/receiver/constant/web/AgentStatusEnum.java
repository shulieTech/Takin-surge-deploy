package io.shulie.takin.kafka.receiver.constant.web;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * agent状态枚举
 *
 * @author ocean_wll
 * @date 2021/8/19 10:15 上午
 */
@Getter
@AllArgsConstructor
public enum AgentStatusEnum {
    INSTALLED(0, "INSTALLED", "安装成功"),
    UNINSTALL(1, "UNINSTALL", "未安装"),
    INSTALL_FAILED(4, "INSTALL_FAILED", "安装失败"),
    ;

    private Integer val;
    private String code;
    private String desc;
}
