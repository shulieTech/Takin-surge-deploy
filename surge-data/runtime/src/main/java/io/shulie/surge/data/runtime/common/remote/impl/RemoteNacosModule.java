package io.shulie.surge.data.runtime.common.remote.impl;

import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import io.shulie.surge.data.runtime.common.guice.FieldInjectionListener;
import io.shulie.surge.data.runtime.common.remote.Remote;
import io.shulie.surge.data.runtime.module.BaseDataModule;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Properties;

public class RemoteNacosModule extends BaseDataModule implements Serializable {
    @Override
    protected void configure() {

        Properties properties = bootstrap.getProperties();
        String property = properties.getProperty("nacos.server.addr");
        // 配置了nacos地址才使用
        if(StringUtils.isEmpty(property)){
            return;
        }

        bind(new TypeLiteral<Remote<Boolean>>() {
        }).annotatedWith(Named.class).to(new TypeLiteral<NacosDataImpl<Boolean>>() {
        });
        bind(new TypeLiteral<Remote<String>>() {
        }).annotatedWith(Named.class).to(new TypeLiteral<NacosDataImpl<String>>() {
        });
        bind(new TypeLiteral<Remote<Integer>>() {
        }).annotatedWith(Named.class).to(new TypeLiteral<NacosDataImpl<Integer>>() {
        });
        bind(new TypeLiteral<Remote<Long>>() {
        }).annotatedWith(Named.class).to(new TypeLiteral<NacosDataImpl<Long>>() {
        });
        bind(new TypeLiteral<Remote<Double>>() {
        }).annotatedWith(Named.class).to(new TypeLiteral<NacosDataImpl<Double>>() {
        });

        bindListener(Matchers.any(), new FieldInjectionListener(Remote.class, Inject.class, NacosDataImpl.class));

    }
}
