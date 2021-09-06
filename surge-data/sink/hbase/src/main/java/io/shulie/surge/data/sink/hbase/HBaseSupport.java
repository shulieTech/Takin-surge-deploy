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


import io.shulie.surge.data.common.lifecycle.Stoppable;
import org.hbase.async.KeyValue;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 封装 HBase 的服务的写实现
 *
 * @author pamirs
 */
public interface HBaseSupport extends Stoppable {

    /**
     * 写入单个列
     */
    void put(byte[] table, byte[] key, byte[] family, byte[] qualifier, byte[] value, Runnable errCallback)
            throws IOException;

    /**
     * 写入多个列
     */
    void put(byte[] table, byte[] key, byte[] family, byte[][] qualifiers, byte[][] values, Runnable errCallback)
            throws IOException;

    /**
     * 写入单个列，指定时间戳
     */
    void put(byte[] table, byte[] key, byte[] family, byte[] qualifier, byte[] value, long timestamp,
             Runnable errCallback)
            throws IOException;

    /**
     * 写入多个列，指定时间戳
     */
    void put(byte[] table, byte[] key, byte[] family, byte[][] qualifiers, byte[][] values,
             long timestamp, Runnable errCallback) throws IOException;

    /**
     * 递增某个列的值
     */
    void atomicIncrement(byte[] table, byte[] key, byte[] family, byte[] qualifier, long amount, Runnable errCallback)
            throws IOException;

    /**
     * 同步刷新
     */
    void syncFlush() throws IOException;


    /**
     * @param table
     * @param key
     * @throws IOException
     */
    ArrayList<KeyValue> get(byte[] table, byte[] key) throws IOException;


}
