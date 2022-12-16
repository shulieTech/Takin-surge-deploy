package io.shulie.takin.kafka.receiver.constant.web;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 探针状态枚举
 *
 * @author ocean_wll
 * @date 2021/8/19 10:19 上午
 */
@Getter
@AllArgsConstructor
public enum ProbeStatusEnum {
    INSTALLED(0, "INSTALLED", "安装成功"),
    UNINSTALL(1, "UNINSTALL", "未安装"),
    INSTALLING(2, "INSTALLING", "安装中"),
    UNINSTALLING(3, "UNINSTALLING", "卸载中"),
    INSTALL_FAILED(4, "UNINSTALL_FAILED", "安装失败"),
    UNINSTALL_FAILED(5, "INSTALL_FAILED", "卸载失败"),
    ;

    private Integer val;
    private String code;
    private String desc;
}
