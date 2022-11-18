package io.shulie.surge.data.runtime.module;

import com.alibaba.nacos.api.config.ConfigService;
import io.shulie.surge.data.runtime.common.nacos.NacosClientProvider;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

public class NacosClientModule extends BaseDataModule {

    @Override
    protected void configure() {
        Properties properties = bootstrap.getProperties();
        String property = properties.getProperty("nacos.server.addr");
        if (StringUtils.isEmpty(property)) {
            return;
        }
        bind(ConfigService.class).toProvider(NacosClientProvider.class);
    }
}
