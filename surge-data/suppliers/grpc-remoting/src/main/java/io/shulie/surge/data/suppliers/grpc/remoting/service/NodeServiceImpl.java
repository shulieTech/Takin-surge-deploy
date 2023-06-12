package io.shulie.surge.data.suppliers.grpc.remoting.service;

import com.google.common.collect.Lists;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.node.v1.NodeServiceGrpc;
import io.opentelemetry.proto.collector.node.v1.NodeServiceProto;
import io.opentelemetry.proto.node.v1.NodeProto;
import io.shulie.surge.data.suppliers.grpc.remoting.AcceptorDispatcherException;
import io.shulie.surge.data.suppliers.grpc.remoting.NodeDataChecker;
import io.shulie.surge.data.suppliers.grpc.remoting.node.NodeAcceptorProto;
import io.shulie.surge.data.suppliers.grpc.remoting.node.NodeProtoBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * @Description
 * @Author ocean_wll
 * @Date 2022/11/4 15:01
 */
@Slf4j
public class NodeServiceImpl extends NodeServiceGrpc.NodeServiceImplBase {

    private NodeAcceptorProto nodeAcceptorProto;

    public NodeServiceImpl(NodeAcceptorProto nodeAcceptorProto) {
        this.nodeAcceptorProto = nodeAcceptorProto;
    }

    //@Override
    @Override
    public void export(NodeServiceProto.ExportNodeServiceRequest request, StreamObserver<NodeServiceProto.ExportNodeServiceResponse> responseObserver) {

        List<NodeProtoBean> nodeProtoBeans = Lists.newArrayList();
        List<NodeProto.NodeInfo> nodeInfos = request.getResourceNodeInfoList();
        for (NodeProto.NodeInfo nodeInfo : nodeInfos) {
            NodeProtoBean nodeProtoBean = new NodeProtoBean();
            nodeProtoBean.setMessage(nodeInfo.getMessage());
            if (NodeDataChecker.isAccurateData(nodeInfo.getMessage())) {
                nodeProtoBean.setProtocol("accurate");
            } else {
                nodeProtoBean.setProtocol("inaccurate");
            }
            nodeProtoBeans.add(nodeProtoBean);
        }

        if (CollectionUtils.isNotEmpty(nodeProtoBeans)) {
            try {
                //log.info("nodeProtoBeans数据采集：{}", JSON.toJSONString(nodeProtoBeans));
                nodeAcceptorProto.batchPush(nodeProtoBeans);
            } catch (AcceptorDispatcherException e) {
                log.error("grpc push node to mq:{}", nodeProtoBeans, e);
            }
        }
        responseObserver.onNext(NodeServiceProto.ExportNodeServiceResponse.newBuilder().build());
        responseObserver.onCompleted();

    }
}
