/*
 * Copyright 2021 Shulie Technology, Co.Ltd
 * Email: shulie@shulie.io
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.shulie.surge.data.runtime.common.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.shulie.surge.data.common.utils.HttpUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: vernon
 * @Date: 2020/4/2 13:45
 * @Description: api聚合器
 */
@Singleton
public class ApiProcessor {
    @Inject
    @Named("tro.url.ip")
    private String URI;

    @Inject
    @Named("tro.api.path")
    private String PATH;

    @Inject
    @Named("tro.port")
    private String PORT;

    private Gson gson = new Gson();

    protected static Map<String, Map<String, List<String>>> API_COLLECTION = new HashMap<>();

    protected static Map<String, Matcher> MATHERS = new HashMap<>();

    private ScheduledExecutorService service =
            new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("api-collector");
                    t.setDaemon(true);
                    return t;
                }
            });


    public void init() {
        service.scheduleAtFixedRate(
                new Thread(() -> refresh())
                , 0
                , 2
                , TimeUnit.MINUTES)
        ;
    }

    private void refresh() {
        Map<String, Object> res = new HashMap<>();
        try {
            res = gson.fromJson(HttpUtil.doGet(URI, Integer.valueOf(PORT), PATH), Map.class);
        } catch (Throwable e) {
            System.err.println(e.getMessage());
        }
        if (Objects.nonNull(res) && Objects.nonNull(res.get("data"))) {
            Object data = res.get("data");
            Map<String, List<String>> map = (Map<String, List<String>>) data;
            for (String appName : map.keySet()) {
                List<String> apiList = map.get(appName);
                Map<String, List<String>> newApiMap = Maps.newHashMap();
                for (String api : apiList) {
                    String[] splits = api.split("#");
                    String url = splits[0];
                    String type = splits[1];
                    if (Objects.isNull(newApiMap.get(type))) {
                        List<String> list = new ArrayList<>();
                        list.add(url);
                        newApiMap.put(type, list);
                    } else {
                        List<String> newList = newApiMap.get(type);
                        newList.add(url);
                        newApiMap.put(type, newList);
                    }
                }
                API_COLLECTION.put(appName, newApiMap);
            }
            MATHERS.clear();
        }
    }

    /**
     * url 格式化
     *
     * @param url
     * @return
     */
    public static String urlFormat(String url) {
        if (StringUtils.isBlank(url)) {
            return url;
        } else {
            try {
                URL u = new URL(url);
                String protocol = u.getProtocol();
                String host = u.getHost();
/*
                if (IpAddressUtils.isIpv4AddressFast(host)) {
*/
                host = "";
                //}
                if ("null".equals(host)) {
                    host = "";
                }
                url = /*protocol + "://" + host +*/ u.getPath();
            } catch (Exception e) {
                //ignore
            }
        }
        return url;
    }

    /**
     * 截取host部分
     *
     * @param url
     * @return
     */
    public static String formatUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return url;
        } else {
            try {
                URL u = new URL(url);
                String protocol = u.getProtocol();
                String host = u.getHost();
                if ("null".equals(host)) {
                    host = "";
                }
                url = protocol + "://" + host;
            } catch (Exception e) {
                //ignore
            }
        }
        return url;
    }

    public static String merge(String appName, String url, String type) {
        url = urlFormat(url);
        if (StringUtils.isBlank(url)) {
            return "";
        }
        Matcher matcher = MATHERS.get(appName);
        if (Objects.isNull(matcher)) {
            Map<String, List<String>> apiMaps = API_COLLECTION.get(appName);
            if (Objects.isNull(apiMaps) || apiMaps.size() < 1) {
                return url;
            }
            matcher = new Matcher(apiMaps);
            MATHERS.putIfAbsent(appName, matcher);
        }
        return matcher.match2(url, type);
    }

    @Deprecated
    public static String oldMerge(String appName, String url, String type) {
        url = urlFormat(url);
        if (StringUtils.isBlank(url)) {
            return "";
        }
        Matcher matcher = MATHERS.get(appName);
        if (Objects.isNull(matcher)) {
            Map<String, List<String>> apiMaps = API_COLLECTION.get(appName);
            if (Objects.isNull(apiMaps) || apiMaps.size() < 1) {
                return url;
            }
            matcher = new Matcher(apiMaps);
            MATHERS.putIfAbsent(appName, matcher);
        }
        return matcher.match(url, type);
    }

    /**
     * 获取url Path部分
     */
    public static String parsePath(String href) {
        try {
            URL u = new URL(href);
            return u.getPath();
        } catch (Throwable e) {
        }
        return href;
    }
}

