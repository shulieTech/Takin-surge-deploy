package io.shulie.surge.data.runtime.common.nacos;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class NacosClientProvider implements Provider<ConfigService> {

    private static final Logger logger = LoggerFactory.getLogger(NacosClientProvider.class);
    private ConfigService configService = null;

    @Inject
    public NacosClientProvider(@Named("nacos.server.addr") String nacosServerAddr,
                               @Named("nacos.namespace") String nacosNamespace,
                               @Named("nacos.username") String nacosUsername,
                               @Named("nacos.password") String nacosPassword) {

        try {
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.SERVER_ADDR, nacosServerAddr);
            properties.put(PropertyKeyConst.NAMESPACE, nacosNamespace);
            properties.put(PropertyKeyConst.USERNAME, nacosUsername);
            properties.put(PropertyKeyConst.PASSWORD, nacosPassword);

            configService = ConfigFactory.createConfigService(properties);

        } catch (Exception e) {
            logger.error("Failed to initialize nacos client. {}", e.toString());
        }
    }

    @Override
    public ConfigService get() {
        return configService;
    }
}
