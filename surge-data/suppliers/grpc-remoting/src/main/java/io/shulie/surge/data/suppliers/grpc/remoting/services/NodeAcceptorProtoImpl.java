package io.shulie.surge.data.suppliers.grpc.remoting.services;

import com.alibaba.fastjson.JSON;
import io.shulie.surge.data.runtime.digest.DigestContext;
import io.shulie.surge.data.runtime.processor.DataQueue;
import io.shulie.surge.data.suppliers.grpc.remoting.AcceptorDispatcherException;
import io.shulie.surge.data.suppliers.grpc.remoting.AcceptorResult;
import io.shulie.surge.data.suppliers.grpc.remoting.AcceptorResultCodeEnum;
import io.shulie.surge.data.suppliers.grpc.remoting.node.NodeAcceptorProto;
import io.shulie.surge.data.suppliers.grpc.remoting.node.NodeInfo;
import io.shulie.surge.data.suppliers.grpc.remoting.node.NodeProtoBean;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author ocean_wll
 * @Date 2022/11/4 15:32
 */
@Slf4j
public class NodeAcceptorProtoImpl implements NodeAcceptorProto {
    private DataQueue dataQueue;

    public NodeAcceptorProtoImpl(DataQueue dataQueue) {
        this.dataQueue = dataQueue;
    }

    @Override
    public AcceptorResult<String> batchPush(List<NodeProtoBean> messages) throws AcceptorDispatcherException {
        List<DigestContext<NodeInfo>> list = messages.stream().map(message -> {
                    try {
                        NodeInfo nodeInfo = JSON.parseObject(message.getMessage(), NodeInfo.class);
                        return nodeInfo;
                    } catch (Exception e) {
                        log.info("node acceptor parser json error.{}", message.getMessage(), e);
                    }
                    return null;
                }).filter(Objects::nonNull)
                .map(nodeInfo -> {
                    DigestContext<NodeInfo> context = new DigestContext<>();
                    context.setContent(nodeInfo);
                    context.setProcessTime(System.currentTimeMillis());
                    return context;
                })
                .collect(Collectors.toList());
        if (!dataQueue.canPublish(list.size())) {
            return new AcceptorResult<>(AcceptorResultCodeEnum.FLOW_CONTROL, "flow_control");
        }
        try {
            boolean ret = dataQueue.publish(list);
            if (!ret) {
                return new AcceptorResult<>(AcceptorResultCodeEnum.FLOW_CONTROL, "flow_control");
            }
            return AcceptorResult.SUCCESS;
        } catch (Throwable e) {
            log.error("node acceptor process error.{}", messages, e);
            return new AcceptorResult<>(AcceptorResultCodeEnum.SAVE_FAILED, e.getMessage());
        }
    }
}
