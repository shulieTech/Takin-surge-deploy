package io.shulie.surge.data.runtime.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2023/4/13 21:20
 */
public class RestfulUrlPacket {
    private Map<String, Map<String, List<RestfulUrl>>>[] indexes = new Map[100];

    /**
     * @param length
     */
    private void ensureCapacity(int length) {
        Map<String, List<RestfulUrl>>[] newArray = new Map[length + 8];
        System.arraycopy(indexes, 0, newArray, 0, indexes.length);
    }

    public void addUrl(String appName, Map<String, List<String>> map) {
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            for (String url : entry.getValue()) {
                addUrl(appName, url, entry.getKey());
            }
        }
    }

    public void addUrl(String appName, String url, String method) {
        RestfulUrl restfulUrl = new RestfulUrl(appName, url, method);
        // 如果当前 list 长度小于索引需要的长度
        if (indexes.length <= restfulUrl.getSegmentLength()) {
            ensureCapacity(restfulUrl.getSegmentLength());
        }

        Map<String, Map<String, List<RestfulUrl>>> index = indexes[restfulUrl.getSegmentLength()];
        if (index == null) {
            index = new HashMap<>();
            indexes[restfulUrl.getSegmentLength()] = index;
        }

        Map<String, List<RestfulUrl>> map = index.get(restfulUrl.getAppName());
        if (map == null) {
            map = new HashMap<>();
            index.put(restfulUrl.getAppName(), map);
        }


        List<RestfulUrl> list = map.get(restfulUrl.getPrefix());
        if (list == null) {
            list = new ArrayList<>();
            map.put(restfulUrl.getPrefix(), list);
        }
        list.add(restfulUrl);
    }

    public String getRestfulUrl(String appName, String orgUrl, String method) {
        if (StringUtils.isBlank(orgUrl)) {
            return orgUrl;
        }
        String url = orgUrl;
        if (StringUtils.startsWith(url, "https://")
                || StringUtils.startsWith(url, "http://")) {
            URI uri = URI.create(url);
            url = uri.getPath();
        }
        int indexOfQuestion = url.indexOf('?');
        if (indexOfQuestion != -1) {
            url = url.substring(0, indexOfQuestion);
        }
        int indexOf = url.indexOf('#');
        if (indexOf != -1) {
            url = url.substring(0, indexOf);
        }

        String[] segments = StringUtils.split(url, '/');
        if (segments.length >= indexes.length) {
            return orgUrl;
        }
        Map<String, Map<String, List<RestfulUrl>>> appNameMapping = indexes[segments.length];
        if (appNameMapping == null) {
            return orgUrl;
        }

        Map<String, List<RestfulUrl>> map = appNameMapping.get(appName);
        if (appNameMapping == null) {
            return orgUrl;
        }

        for (Map.Entry<String, List<RestfulUrl>> entry : map.entrySet()) {
            if (!StringUtils.startsWith(url, entry.getKey())) {
                continue;
            }
            for (RestfulUrl restfulUrl : entry.getValue()) {
                boolean matches = restfulUrl.matches(segments, method);
                if (matches) {
                    return restfulUrl.getUrl();
                }
            }
        }
        return orgUrl;
    }
}
