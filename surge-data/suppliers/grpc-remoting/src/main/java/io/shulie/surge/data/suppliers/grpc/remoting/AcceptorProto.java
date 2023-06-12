package io.shulie.surge.data.suppliers.grpc.remoting;


import java.io.Serializable;
import java.util.List;

/**
 * @author vincent
 * @date 2022/04/26 20:46
 **/
public interface AcceptorProto<T extends ProtoBean, R extends Serializable> extends Service {

    /**
     * 推送多条数据
     *
     * @param messages
     * @return
     */
    AcceptorResult<R> batchPush(List<T> messages) throws AcceptorDispatcherException;
}
