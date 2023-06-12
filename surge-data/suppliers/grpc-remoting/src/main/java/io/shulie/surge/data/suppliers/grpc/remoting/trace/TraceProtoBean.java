package io.shulie.surge.data.suppliers.grpc.remoting.trace;

import io.shulie.surge.data.suppliers.grpc.remoting.ProtoBean;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * trace协议
 *
 * @author vincent
 * @date 2022/04/26 20:43
 **/
@Data
@ToString
public class TraceProtoBean implements ProtoBean {
    private static final long serialVersionUID = -1901339352432675160L;
    public final static int SUCCESS = 1;
    public final static int FAIL = 0;

    private String traceId;
    private String tenantCode;
    private String envCode;
    private String chainCode;
    private String serviceCode;
    private String appName;
    private String service;
    private String method;
    private String middlewareName;
    private int middlewareType;
    private String serviceType;
    private Long requestSize;
    private Long responseSize;
    private String request;
    private String response;
    private String hostIp;
    private Integer port;
    private String agentId;
    private String resultCode;
    private int clusterTest;
    private String invokeId;
    private Long cost;
    private int async;
    private String attributes;
    private String comment;
    private Integer level;
    private int index;
    private LocalDateTime startTime;
    private String clusterHost;
    private String protocol;
    /**
     * 自耗时
     */
    private Long selfCost;

    /**
     * 事件（例如异常信息）
     */
    private String event;


    /**
     * 1 内部调用
     * 2 客户端
     * 3 服务端
     * 4 生产者
     * 5 消费者
     */
    private int kind;

    /**
     * 0 失败 1 成功
     */
    private Integer success;

    /**
     * 中间件模块名（探针写入使用）
     */
    private String middlewareModule;


    @Override
    public LocalDateTime startTime() {
        return startTime;
    }
}
