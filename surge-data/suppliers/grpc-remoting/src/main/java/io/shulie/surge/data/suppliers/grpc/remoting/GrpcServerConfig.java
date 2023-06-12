package io.shulie.surge.data.suppliers.grpc.remoting;

/**
 * @author angju
 * @date 2022/9/2 11:10
 */
public class GrpcServerConfig {
    //Grpc启动端口
    private int grpcServerPort = 21990;
    //Grpc最大并行数
    private int maxConcurrentCalls = 4;

    /**
     * 跳过异常日志
     */
    private boolean skipErrorLog = false;
    private boolean skipSilentLog = false;

    public int getGrpcServerPort() {
        return grpcServerPort;
    }

    public void setGrpcServerPort(int grpcServerPort) {
        this.grpcServerPort = grpcServerPort;
    }

    public int getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    public void setMaxConcurrentCalls(int maxConcurrentCalls) {
        this.maxConcurrentCalls = maxConcurrentCalls;
    }

    public boolean isSkipErrorLog() {
        return skipErrorLog;
    }

    public void setSkipErrorLog(boolean defaultSkipErrorLog) {
        this.skipErrorLog = defaultSkipErrorLog;
    }

    public boolean isSkipSilentLog() {
        return skipSilentLog;
    }

    public void setSkipSilentLog(boolean skipSilentLog) {
        this.skipSilentLog = skipSilentLog;
    }
}
