package io.shulie.surge.data.suppliers.grpc.remoting.metrics;


import io.shulie.surge.data.suppliers.grpc.remoting.AcceptorDispatcherException;
import io.shulie.surge.data.suppliers.grpc.remoting.AcceptorResult;
import io.shulie.surge.data.suppliers.grpc.remoting.Service;

import java.io.Serializable;
import java.util.List;

/**
 * metrics 接口
 *
 * @author vincent
 * @date 2022/04/26 20:43
 **/
public interface MetricsAcceptorProto<R extends Serializable> extends Service {


    /**
     * cpu指标推送多条数据
     *
     * @param messages
     * @return
     */
    AcceptorResult<R> batchPushCpuMetrics(List<CpuMetricsProtoBean> messages)
            throws AcceptorDispatcherException;

    /**
     * 通用指标调用
     *
     * @param messages
     * @return
     * @throws AcceptorDispatcherException
     */
    AcceptorResult<R> batchPush(List<MetricsProtoBean> messages)
            throws AcceptorDispatcherException;


    /**
     * 内存指标推送多条数据
     *
     * @param messages
     * @return
     */
    AcceptorResult<R> batchPushMemoryMetrics(List<MemoryMetricsProtoBean> messages)
            throws AcceptorDispatcherException;


    /**
     * 磁盘指标推送多条数据
     *
     * @param messages
     * @return
     */
    AcceptorResult<R> batchPushDiskMetrics(List<DiskMetricsProtoBean> messages)
            throws AcceptorDispatcherException;


    /**
     * 网络指标推送多条数据
     *
     * @param messages
     * @return
     */
    AcceptorResult<R> batchPushNetworkMetrics(List<NetworkMetricsProtoBean> messages)
            throws AcceptorDispatcherException;

}
