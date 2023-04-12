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

package io.shulie.surge.data.common.batch;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author vincent
 */
public class RotationBatch<T extends Serializable> {

    private Logger logger = LoggerFactory.getLogger(RotationBatch.class);
    private int maxRetries = 3;
    private LinkedBlockingQueue<T> batchObjs = new LinkedBlockingQueue<>(200000);
    private List<RotationPolicy> rotationPolicies = Lists.newLinkedList();
    private ExecutorService executor;
    private BatchSaver batchSaver;
    private AtomicBoolean started = new AtomicBoolean(false);
    private String shardKey = "";

    private volatile long lastFlushTime;

    private volatile boolean timeFlush = false;

    private volatile long flushInterval = -1L;

    private ReentrantLock lock;
    private Condition signal;

    private volatile boolean isRunning = false;

    //结合 init 一起使用
    public RotationBatch() {
    }

    public RotationBatch(RotationPolicy... rotationPolicy) {
        this(null, rotationPolicy);
    }

    public RotationBatch(String shardKey, RotationPolicy... rotationPolicy) {
        this.shardKey = shardKey;
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.lock = new ReentrantLock(false);
        this.signal = lock.newCondition();
        rotationPolicy(rotationPolicy);
    }

    public void init(String shardKey, RotationPolicy... rotationPolicy) {
        this.shardKey = shardKey;
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.lock = new ReentrantLock(false);
        this.signal = lock.newCondition();
        rotationPolicy(rotationPolicy);
    }

    public AtomicBoolean getStarted() {
        return started;
    }

    /**
     * 滚动策略
     *
     * @param rotationPolicy
     * @return
     */
    public RotationBatch rotationPolicy(RotationPolicy... rotationPolicy) {
        rotationPolicies.addAll(Arrays.asList(rotationPolicy));

        Iterator<RotationPolicy> iterator = rotationPolicies.iterator();
        while (iterator.hasNext()) {
            RotationPolicy policy = iterator.next();
            if (policy instanceof TimedRotationPolicy) {
                timeFlush = true;
                flushInterval = ((TimedRotationPolicy) policy).getInterval();
                iterator.remove();
            }
        }
        return this;
    }

    /**
     * 最大重试次数
     *
     * @param maxRetries
     * @return
     */
    public RotationBatch maxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    /**
     * 批保存
     *
     * @param batchSaver
     * @return
     */
    public RotationBatch batchSaver(BatchSaver batchSaver) {
        this.batchSaver = batchSaver;
        return this;
    }

    public RotationBatch addBatch(List<T> list, long offset) {
        if (started.compareAndSet(false, true)) {
            start();
        }
        try {
            for (T obj : list) {
                batchObjs.put(obj);
            }
        } catch (InterruptedException e) {
            logger.error("", e);
        }
        /**
         * 检查mark
         */
        if (checkMark(offset)) {
            inFlushBatch();
        }
        return this;
    }

    /**
     * 添加对象到批中
     *
     * @param object
     * @return
     */
    public RotationBatch addBatch(T object, long offset) {
        if (started.compareAndSet(false, true)) {
            start();
        }
        try {
            batchObjs.put(object);
        } catch (InterruptedException e) {
            logger.error("", e);
        }
        /**
         * 检查mark
         */
        if (checkMark(offset)) {
            inFlushBatch();
        }
        return this;
    }

    private synchronized void inFlushBatch() {
        boolean shouldFlush = timeFlush && (System.currentTimeMillis() - lastFlushTime >= flushInterval);
        if (!timeFlush || shouldFlush) {
            if (lock.tryLock()) {
                try {
                    signal.signal();
                } catch (Throwable e) {
                    logger.error("fail to signal notEmpty.", e);
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    /**
     * 添加对象到批中
     *
     * @param object
     * @return
     */
    public RotationBatch addBatch(T object) {
        addBatch(object, 1);
        return this;
    }

    /**
     * 添加对象到批中
     *
     * @param list
     * @return
     */
    public RotationBatch addBatch(List<T> list) {
        addBatch(list, list.size());
        return this;
    }

    /**
     *
     */
    private void saveBatch() {
        LinkedBlockingQueue<T> batch = null;
        synchronized (this) {
            batch = batchObjs;
            batchObjs = new LinkedBlockingQueue<T>(200000);
            reset();
        }
        if (CollectionUtils.isEmpty(batch)) {
            return;
        }
        int count = 0;
        while (count < maxRetries) {
            try {
                if (StringUtils.isNotBlank(shardKey)) {
                    batchSaver.shardSaveBatch(shardKey, batch);
                    break;
                } else {
                    if (batchSaver.saveBatch(batch)) {
                        break;
                    }
                }
                count++;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查是需要rotate
     *
     * @return
     */
    private boolean checkMark(long offset) {
        boolean rotate = false;
        for (RotationPolicy rotationPolicy : rotationPolicies) {
            if (rotationPolicy.mark(offset)) {
                rotate = true;
                continue;
            }
        }
        return rotate;
    }

    /**
     * 重置
     */
    private void reset() {
        for (RotationPolicy rotationPolicy : rotationPolicies) {
            rotationPolicy.reset();
        }
    }

    /**
     * 启动
     *
     * @return
     */
    public RotationBatch start() {
        isRunning = true;
        executor.execute(() -> {
            while (isRunning) {
                if (lock.tryLock()) {
                    try {
                        signal.await();
                    } catch (InterruptedException e) {
                        logger.error("", e);
                    } finally {
                        lock.unlock();
                    }
                }
                saveBatch();
                lastFlushTime = System.currentTimeMillis();

            }
        });

        return this;
    }


    /**
     * 批处理保存器
     *
     * @param <T>
     */
    public interface BatchSaver<T extends Serializable> {
        boolean saveBatch(LinkedBlockingQueue<T> ObjectBatch);

        boolean shardSaveBatch(String key, LinkedBlockingQueue<T> ObjectBatch);
    }


    /**
     * flush数据
     */
    public synchronized void flush() {
        batchSaver.saveBatch(batchObjs);
    }
}
