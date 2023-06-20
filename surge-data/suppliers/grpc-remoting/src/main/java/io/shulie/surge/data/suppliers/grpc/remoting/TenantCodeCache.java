package io.shulie.surge.data.suppliers.grpc.remoting;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Sunsy
 * @date 2022/5/20
 * @apiNode
 * @email sunshiyu@shulie.io
 */
@Slf4j
public class TenantCodeCache {

    private static final TenantCodeCache INSTANCE = new TenantCodeCache();

    private Cache<String, String> tenantCodeCache =
            CacheBuilder.newBuilder().maximumSize(1000).build();

    private AtomicBoolean inited = new AtomicBoolean(false);

    private ScheduledExecutorService executorService;

    private TenantCodeCache() {

    }

    public void init(String host, int port, String path) {
        if (!inited.compareAndSet(false, true)) {
            log.error("TenantCodeCache is inited.");
            return;
        }
        loadTenantCodeMap(host, port, path);

        executorService = Executors.newScheduledThreadPool(1);

        executorService.scheduleAtFixedRate(new Thread(() -> {
            loadTenantCodeMap(host, port, path);
        }), 2, 2, TimeUnit.MINUTES);

        executorService = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("tenantConfig-collector");
                t.setDaemon(true);
                return t;
            }
        });
    }

    /**
     * 获取租户编号
     *
     * @return
     */
    public String get(String userAppKey) {
        if (!inited.get()) {
            log.error("TenantCodeCache is not inited");
            return userAppKey;
        }
        String value = tenantCodeCache.getIfPresent(userAppKey);
        if (StringUtils.isBlank(value)) {
            return userAppKey;
        }
        return value;
    }

    private void loadTenantCodeMap(String host, int port, String path) {
        JSONObject res = null;
        try {
            String response = HttpUtil.doGet(host, port, path, null, null);
            res = (JSONObject) JSON.parse(response);
            if (Objects.nonNull(res) && Objects.nonNull(res.get("data"))) {
                JSONArray tenantConfigList = (JSONArray) res.get("data");
                if (CollectionUtils.isNotEmpty(tenantConfigList)) {
                    for (Object tenantConfigObj : tenantConfigList) {
                        JSONObject tenantConfig = (JSONObject) tenantConfigObj;
                        tenantCodeCache.put(ObjectUtils.toString(tenantConfig.get("tenantAppKey")),
                                ObjectUtils.toString(tenantConfig.get("tenantCode")));
                    }
                }

            }
            tenantCodeCache.put("dc998ed4-bd15-4f53-ad8d-edfce40dd3fd", "defaultAdmin");
        } catch (Exception e) {
            log.error("query tenant list catch exception:{},{}", e, e.getStackTrace());
        }
    }

    public static TenantCodeCache getInstance() {
        return INSTANCE;
    }
}