final class Matcher {
    final static Logger logger = LoggerFactory.getLogger(Matcher.class);
    private Map<String, List<String>> apiMap;

    public Matcher(Map<String, List<String>> apiMap) {
        this.apiMap = apiMap;
    }

    @Deprecated
    protected String match(String url, String type) {
        List<String> apis = apiMap.get(type);
        if (CollectionUtils.isEmpty(apis)) {
            apis = Lists.newArrayList();
            for (Map.Entry<String, List<String>> entry : apiMap.entrySet()) {
                apis.addAll(entry.getValue());
            }
        }
        url = url.trim();
        String res = url;
        try {
            StringBuilder pre = new StringBuilder();
            int index = url.indexOf("://");
            if (index > -1) {
                pre = pre.append(url.substring(0, index + 3));
                url = url.substring(index + 3, url.length());
                if (url.indexOf("/") > -1) {
                    pre = pre.append(url.substring(0, url.indexOf("/")));
                    url = url.substring(url.indexOf("/"), url.length());
                }
            }
            if (apis.contains(url)) {
                return res;
            }
            for (String api : apis) {
                // /app/add/{name}
                // /app/add/1
                // 2021-05-09 new Data_Format format2
                // /app/{name}/add
                // /app/1/add
                if (api.contains("/{") && api.contains("}")) {
                    String subApi = api.substring(0, api.indexOf("/{"));
                    //如果包含前缀
                    if (StringUtils.isNotBlank(subApi) && res.contains(subApi)) {
                        //获取后缀
                        String suffixApi = api.substring(api.indexOf("}") + 1);
                        String tempStr = null;
                        if (suffixApi.length() != 0) {
                            tempStr = res.substring(0 + subApi.length() + 1);
                        }
                        // add rules:如果属于格式2 同时匹配后缀api
                        if (!(tempStr == null ? true : (!tempStr.contains("/") ? false : tempStr.substring(tempStr.indexOf("/")).equals(suffixApi)))) {
                            continue;
                        }
                        return pre.append(api).toString();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("entrance rule match failed:{},{}", e, e.getStackTrace());
        }
        return res;
    }

    /**
     * 在原匹配逻辑上支持多参数匹配,全参数匹配
     * /{id}
     * /{id}/{name}
     * /get/{id}/test/{name}
     *
     * @param url
     * @param type
     * @return
     */
    protected String match2(String url, String type) {
        List<String> apis = apiMap.get(type);
        if (CollectionUtils.isEmpty(apis)) {
            apis = Lists.newArrayList();
            for (Map.Entry<String, List<String>> entry : apiMap.entrySet()) {
                apis.addAll(entry.getValue());
            }
        }
        url = url.trim();
        String res = url;
        try {
            StringBuilder pre = new StringBuilder();
            int index = url.indexOf("://");
            if (index > -1) {
                pre = pre.append(url.substring(0, index + 3));
                url = url.substring(index + 3, url.length());
                if (url.indexOf("/") > -1) {
                    pre = pre.append(url.substring(0, url.indexOf("/")));
                    url = url.substring(url.indexOf("/"), url.length());
                }
            }
            if (apis.contains(url)) {
                return res;
            }
            /**
             * 确保首位是/
             */
            if (url.charAt(0) != '/') {
                url = '/' + url;
            }

            /**
             * 确保长度大于1的末尾不是/
             */
            if (url.length() > 1 && url.charAt(url.length() - 1) == '/') {
                url = url.substring(0, url.length() - 1);
            }

            String[] sourceSplit = url.split("/");
            Boolean flag = true;
            String temp = "";

            loop:
            for (String api : apis) {
                /**
                 * 确保首位是/
                 */
                if (url.charAt(0) != '/') {
                    url = '/' + url;
                }

                /**
                 * 确保长度大于1的末尾不是/
                 */
                if (url.length() > 1 && url.charAt(url.length() - 1) == '/') {
                    url = url.substring(0, url.length() - 1);
                }

                if (api.split("/").length != url.split("/").length) {
                    continue;
                }
                String[] paramArr = api.split("\\{");
                //规则中含有多个参数或者只有一个参数的规则
                if (paramArr.length >= 3 || (paramArr.length == 2 && "/".equals(paramArr[0]) && paramArr[1].contains("}"))) {
                    String[] apiSplit = api.split("/");
                    //如果规则长度和源字符不相等,进入下一条规则匹配
                    if (apiSplit.length != sourceSplit.length) {
                        continue;
                    }
                    //如果长度相等
                    //忽略第一个空值,从第二位开始匹配
                    int paramCount = 0;
                    for (int i = 1; i < apiSplit.length; i++) {
                        String word = apiSplit[i];
                        //如果是变量,跳过
                        if ("{".equals(word.substring(0, 1)) && "}".equals(word.substring(word.length() - 1))) {
                            paramCount++;
                            continue;
                        }
                        //如果两者不相等,直接进入下一个规则匹配
                        if (!word.equals(sourceSplit[i])) {
                            continue loop;
                        }
                    }
                    //如果等值匹配上,则返回规则
                    if (flag) {
                        //如果是全参数匹配,需要继续向下检索,是否存在等值匹配
                        if (paramCount == apiSplit.length - 1) {
                            //保存临时结果集
                            temp = api;
                            continue;
                        }
                        return pre.append(api).toString();
                    }
                }
                // /app/add/{name}
                // /app/add/1

                // 2021-05-09 new Data_Format format2
                // /app/{name}/add
                // /app/1/add
                if (api.contains("/{") && api.contains("}")) {
                    String subApi = api.substring(0, api.indexOf("/{"));
                    //如果包含前缀
                    if (StringUtils.isNotBlank(subApi) && res.startsWith(subApi)) {
                        //获取后缀
                        String suffixApi = api.substring(api.indexOf("}") + 1);
                        String tempStr = null;
                        if (suffixApi.length() != 0) {
                            tempStr = res.substring(0 + subApi.length() + 1);
                        } else {
                            //如果后缀为空,需要继续比较位数
                            if (api.split("/").length != res.split("/").length)
                                continue;
                        }

                        // add rules:如果属于格式2 同时匹配后缀api
                        if (!(tempStr == null ? true : (!tempStr.contains("/") ? false : tempStr.substring(tempStr.indexOf("/")).equals(suffixApi)))) {
                            continue;
                        }
                        return pre.append(api).toString();
                    }
                }
            }
            if (StringUtils.isNotBlank(temp)) {
                return pre.append(temp).toString();
            }
        } catch (Exception e) {
            logger.error("entrance:{},rule:{},match failed:{},{}", url, apis, e, e.getStackTrace());
        }

        return res;
    }

    static enum HttpTypeEnum {

        POST,
        GET,
        PUT,
        DELETE,
        OPTIONS,
        HEAD,
        TRACE,
        CONNECTION

    }

    public static void main(String[] args) {
        Map<String, List<String>> newApiMap = Maps.newHashMap();
        newApiMap.put("GET", Arrays.asList("/hello/{name}", "/{id}/{name}", "/path/{id}/test", "/path/{id}/test/{num}", "/path/{id}", "/{id}/{name}/test", "/{id}"));
        ApiProcessor.API_COLLECTION.put("test", newApiMap);
        System.out.println(ApiProcessor.merge("test", "/123/hello/sdsds/dsds", "GET"));
        System.out.println(ApiProcessor.merge("test", "/hello/sdsds/dsds", "GET"));
        System.out.println(ApiProcessor.merge("test", "/hello/yyy", "GET"));
        System.out.println(ApiProcessor.merge("test", "/mmm/nnn", "GET"));
        System.out.println(ApiProcessor.merge("test", "/path/321/test", "GET"));
        System.out.println(ApiProcessor.merge("test", "/path/321/test/654", "GET"));
        System.out.println(ApiProcessor.merge("test", "/path/123", "GET"));
        System.out.println(ApiProcessor.merge("test", "/123/haha/test", "GET"));
        System.out.println(ApiProcessor.merge("test", "/123", "GET"));
    }
}