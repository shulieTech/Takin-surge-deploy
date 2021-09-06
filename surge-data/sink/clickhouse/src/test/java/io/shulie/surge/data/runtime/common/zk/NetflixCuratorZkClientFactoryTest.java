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

package io.shulie.surge.data.runtime.common.zk;

import io.shulie.surge.data.common.zk.ZkClientSpec;
import junit.framework.TestCase;

public class NetflixCuratorZkClientFactoryTest extends TestCase {

    /**
     * 判断是否有空指针异常
     * @throws Exception
     */
    public void testCreate() throws Exception {
        NetflixCuratorZkClientFactory netflixCuratorZkClientFactory = new NetflixCuratorZkClientFactory();
        ZkClientSpec spec = new ZkClientSpec();
        spec.setZkServers("127.0.0.1:2181");
        netflixCuratorZkClientFactory.create(spec);
    }
}