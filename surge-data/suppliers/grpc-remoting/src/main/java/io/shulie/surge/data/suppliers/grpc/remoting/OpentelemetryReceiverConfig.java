package io.shulie.surge.data.suppliers.grpc.remoting;

import lombok.Data;

/**
 * receiver 配置类
 *
 * @author Sunsy
 * @date 2022/4/28
 * @apiNode
 * @email sunshiyu@shulie.io
 */
@Data
public class OpentelemetryReceiverConfig {

    //强制成功-接收探针数据后，是否要走处理流程，当推送数据大量堆积时开启
    private boolean forceSuccess = false;
    private int directNettyServerPort = 21890;
    //netty启动端口
    private int nettyServerPort = 21900;
    //Grpc启动端口
    private int grpcServerPort = 21990;
    //Grpc最大并行数
    private int maxConcurrentCalls = 4;
    //最大接收报文长度，单位K，默认2m
    private int maxContentLength = 2048;
    //入口IP白名单
    private String whiteList = "";
    //入口IP黑名单
    private String blackList = "";
    //trace url path
    private String tracePath = "/trace";
    //metrics url path
    private String metricsPath = "/metrics";
    //node url path
    private String nodePath = "/node";

    private String forwardPath = "/forward";
    //select thread工作线程
    private int selectThreads = 4;
    //server工作线程
    private int serverWorkerThreads = 16;
    //服务端最大连接数
    private int serverMaxConnections = 2048;
    //跳过异常日志
    private boolean skipErrorLog = false;
    // 日志静默打印
    private boolean skipSilentLog = false;

}
