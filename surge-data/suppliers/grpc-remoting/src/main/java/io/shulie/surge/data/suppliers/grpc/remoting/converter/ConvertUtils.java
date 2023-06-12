package io.shulie.surge.data.suppliers.grpc.remoting.converter;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span.Event;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author vincent
 * @date 2022/07/03 22:07
 **/
public class ConvertUtils {
    /**
     * 将attribute 转换为map
     *
     * @param attributes
     * @return
     */
    public static Map<String, String> attributeToMap(List<KeyValue> attributes) {
        Map<String, String> map = Maps.newHashMap();
        for (KeyValue keyValue : attributes) {
            map.put(keyValue.getKey(), keyValue.getValue().getStringValue());
        }
        return map;
    }

    /**
     * 将attributes key 移除
     *
     * @param attributes
     * @return
     */
    public static void removeAttribute(Map<String, String> attributes , String ... keys) {
        Arrays.stream(keys).forEach(attributes::remove);
    }



    /**
     * 将attribute转换为string
     *
     * @param attributes
     * @return
     */
    public static String exportAttribute(Map<String, ? extends Object> attributes) {
        return JSON.toJSONString(attributes);
    }

    /**
     * 将event转换为map
     *
     * @param events
     * @return
     */
    public static Map<String, Map<String, String>> eventToMap(List<Event> events) {
        Map<String, Map<String, String>> map = Maps.newHashMap();
        for (Event event : events) {
            if (event.getAttributesList() == null) {
                continue;
            }
            map.put(event.getName(), attributeToMap(event.getAttributesList()));
        }
        return map;
    }

    /**
     * 把event转化成string
     *
     * @param eventMap
     * @return
     */
    public static String exportEvents(Map<String, Map<String, String>> eventMap) {
        return JSON.toJSONString(eventMap);
    }

    public static boolean isBlank(String str) {
        return StringUtils.isBlank(str) || StringUtils.equalsIgnoreCase(str, "null");
    }

    public static boolean isNullString(String str) {
        return StringUtils.equalsIgnoreCase(str, "null");
    }

    public static int[] getInvokeIdArray(String invokeId) {
        int[] invokeIdArray = parseVersion(invokeId);
        if (invokeIdArray.length >= 1) {
            invokeIdArray[0] = 0; // 修正部分 trace 不是以 0 作根的问题
        }
        return invokeIdArray;
    }

    /**
     * 将“.” 分隔的版本号、rpcId 切分成整数数组
     *
     * @param str
     * @return
     */
    private static int[] parseVersion(String str) {
        if (str != null) {
            String[] strs = StringUtils.split(str, '.');
            int[] ints = new int[strs.length];
            for (int i = 0; i < strs.length; ++i) {
                ints[i] = NumberUtils.toInt(strs[i], 0);
            }
            return ints;
        }
        return new int[] {0};
    }

}
