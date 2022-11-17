package io.shulie.surge.data.runtime.common.remote.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.exception.NacosException;
import com.google.inject.Inject;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class NacosDataImpl<T> extends BaseRemoteData<T> {

    private static final Logger logger = Logger.getLogger(NacosDataImpl.class);

    private static final String NACOS_DATA_ID = "pradarConfig";
    private static final String NACOS_GROUP = "PRADAR_CONFIG";

    @Inject
    private ConfigService configService;

    private AtomicBoolean initConfigs = new AtomicBoolean(false);

    private volatile static Map<String, Object> pradarConfigs;

    private static List<ConfigRefreshListener> refreshListeners = new ArrayList<>();

    @Override
    public T get() {
        return data;
    }

    @Override
    public void set(T value) {
        // 当前方法一般不会调用
        pradarConfigs.put(dataId, value);
    }

    @Override
    protected void init(Field field, Type fieldType, Object instance) throws Exception {
        Object data;
        if (initConfigs.compareAndSet(false, true)) {
            initPradarConfigs();
        }
        refreshListeners.add(new ConfigRefreshListener(dataId, this));
        if (pradarConfigs.containsKey(dataId)) {
            data = pradarConfigs.get(dataId);
        } else {
            data = defalutData;
            logger.info("nacos configs don`t exists key:" + dataId + ", add config and push to nacos");
            pradarConfigs.put(dataId, defalutData);
            // 写入配置,推送配置
            configService.publishConfig(NACOS_DATA_ID, NACOS_GROUP, JSON.toJSONString(pradarConfigs));
        }
        updateData(data);
        notifyUpdate();
    }

    /**
     * 初始化nacos配置
     */
    private void initPradarConfigs() throws NacosException {
        String config = configService.getConfig(NACOS_DATA_ID, NACOS_GROUP, 3000);
        pradarConfigs = new ConcurrentHashMap<>(JSON.parseObject(config, Map.class));

        configService.addListener(NACOS_DATA_ID, NACOS_GROUP, new AbstractListener() {
            @Override
            public void receiveConfigInfo(String configInfo) {
                //  获取最新配置
                pradarConfigs = new ConcurrentHashMap<>(JSON.parseObject(configInfo, Map.class));
                // 刷新每个key
                for (ConfigRefreshListener listener : refreshListeners) {
                    listener.refresh();
                }
            }
        });
    }

    private static class ConfigRefreshListener {
        private String key;
        private NacosDataImpl nacosData;

        public ConfigRefreshListener(String key, NacosDataImpl nacosData) {
            this.key = key;
            this.nacosData = nacosData;
        }

        public void refresh() {
            Object newValue = pradarConfigs.get(key);
            if (newValue == null) {
                nacosData.updateData(null);
                nacosData.notifyUpdate();
                return;
            }
            Object oldValue = nacosData.get();
            if (!newValue.toString().equals(oldValue.toString())) {
                nacosData.updateData(newValue);
                nacosData.notifyUpdate();
            }
        }
    }

    public Object getValue(String key){
        return pradarConfigs.get(key);
    }

}
