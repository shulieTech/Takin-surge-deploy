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

import io.shulie.surge.data.runtime.supplier.DefaultMultiProcessorSupplier;

/**
 * @Author: xingchen
 * @ClassName: JettySupplier
 * @Package: io.shulie.surge.data
 * @Date: 2020/11/1610:59
 * @Description:
 */
public class MixedSupplier extends DefaultMultiProcessorSupplier {
    private DefaultMultiProcessorSupplier[] suppliers;

    public MixedSupplier(DefaultMultiProcessorSupplier... suppliers) {
        this.suppliers = suppliers;
    }

    @Override
    public void start() throws Exception {
        for (DefaultMultiProcessorSupplier supplier : suppliers) {
            supplier.start();
        }
    }

    @Override
    public void stop() throws Exception {
        for (DefaultMultiProcessorSupplier supplier : suppliers) {
            supplier.start();
        }
    }
}
