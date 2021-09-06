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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.stumbleupon.async.Callback;
import com.stumbleupon.async.Deferred;
import io.shulie.surge.data.common.lifecycle.Lifecycle;
import io.shulie.surge.data.common.lifecycle.Stoppable;
import org.hbase.async.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于 asynchbase 的异步实现
 *
 * @author pamirs
 */
public class AsyncHBaseSupport implements HBaseSupport, Lifecycle, Stoppable {

    private static final Logger logger = LoggerFactory.getLogger(AsyncHBaseSupport.class);

    private final String zkServers;
    private final String zkRootNode;
    private HBaseClient client = null;
    private boolean traceExceptions = false;

    private static int MAX_PENDING_REQUEST = 50000;
    public static boolean ENABLE_FLOW_CONTROL = true;

    private static AtomicInteger pending = new AtomicInteger(0);
    @SuppressWarnings("rawtypes")
    private static Callback FLOW_CONTROL_CALLBACK = new Callback() {

        @Override
        public Integer call(Object arg) throws Exception {
            return pending.decrementAndGet();
        }

    };

    private static int fails = 0;

    @Inject
    public AsyncHBaseSupport(@Named("config.hbase.zk") String zkServers,
                             @Named("config.hbase.zk.rootNode") String zkRootNode) {
        this.zkServers = zkServers;
        this.zkRootNode = zkRootNode;
        this.client = new HBaseClient(zkServers, zkRootNode);
    }

    @Override
    public void start() throws Exception {
        if (client == null) {
            client = new HBaseClient(zkServers, zkRootNode);
        }
    }

    @Override
    public void stop() throws Exception {
        if (client != null) {
            HBaseClient x = client;
            client = null;
            x.shutdown().join();
        }
    }

    @Override
    public boolean isRunning() {
        return client != null;
    }

    public void setFlushInterval(short flushInterval) {
        client.setFlushInterval(flushInterval);
    }

    public short getFlushInterval() {
        return client.getFlushInterval();
    }

    public void setTraceExceptions(boolean traceExceptions) {
        this.traceExceptions = traceExceptions;
    }

    public HBaseClient getInternalHBaseClient() {
        return client;
    }

    @Override
    public void put(byte[] table, byte[] key, byte[] family, byte[] qualifier, byte[] value, Runnable errCallback)
            throws IOException {
        put(new PutRequest(table, key, family, qualifier, value), errCallback);
    }

    @Override
    public void put(byte[] table, byte[] key, byte[] family, byte[][] qualifiers, byte[][] values, Runnable errCallback)
            throws IOException {
        put(new PutRequest(table, key, family, qualifiers, values), errCallback);
    }

    @Override
    public void put(byte[] table, byte[] key, byte[] family, byte[] qualifier, byte[] value, long timestamp,
                    Runnable errCallback)
            throws IOException {
        put(new PutRequest(table, key, family, qualifier, value, timestamp), errCallback);
    }

    @Override
    public void put(byte[] table, byte[] key, byte[] family, byte[][] qualifiers, byte[][] values, long timestamp,
                    Runnable errCallback)
            throws IOException {
        put(new PutRequest(table, key, family, qualifiers, values, timestamp), errCallback);
    }


    @SuppressWarnings("unchecked")
    private void put(PutRequest request, final Runnable errCallback) throws IOException {
        Deferred<Object> deferred = client.put(request);

        if (ENABLE_FLOW_CONTROL) {
            deferred.addCallback(FLOW_CONTROL_CALLBACK);
            if (pending.incrementAndGet() > MAX_PENDING_REQUEST) {
                while (pending.get() > MAX_PENDING_REQUEST) {
                    try {
                        Thread.sleep(200);
                        logger.error("HBase FlowControl Activated...等待200ms,目前水位" + pending.get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }

        }

        if (traceExceptions) {
            deferred.addErrback(new Callback<Object, Exception>() {
                @Override
                public Object call(Exception e) throws Exception {
                    //logger.error("fail to put", e);
                    fails++;
                    if (fails % 10000 == 0) {
                        logger.error("fail to put", e);
                    }
                    if (errCallback != null) {
                        errCallback.run();
                    }
                    return null;
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void atomicIncrement(byte[] table, byte[] key, byte[] family, byte[] qualifier, long amount,
                                final Runnable errCallback)
            throws IOException {
        Deferred<Long> deferred = client.bufferAtomicIncrement(new AtomicIncrementRequest(
                table, key, family, qualifier, amount));

        if (ENABLE_FLOW_CONTROL) {
            deferred.addCallback(FLOW_CONTROL_CALLBACK);
            if (pending.incrementAndGet() > MAX_PENDING_REQUEST) {
                while (pending.get() > MAX_PENDING_REQUEST) {
                    synchronized (this) {
                        try {
                            this.wait(100);
                            logger.error("HBase FlowControl Activated...等待1秒,目前水位" + pending.get());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }

        }

        if (traceExceptions) {
            deferred.addErrback(new Callback<Long, Exception>() {
                @Override
                public Long call(Exception e) throws Exception {
                    //logger.error("fail to atomicIncrement", e);
                    fails++;
                    if (fails % 10000 == 0) {
                        logger.error("fail to atomicIncrement", e);
                    }
                    if (errCallback != null) {
                        errCallback.run();
                    }
                    return null;
                }
            });
        }
    }

    public void asyncFlush(final Runnable errCallback) {
        Deferred<Object> deferred = client.flush();
        if (traceExceptions) {
            deferred.addErrback(new Callback<Object, Exception>() {
                @Override
                public Long call(Exception e) throws Exception {
                    //logger.error("fail to flush", e);
                    if (errCallback != null) {
                        errCallback.run();
                    }
                    return null;
                }
            });
        }
    }

    @Override
    public void syncFlush() throws IOException {
        try {
            client.flush().join();
        } catch (Exception e) {
            throw new IOException("fail to flush and join", e);
        }
    }

    @Override
    public ArrayList<KeyValue> get(byte[] table, byte[] key) throws IOException {
        GetRequest getRequest = new GetRequest(table, key);
        Deferred<ArrayList<KeyValue>> deferred = client.get(getRequest);
        try {
            return deferred.join(500l);
        } catch (Exception e) {
            logger.error("fail to get trace", e);
            return null;
        }
    }
}
