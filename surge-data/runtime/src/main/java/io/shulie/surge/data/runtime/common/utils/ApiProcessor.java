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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.shulie.surge.data.common.utils.HttpUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Auther: vernon
 * @Date: 2020/4/2 13:45
 * @Description: api聚合器
 */
@Singleton
public class ApiProcessor {
    private final static Logger logger = LoggerFactory.getLogger(ApiProcessor.class);

    private static String staticHost;
    private static String staticUrl;
    private static String staticPort;
    private static String staticTenantConfigUrl;
    private static String staticEntryUrl;
    public static String staticDefaultTenantAppKey;

    private String host;
    private String url;
    private String port;
    private String tenantConfigUrl;
    private String entryUrl;
    private String defaultTenantAppKey;

    private String amdbHost;
    private String amdbUrl;
    private String amdbPort;

    private static Gson gson = new Gson();

    protected static Map<String, Map<String, List<String>>> API_COLLECTION = new HashMap<>();

    private static Map<String, String> tenantConfigMap = new HashMap<>();

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

    private ScheduledExecutorService tenantService =
            new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("tenantConfig-collector");
                    t.setDaemon(true);
                    return t;
                }
            });

    //10分钟的本地缓存,1000个压测报告
    private static Cache<String, List<Map<String, Object>>> cache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES).build();

    public ApiProcessor() {
    }

    @Inject
    public ApiProcessor(@Named("tro.url.ip") String host, @Named("tro.api.path") String url, @Named("tro.tenant.config.path") String tenantConfigUrl, @Named("amdb.url.ip") String amdbHost, @Named("amdb.api.apiUrl.path") String amdbUrl, @Named("amdb.port") String amdbPort, @Named("tro.entries.path") String entryUrl, @Named("tro.port") String port, @Named("config.tenant.defaultTenantAppKey") String defaultTenantAppKey) {
        this.host = host;
        this.url = url;
        this.entryUrl = entryUrl;
        this.tenantConfigUrl = tenantConfigUrl;
        this.port = port;
        this.defaultTenantAppKey = defaultTenantAppKey;
        this.amdbHost = amdbHost;
        this.amdbPort = amdbPort;
        this.amdbUrl = amdbUrl;

        staticHost = host;
        staticUrl = url;
        staticTenantConfigUrl = tenantConfigUrl;
        staticEntryUrl = entryUrl;
        staticPort = port;
        staticDefaultTenantAppKey = defaultTenantAppKey;
    }


    public void init() {
        service.scheduleAtFixedRate(
                new Thread(() -> refreshV2())
                , 0
                , 2
                , TimeUnit.MINUTES);

        //每隔5分钟调用一次
        tenantService.scheduleAtFixedRate(
                new Thread(() -> queryTenantConfig())
                , 0
                , 5
                , TimeUnit.MINUTES);
    }

    private void refreshV2() {
        Map<String, Object> res = new HashMap<>();
        try {
            res = gson.fromJson(HttpUtil.doGet(amdbHost, Integer.valueOf(amdbPort), amdbUrl, null, null), Map.class);
        } catch (Throwable e) {
            logger.error("query all entry rules catch exception:{},{}", e, e.getStackTrace());
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


    @Deprecated
    private void refresh() {
        Map<String, Object> res = new HashMap<>();
        try {
            res = gson.fromJson(HttpUtil.doGet(host, Integer.valueOf(port), url, null, null), Map.class);
        } catch (Throwable e) {
            logger.error("query all entry rules catch exception:{},{}", e, e.getStackTrace());
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

    private void queryTenantConfig() {
        //重复应用列表
        Set<String> repeatAppList = new HashSet<>();
        //唯一应用列表
        Set<String> uniqueAppList = new HashSet<>();
        Map<String, Object> res = null;
        try {
            res = gson.fromJson(HttpUtil.doGet(host, Integer.valueOf(port), tenantConfigUrl, null, null), Map.class);
            if (Objects.nonNull(res) && Objects.nonNull(res.get("data"))) {
                Object data = res.get("data");
                List<Map<String, Object>> tenantConfigList = (List<Map<String, Object>>) data;
                if (CollectionUtils.isNotEmpty(tenantConfigList)) {
                    tenantConfigList.forEach((tenantConfig) -> {
                        if (!tenantConfig.containsKey("tenantAppKey") || !tenantConfig.containsKey("envAppMap")) {
                            return;
                        }
                        String tenantAppKey = (String) tenantConfig.get("tenantAppKey");
                        Map<String, Object> envAppMap = (Map<String, Object>) tenantConfig.get("envAppMap");
                        if (MapUtils.isNotEmpty(envAppMap)) {
                            envAppMap.forEach((k, v) -> {
                                List<String> appList = (List<String>) v;
                                if (CollectionUtils.isNotEmpty(appList)) {
                                    appList.forEach((appName) -> {
                                        if (uniqueAppList.contains(appName)) {
                                            repeatAppList.add(appName + "#" + tenantAppKey);
                                        } else {
                                            uniqueAppList.add(appName);
                                        }
                                        tenantConfigMap.put(appName, tenantAppKey + "#" + k);
                                    });
                                }
                            });
                        }
                    });
                }
                //如果不同环境应用名相同,默认test环境,不考虑不同租户同名应用的情况
                repeatAppList.forEach((app) -> {
                    tenantConfigMap.put(app.split("#")[0], app.split("#")[1] + "#test");
                });
            }
        } catch (Exception e) {
            logger.error("query tenant config catch exception:{},{}", e, e.getStackTrace());
        }
    }


    public static Map<String, String> getTenantConfigByAppName(String appName) {
        Map<String, String> config = Maps.newHashMap();
        if (tenantConfigMap.containsKey(appName)) {
            config.put("tenantAppKey", tenantConfigMap.get(appName).split("#")[0]);
            config.put("envCode", tenantConfigMap.get(appName).split("#")[1]);
        } else {
            config.put("tenantAppKey", StringUtils.defaultString(staticDefaultTenantAppKey, "default"));
            config.put("envCode", "test");
        }
        return config;
    }

    /**
     * 匹配报告ID下的业务活动
     *
     * @param taskId
     * @param url
     * @param type
     * @return
     */
    public static String matchBusinessActivity(String taskId, String url, String type) {
        List<String> matchUrls = new ArrayList<>(4);
        //String defaultAppName = "pressure-engine";
        String defaultResult = url;
        try {

            //先从cache里面拿,如果没拿到,调用查询接口
            List<Map<String, Object>> businessActivities = cache.getIfPresent(taskId);
            if (businessActivities == null) {
                businessActivities = getBusinessActivityByReportId(taskId);
                if (logger.isDebugEnabled()) {
                    logger.debug("taskId {} query businessActivity from tro:{}", taskId, businessActivities);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("taskId {} query businessActivity from cache:{}", taskId, businessActivities);
            }
            if (CollectionUtils.isEmpty(businessActivities)) {
                return defaultResult;
            }

            AntPathMatcher matcher = new AntPathMatcher();

            businessActivities.forEach(map -> {
                //是否虚拟业务活动 1为是,0为不是
                double isVirtual = (double) map.get("isVirtual");
                //正常
                if (isVirtual == 0) {
                    //先校验请求方式
                    String parsedMethod = (String) map.get("methodName");
                    if (!type.equalsIgnoreCase(parsedMethod)) {
                        return;
                    }
                }
                String pattern = (String) map.get("serviceName");
                if (matcher.match(pattern, url)) {
                    matchUrls.add(pattern);
                }
            });

            if (logger.isDebugEnabled()) {
                logger.debug("match urls:{}", matchUrls);
            }
            if (CollectionUtils.isEmpty(matchUrls)) {
                return defaultResult;
            }
            if (matchUrls.size() == 1) {
//                AtomicReference<String> appName = getMatchAppName(matchUrls, defaultAppName, businessActivities);
                return matchUrls.get(0);
            }
            // 选中匹配度最高的path (spring的原始逻辑：PatternsRequestCondition#compareTo)
            matchUrls.sort((pattern1, pattern2) -> matcher.getPatternComparator(url).compare(pattern1, pattern2));

//            AtomicReference<String> appName = getMatchAppName(matchUrls, defaultAppName, businessActivities);
            return matchUrls.get(0);
        } catch (Exception e) {
            logger.error("dealWith url match businessActivity catch exception :{},{}", e, e.getStackTrace());
        }
        return url;
    }

    private static AtomicReference<String> getMatchAppName(List<String> matchUrls, String
            defaultAppName, List<Map<String, Object>> businessActivities) {
        AtomicReference<String> appName = new AtomicReference<String>();
        businessActivities.forEach(map -> {
            if (matchUrls.get(0).equals(map.get("serviceName"))) {
                String applicationName = (String) map.get("applicationName");
                if (StringUtils.isBlank(applicationName)) {
                    appName.set(defaultAppName);
                } else {
                    appName.set(applicationName);
                }
            }
        });
        return appName;
    }


    /**
     * 根据报告ID查询业务活动
     */
    private static List<Map<String, Object>> getBusinessActivityByReportId(String taskId) {
        Map<String, Object> res = null;
        HashMap<String, String> param = Maps.newHashMap();
        param.put("reportId", taskId);

        try {
            res = gson.fromJson(HttpUtil.doGet(staticHost, Integer.valueOf(staticPort), staticEntryUrl, null, param), Map.class);
        } catch (Throwable e) {
            logger.error("query businessActivity catch exception :{},{}", e, e.getStackTrace());
        }
        if (Objects.nonNull(res) && Objects.nonNull(res.get("data"))) {
            Object data = res.get("data");
            List<Map<String, Object>> dataMapList = (List<Map<String, Object>>) data;
            if (CollectionUtils.isNotEmpty(dataMapList)) {
                cache.put(taskId, dataMapList);
                return dataMapList;
            } else {
                cache.put(taskId, Lists.newArrayList());
            }
        } else {
            cache.put(taskId, Lists.newArrayList());
        }
        return Lists.newArrayList();
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
        return matcher.match3(url, type, null);
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

    public Matcher() {
    }

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
    @Deprecated
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

    /**
     * 支持传入指定规则解析
     *
     * @param url
     * @param type
     * @param apiPatterns
     * @return
     */
    protected String match3(String url, String type, List<String> apiPatterns) {
        if (apiPatterns == null) {
            apiPatterns = apiMap.get(type);
        } else {
            List<String> tmpApiList = Lists.newArrayList();
            apiPatterns.stream().forEach(apiPattern -> {
                String api = apiPattern.split("#")[0];
                String method = apiPattern.split("#")[1];
                if (type.equalsIgnoreCase(method)) {
                    tmpApiList.add(api);
                }
            });
            apiPatterns = tmpApiList;
        }
        if (CollectionUtils.isEmpty(apiPatterns)) {
            apiPatterns = Lists.newArrayList();
            for (Map.Entry<String, List<String>> entry : apiMap.entrySet()) {
                apiPatterns.addAll(entry.getValue());
            }
        }
        if (apiPatterns.contains(url)) {
            return url;
        }
        /*
            借用 spring requestMapping 的匹配逻辑，先匹配出所有满足匹配条件的path，然后选中匹配度最高的path
            此处性能较merge2略差，但是能保证同一个url返回唯一结果，merge2返回的结果依赖于apiPath的顺序
            如：
            顺序1：{hello}/name      {hello}/{name}      hello/{name}
            url：hello/name
            merge2：{hello}/name
            merge：hello/{name}

            顺序2：{hello}/{name}      hello/{name}      {hello}/name
            url：hello/name
            merge2：hello/{name}
            merge：hello/{name}
         */
        List<String> matchUrls = new ArrayList<>(4);
        AntPathMatcher matcher = new AntPathMatcher();
        for (String pattern : apiPatterns) {
            if (matcher.match(pattern, url)) {
                matchUrls.add(pattern);
            }
        }
        if (CollectionUtils.isEmpty(matchUrls)) {
            return url;
        }
        if (matchUrls.size() == 1) {
            return matchUrls.get(0);
        }
        // 选中匹配度最高的path (spring的原始逻辑：PatternsRequestCondition#compareTo)
        matchUrls.sort((pattern1, pattern2) -> matcher.getPatternComparator(url).compare(pattern1, pattern2));
        return matchUrls.get(0);
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
}
