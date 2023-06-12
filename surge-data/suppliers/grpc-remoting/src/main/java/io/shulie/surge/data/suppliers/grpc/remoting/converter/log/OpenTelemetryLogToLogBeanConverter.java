package io.shulie.surge.data.suppliers.grpc.remoting.converter.log;

import com.google.protobuf.InvalidProtocolBufferException;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.logs.v1.LogRecord;
import io.opentelemetry.proto.logs.v1.LogsData;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.proto.logs.v1.ScopeLogs;
import io.opentelemetry.proto.resource.v1.Resource;
import io.shulie.surge.data.suppliers.grpc.remoting.converter.OpenTelemetryBeanConverter;
import io.shulie.takin.data.stream.acceptor.proto.ProtoJsonUtil;

import java.util.List;

/**
 * @author vincent
 * @date 2022/07/26 16:36
 **/
public class OpenTelemetryLogToLogBeanConverter implements OpenTelemetryBeanConverter {

    /**
     * 转换为协议类
     *
     * @param json
     * @return
     */
    @Override
    public List convert(String json) throws InvalidProtocolBufferException {
        LogsData logsData = (LogsData)ProtoJsonUtil.toObject(LogsData.newBuilder(), json);
        List<ResourceLogs> resouceLogs = logsData.getResourceLogsList();
        for (ResourceLogs resourceLogs : resouceLogs) {
            Resource resource = resourceLogs.getResource();
            List<ScopeLogs> scopeLogsList = resourceLogs.getScopeLogsList();
            for (ScopeLogs scopeLogs : scopeLogsList) {
                List<LogRecord> logRecordsList = scopeLogs.getLogRecordsList();
                for (LogRecord logRecord : logRecordsList) {
                    String log = logRecord.getBody().getStringValue();

                }
            }
        }
        return null;
    }

    @Override
    public List convertTrace(ExportTraceServiceRequest request) {
        return null;
    }

    @Override
    public List convertMetric(ExportMetricsServiceRequest request) {
        return null;
    }

    @Override
    public List convertLog(ExportLogsServiceRequest request) {
        return null;
    }
}
