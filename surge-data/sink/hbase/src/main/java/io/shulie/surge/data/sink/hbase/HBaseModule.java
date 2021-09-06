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

package io.shulie.surge.data.sink.hbase;

import io.shulie.surge.data.runtime.module.BaseDataModule;

/**
 * hbase 模块
 *
 * @author vincent
 */
public class HBaseModule extends BaseDataModule {

    @Override
    protected void configure() {
        bindGeneric(HBaseSupport.class, AsyncHBaseSupportFactory.class, HBaseSupportSpec.class);
        bind(HBaseSupport.class).toProvider(HBaseSupportProvider.class);
    }
}