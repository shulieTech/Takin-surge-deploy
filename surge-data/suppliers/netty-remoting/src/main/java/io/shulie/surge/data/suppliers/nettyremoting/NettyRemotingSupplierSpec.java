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

package io.shulie.surge.data.suppliers.nettyremoting;


import io.shulie.surge.data.common.factory.GenericFactorySpec;

import java.util.Map;

public class NettyRemotingSupplierSpec implements GenericFactorySpec<NettyRemotingSupplier> {
    private Map<String, String> netMap;
    private Map<String, String> hostNameMap;
    private boolean registerZk;

    public NettyRemotingSupplierSpec() {
    }

    public Map<String, String> getNetMap() {
        return netMap;
    }

    public void setNetMap(Map<String, String> netMap) {
        this.netMap = netMap;
    }

    public Map<String, String> getHostNameMap() {
        return hostNameMap;
    }

    public void setHostNameMap(Map<String, String> hostNameMap) {
        this.hostNameMap = hostNameMap;
    }

    public boolean isRegisterZk() {
        return registerZk;
    }

    public void setRegisterZk(boolean registerZk) {
        this.registerZk = registerZk;
    }

    @Override
    public String factoryName() {
        return "NettyRemotingSupplier";
    }


    @Override
    public Class<NettyRemotingSupplier> productClass() {
        return NettyRemotingSupplier.class;
    }

}
