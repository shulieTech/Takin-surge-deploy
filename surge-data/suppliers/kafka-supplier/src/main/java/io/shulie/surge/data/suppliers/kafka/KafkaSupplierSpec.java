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

package io.shulie.surge.data.suppliers.kafka;


import io.shulie.surge.data.common.factory.GenericFactorySpec;

public class KafkaSupplierSpec implements GenericFactorySpec<KafkaSupplier> {

    private String bootstrap;
    private String topic;

    public KafkaSupplierSpec() {
    }

    public KafkaSupplierSpec(String bootstrap, String topic) {
        this.bootstrap = bootstrap;
        this.topic = topic;
    }

    @Override
    public String factoryName() {
        return "KafkaSupplier";
    }


    @Override
    public Class<KafkaSupplier> productClass() {
        return KafkaSupplier.class;
    }

    public String getBootstrap() {
        return bootstrap;
    }

    public String getTopic() {
        return topic;
    }
}
