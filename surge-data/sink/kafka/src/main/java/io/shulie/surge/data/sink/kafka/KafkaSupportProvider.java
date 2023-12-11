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

package io.shulie.surge.data.sink.kafka;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.log4j.Logger;


/**
 * 返回单例的 {@link KafkaSupportProvider} 对象
 *
 * @author xingchen
 */
@Singleton
public class KafkaSupportProvider implements Provider<KafkaSupport> {
    private static final Logger logger = Logger.getLogger(KafkaSupportProvider.class);

    private DefaultKafkaSupport singleton;

    @Inject
    public KafkaSupportProvider(@Named("config.kafka.namesrv") String namesrv,
                                @Named("config.kafka.producerGroup") String producerGroup) {

        try {
            singleton = new DefaultKafkaSupport(namesrv, producerGroup);
        } catch (Exception e) {
            logger.warn("InfluxDBServiceProvider init fail", e);
            throw e;
        }
    }

    @Override
    public DefaultKafkaSupport get() {
        return this.singleton;
    }
}
