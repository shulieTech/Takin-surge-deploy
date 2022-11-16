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

import io.shulie.surge.data.runtime.module.BaseDataModule;

public class KafkaModule extends BaseDataModule {

    private static final long serialVersionUID = 6167505168329934505L;

    @Override
    protected void configure() {
        bindGeneric(KafkaSupplier.class, KafkaSupplierFactory.class, KafkaSupplierSpec.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return false;
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

}