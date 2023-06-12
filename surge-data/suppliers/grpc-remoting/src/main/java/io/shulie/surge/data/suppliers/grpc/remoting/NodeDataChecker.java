package io.shulie.surge.data.suppliers.grpc.remoting;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 * @Description
 * @Author ocean_wll
 * @Date 2022/11/4 16:11
 */
public class NodeDataChecker {
    /**
     * 检查数据质量
     *
     * @param message 原始数据
     * @return true 采集到的时间正确无误 ， false 采集到的数据有问题
     */
    public static boolean isAccurateData(String message) {
        if (StringUtils.isBlank(message)) {
            return false;
        }
        try {
            // 探针传过来的数据是个jsonString，如果解析有问题直接认为数据有问题
            JSONObject jsonObject = JSON.parseObject(message);
            return jsonObject != null
                    && jsonObject.get("collectionTime") != null
                    && jsonObject.get("cpu") != null
                    && jsonObject.get("disk") != null
                    && jsonObject.get("gcInfo") != null
                    && jsonObject.get("jvmInfo") != null
                    && jsonObject.get("resource") != null
                    && jsonObject.get("thread") != null;
        } catch (Exception e) {
            return false;
        }
    }
}
