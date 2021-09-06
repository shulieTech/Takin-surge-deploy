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

package io.shulie.surge.data.deploy.pradar.common;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpAsyncPoster {
    private static final Logger logger = LoggerFactory.getLogger(HttpAsyncPoster.class);

    private ArrayBlockingQueue<Object> postDataQueue;

    private String httpAsyncPosterId;

    public HttpAsyncPoster(String httpAsyncPosterId, int maxCacheSize, String url, int timeout) {
        this.httpAsyncPosterId = httpAsyncPosterId;
        postDataQueue = new ArrayBlockingQueue<>(maxCacheSize);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            while (true) {
                Object data = postDataQueue.poll();
                if (data != null) {
                    try {
                        HttpUtil.sendPost(url, data, timeout);
                        logger.error(httpAsyncPosterId + ":数据发送完成");
                    } catch (Throwable e) {
                        logger.error(httpAsyncPosterId + ":数据发送失败-{}", e.getMessage());
                    }
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void sendPost(Object data) {
        if (!postDataQueue.offer(data)) {
            logger.error(httpAsyncPosterId + ":队列已满，可调整采样率");
        }
    }

    public static void main(String args[]) {
        HttpAsyncPoster httpAsyncPoster = new HttpAsyncPoster("测试", 1, "http://www.baidu.com",3000);
        for (int i = 0; i < 10; i++) {
            httpAsyncPoster.sendPost("999999-" + i);
        }
    }
}
