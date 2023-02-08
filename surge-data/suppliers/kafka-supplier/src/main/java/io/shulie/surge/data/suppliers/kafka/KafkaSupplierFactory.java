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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.shulie.surge.data.common.factory.GenericFactory;
import io.shulie.surge.data.common.lifecycle.LifecycleObserver;
import io.shulie.surge.data.runtime.common.DataRuntime;
import io.shulie.surge.data.runtime.supplier.Supplier;

@Singleton
public class KafkaSupplierFactory implements GenericFactory<KafkaSupplier, KafkaSupplierSpec> {
    @Inject
    private DataRuntime runtime;

    @Override
    public KafkaSupplier create(KafkaSupplierSpec syncSpec) {
        KafkaSupplier supplier = new KafkaSupplier(syncSpec.getBootstrap(), syncSpec.getTopic(), syncSpec.getKafkaAuthFlag(),
                syncSpec.getSecurityProtocol(), syncSpec.getSaslMechanism(), syncSpec.getSaslJaasConfig());
        runtime.inject(supplier);
        LifecycleObserver<Supplier> logSupplierConfigSynchronize = new KafkaSupplierObserver();
        runtime.inject(logSupplierConfigSynchronize);
        supplier.addObserver(logSupplierConfigSynchronize);
        return supplier;
    }
}
