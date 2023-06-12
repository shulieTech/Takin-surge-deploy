package io.shulie.surge.data.suppliers.grpc.remoting.node;

import io.shulie.surge.data.suppliers.grpc.remoting.ProtoBean;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @Description
 * @Author ocean_wll
 * @Date 2022/11/4 15:04
 */
@Data
@ToString
public class NodeProtoBean implements ProtoBean {

    private static final long serialVersionUID = -2100739352492675160L;

    private String message;
    private String protocol;


    @Override
    public LocalDateTime startTime() {
        return LocalDateTime.now();
    }
}
