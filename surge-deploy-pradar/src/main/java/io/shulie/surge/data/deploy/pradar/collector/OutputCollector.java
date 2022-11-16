package io.shulie.surge.data.deploy.pradar.collector;

import java.util.List;

/**
 * @author vincent
 * @date 2022/11/14 16:45
 **/
public interface OutputCollector {

    /**
     * 获取子节点编号列表
     * @return
     */
    List<Integer> getReduceIds();


    /**
     * 根据分区下发数据
     * @param partition
     * @param streamId
     * @param values
     */
    void emit(int partition, String streamId,Object... values);
}


