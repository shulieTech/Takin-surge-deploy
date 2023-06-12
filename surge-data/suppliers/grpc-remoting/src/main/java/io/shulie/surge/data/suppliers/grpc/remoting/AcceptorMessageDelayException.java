package io.shulie.surge.data.suppliers.grpc.remoting;

/**
 * 接收器路由实现类
 *
 * @author vincent
 * @date 2022/04/26 21:57
 **/
public class AcceptorMessageDelayException extends Exception {

    private static final long serialVersionUID = -7392157453824285497L;

    public AcceptorMessageDelayException(String message) {
        super(message);
    }

    public AcceptorMessageDelayException(String message, Throwable cause) {
        super(message, cause);
    }
}
