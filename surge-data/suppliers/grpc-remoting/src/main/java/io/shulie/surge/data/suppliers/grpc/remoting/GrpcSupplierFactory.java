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

package io.shulie.surge.data.suppliers.grpc.remoting;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.shulie.surge.data.common.factory.GenericFactory;
import io.shulie.surge.data.common.lifecycle.LifecycleObserver;
import io.shulie.surge.data.runtime.common.DataRuntime;
import io.shulie.surge.data.runtime.supplier.Supplier;

@Singleton
public class GrpcSupplierFactory implements GenericFactory<GrpcSupplier, GrpcSupplierSpec> {
    @Inject
    private DataRuntime runtime;

    @Override
    public GrpcSupplier create(GrpcSupplierSpec syncSpec) {
        GrpcSupplier supplier = new GrpcSupplier();
        runtime.inject(supplier);
        LifecycleObserver<Supplier> grpcSupplierObserver = new GrpcSupplierObserver();
        runtime.inject(grpcSupplierObserver);
        supplier.addObserver(grpcSupplierObserver);
        return supplier;
    }
}
