package io.shulie.surge.data.deploy.pradar.common;

/**
 * @author Sunsy
 * @date 2022/2/25
 * @apiNode
 * @email sunshiyu@shulie.io
 */
public enum ResponseCodeEnum {

    CODE_0000("0000", "处理成功"),
    CODE_9999("9999", "处理失败"),
    CODE_9998("9998", "系统繁忙"),
    CODE_9997("9997", "请求参数为空"),
    CODE_9996("9996", "请求参数非法"),
    CODE_9995("9995", "事件时间延迟超过1小时"),
    CODE_9994("9994", "tag列表缺少eventTime"),
    CODE_9993("9993", "eventTime非法");

    private String code;
    private String msg;

    ResponseCodeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
