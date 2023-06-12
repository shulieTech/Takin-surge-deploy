package io.shulie.surge.data.suppliers.grpc.remoting.services;

import io.shulie.surge.data.suppliers.grpc.remoting.AcceptorDispatcherException;
import io.shulie.surge.data.suppliers.grpc.remoting.AcceptorResult;
import io.shulie.surge.data.suppliers.grpc.remoting.metrics.*;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * metrics接收协议
 *
 * @author vincent
 * @date 2022/04/27 09:53
 **/
@Slf4j
@ToString
public class MetricsAcceptorProtoImpl implements MetricsAcceptorProto<String> {

    /**
     * cpu指标推送多条数据
     *
     * @param messages
     * @return
     */
    @Override
    public AcceptorResult<String> batchPushCpuMetrics(List<CpuMetricsProtoBean> messages)
            throws AcceptorDispatcherException {
        List<MetricsProtoBean> list = new ArrayList<>();
        for (CpuMetricsProtoBean cpuMetricsProtoBean : messages) {
            list.addAll(cpuMetricsProtoBean.export());
        }
        return batchPush(list);
    }


    /**
     * 批量推送
     *
     * @param messages
     * @return
     * @throws AcceptorDispatcherException
     */
    @Override
    public AcceptorResult<String> batchPush(List<MetricsProtoBean> messages) throws AcceptorDispatcherException {
        return AcceptorResult.SUCCESS;
    }

    /**
     * 内存指标推送多条数据
     *
     * @param messages
     * @return
     */
    @Override
    public AcceptorResult<String> batchPushMemoryMetrics(List<MemoryMetricsProtoBean> messages)
            throws AcceptorDispatcherException {
        List<MetricsProtoBean> list = new ArrayList<>();
        for (MemoryMetricsProtoBean memoryMetricsProtoBean : messages) {
            list.addAll(memoryMetricsProtoBean.export());
        }
        return batchPush(list);
    }

    /**
     * 磁盘指标推送多条数据
     *
     * @param messages
     * @return
     */
    @Override
    public AcceptorResult<String> batchPushDiskMetrics(List<DiskMetricsProtoBean> messages)
            throws AcceptorDispatcherException {
        List<MetricsProtoBean> list = new ArrayList<>();
        for (DiskMetricsProtoBean diskMetricsProtoBean : messages) {
            list.addAll(diskMetricsProtoBean.export());
        }
        return batchPush(list);
    }

    /**
     * 网络指标推送多条数据
     *
     * @param messages
     * @return
     */
    @Override
    public AcceptorResult<String> batchPushNetworkMetrics(List<NetworkMetricsProtoBean> messages)
            throws AcceptorDispatcherException {
        List<MetricsProtoBean> list = new ArrayList<>();
        for (NetworkMetricsProtoBean networkMetricsProtoBean : messages) {
            list.addAll(networkMetricsProtoBean.export());
        }
        return batchPush(list);
    }

}
