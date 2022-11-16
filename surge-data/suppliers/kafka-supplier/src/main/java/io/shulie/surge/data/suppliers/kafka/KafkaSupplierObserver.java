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

import io.shulie.surge.data.common.lifecycle.LifecycleObserver;
import io.shulie.surge.data.runtime.supplier.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * netty remoting supplier 观察者
 *
 * @author vincent
 */
public final class KafkaSupplierObserver implements LifecycleObserver<Supplier> {

    private static final Logger logger = LoggerFactory.getLogger(KafkaSupplierObserver.class);

    public KafkaSupplierObserver() {

    }

    @Override
    public void beforeStart(Supplier target) {

    }

    @Override
    public void afterStart(Supplier target) {

    }

    @Override
    public void beforeStop(Supplier target) {

    }

    @Override
    public void afterStop(Supplier target) {

    }
}
