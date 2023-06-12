package io.shulie.surge.data.suppliers.grpc.remoting;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author vincent
 * @date 2022/04/27 09:40
 **/
@Getter
@Setter
@ToString
public class AcceptorResult<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 3400994829236148704L;
    public final static AcceptorResult<String> SUCCESS = new AcceptorResult<>(AcceptorResultCodeEnum.SUCCESS, "ok");

    private AcceptorResultCodeEnum code;
    private String message;
    private T data;

    public AcceptorResult() {

    }

    public AcceptorResult(AcceptorResultCodeEnum code, String message) {
        this.code = code;
        this.message = message;
    }

    public AcceptorResult(AcceptorResultCodeEnum code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
