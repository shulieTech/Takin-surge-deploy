package io.shulie.surge.data.suppliers.grpc.remoting;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author vincent
 * @date 2022/04/26 20:47
 **/
public interface ProtoBean extends Serializable {

    /**
     * 开始时间
     * @return
     */
    LocalDateTime startTime();
}
