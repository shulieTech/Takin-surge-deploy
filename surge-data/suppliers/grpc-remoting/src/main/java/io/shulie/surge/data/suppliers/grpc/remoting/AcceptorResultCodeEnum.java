package io.shulie.surge.data.suppliers.grpc.remoting;

import lombok.Getter;

/**
 * 处理状态
 * @author vincent
 * @date 2022/04/27 09:55
 **/
@Getter
public enum AcceptorResultCodeEnum {

    SUCCESS("0000", "处理成功"),
    BAD_FORMAT("0001", "日志格式错误"),
    DELAY("0002", "日志延时"),
    SAVE_FAILED("0003", "保存失败"),
    FLOW_CONTROL("0004", "处理限流");

    private String code;
    private String msg;

    AcceptorResultCodeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
