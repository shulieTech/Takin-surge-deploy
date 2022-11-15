package io.shulie.surge.data.deploy.pradar.common;

import net.openhft.affinity.AffinityLock;

/**
 * @author xingchen
 * @description: TODO
 * @date 2022/11/1 12:58 PM
 */
public class AffinityUtil {
    /**
     * 按任务Id去绑定cpu
     *
     * @param taskId
     * @return
     */
    public static AffinityLock acquireLock(Integer taskId) {
        int processs = Runtime.getRuntime().availableProcessors();
        AffinityLock affinityLock = AffinityLock.acquireLock(taskId % processs);
        return affinityLock;
    }
}
