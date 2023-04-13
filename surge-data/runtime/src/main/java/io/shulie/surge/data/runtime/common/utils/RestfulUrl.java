package io.shulie.surge.data.runtime.common.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2023/4/13 21:04
 */
public class RestfulUrl {
    /**
     * 完整 url
     */
    private String url;

    private int segmentLength;

    private String[] segments;
    private boolean[] wildcards;


    private String prefix;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 方法
     */
    private String method;


    public RestfulUrl(String appName, String url, String method) {
        this.appName = appName;
        this.method = method;
        url = process(url);
        String[] segs = StringUtils.split(url, '/');
        this.segments = new String[segs.length];
        this.wildcards = new boolean[segs.length];
        StringBuilder builder = new StringBuilder();
        boolean appendPrefix = true;
        for (int i = 0, len = segs.length; i < len; i++) {
            String seg = segs[i];
            if (StringUtils.indexOf(seg, '{') != -1
                    && StringUtils.indexOf(seg, '}') != -1) {
                segments[i] = "*";
                wildcards[i] = true;
                appendPrefix = false;
            } else {
                segments[i] = seg;
                wildcards[i] = false;
                if (appendPrefix) {
                    builder.append('/').append(seg);
                }
            }
        }
        this.segmentLength = this.segments.length;
        this.url = url;
        this.prefix = builder.toString();
    }

    public String getAppName() {
        return appName;
    }

    public int getSegmentLength() {
        return segmentLength;
    }

    private String process(String url) {
        StringBuilder builder = new StringBuilder();
        boolean slash = false;
        for (int i = 0, len = url.length(); i < len; i++) {
            char c = url.charAt(i);
            if (c == '/') {
                if (slash) {
                    continue;
                }
                slash = true;
            } else {
                slash = false;
            }
            builder.append(c);
        }
        return builder.toString();
    }

    public boolean matches(String[] segs, String method) {
        if (StringUtils.isNotBlank(method)
                && StringUtils.isNotBlank(this.method)
                && !StringUtils.equals(method, this.method)) {
            return false;
        }
        if (segments.length != segmentLength) {
            return false;
        }

        for (int i = 0; i < segmentLength; i++) {
            if (wildcards[i]) {
                continue;
            }
            if (!StringUtils.equals(segments[i], segs[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 返回当前完整的 url
     *
     * @return
     */
    public String getUrl() {
        return url;
    }

    /**
     * 返回当前 restful 的索引
     *
     * @return
     */
    public String getPrefix() {
        return prefix;
    }
}
