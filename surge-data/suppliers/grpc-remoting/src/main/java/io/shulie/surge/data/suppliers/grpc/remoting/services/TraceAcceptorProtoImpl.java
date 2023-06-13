package io.shulie.surge.data.suppliers.grpc.remoting.services;

import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.runtime.digest.DigestContext;
import io.shulie.surge.data.runtime.processor.DataQueue;
import io.shulie.surge.data.suppliers.grpc.remoting.AcceptorDispatcherException;
import io.shulie.surge.data.suppliers.grpc.remoting.AcceptorResult;
import io.shulie.surge.data.suppliers.grpc.remoting.AcceptorResultCodeEnum;
import io.shulie.surge.data.suppliers.grpc.remoting.trace.TraceAcceptorProto;
import io.shulie.surge.data.suppliers.grpc.remoting.trace.TraceProtoBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author vincent
 * @date 2022/04/27 09:53
 **/
@Slf4j
public class TraceAcceptorProtoImpl implements TraceAcceptorProto {

    private DataQueue dataQueue;

    public TraceAcceptorProtoImpl(DataQueue dataQueue) {
        this.dataQueue = dataQueue;
    }

    @Override
    public AcceptorResult<String> batchPush(List<TraceProtoBean> messages) throws AcceptorDispatcherException {
        List<DigestContext<RpcBased>> list = messages.stream().map(message -> {
            RpcBased rpcBased = new RpcBased();
            rpcBased.setHostIp(message.getAgentId());
            rpcBased.setReceiveHttpTime(System.currentTimeMillis());
            rpcBased.setAppName(message.getAppName());
            rpcBased.setTraceId(message.getTraceId());
            rpcBased.setTenantCode(message.getTenantCode());
            rpcBased.setEnvCode(message.getEnvCode());
            rpcBased.setInvokeId(message.getInvokeId());
            rpcBased.setRpcType(message.getMiddlewareType());
            rpcBased.setStartTime(message.getStartTime().toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
            rpcBased.setCost(message.getCost());
            rpcBased.setMiddlewareName(message.getMiddlewareName());
            rpcBased.setServiceName(message.getService());
            rpcBased.setMethodName(message.getMethod());
            rpcBased.setRemoteIp(message.getHostIp());
            rpcBased.setPort(String.valueOf(message.getPort()));
            rpcBased.setResultCode(message.getResultCode());
            rpcBased.setRequestSize(message.getRequestSize());
            rpcBased.setResponseSize(message.getResponseSize());
            rpcBased.setRequest(message.getRequest());
            rpcBased.setResponse(message.getResponse());
            rpcBased.setClusterTest(message.getClusterTest() == 0 ? false : true);
            rpcBased.setInvokeType(String.valueOf(message.getMiddlewareType()));


            String middleWareName = message.getMiddlewareName();
            if (middleWareName.contains("feign") || middleWareName.contains("dubbo")) {
                rpcBased.setTrackMethod(rpcBased.getServiceName() + "#" + rpcBased.getMethodName());
            }

            return rpcBased;
        }).map(rpcBased -> {
            DigestContext<RpcBased> context = new DigestContext<>();
            context.setContent(rpcBased);
            context.setProcessTime(System.currentTimeMillis());
            context.setEventTime(rpcBased.getLogTime());
            return context;
        }).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return AcceptorResult.SUCCESS;
        }

        if (!dataQueue.canPublish(list.size())) {
            return new AcceptorResult<>(AcceptorResultCodeEnum.FLOW_CONTROL, "flow_control");
        }
        try {
            boolean ret = dataQueue.publish(list);
            if (!ret) {
                return new AcceptorResult<>(AcceptorResultCodeEnum.FLOW_CONTROL, "flow_control");
            }
        } catch (Throwable e) {
            log.error("trace acceptor process failed.", e);
            return new AcceptorResult<>(AcceptorResultCodeEnum.SAVE_FAILED, e.getMessage());
        }
        return AcceptorResult.SUCCESS;
    }
}
