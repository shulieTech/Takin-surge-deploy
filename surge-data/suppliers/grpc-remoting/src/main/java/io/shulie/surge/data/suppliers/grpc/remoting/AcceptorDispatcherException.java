package io.shulie.surge.data.suppliers.grpc.remoting;

/**
 * 接收器路由实现类
 *
 * @author vincent
 * @date 2022/04/26 21:57
 **/
public class AcceptorDispatcherException extends Exception {

    private static final long serialVersionUID = -4809890379050650645L;

    public AcceptorDispatcherException(String message) {
        super(message);
    }

    public AcceptorDispatcherException(String message, Throwable cause) {
        super(message, cause);
    }
}
