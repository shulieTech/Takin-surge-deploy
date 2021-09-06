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

package io.shulie.surge.data.runtime;


import com.google.inject.*;
import io.shulie.surge.data.runtime.common.DataBootstrap;
import io.shulie.surge.data.runtime.common.DataRuntime;
import io.shulie.surge.data.sink.clickhouse.*;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;

public class ClickHouseShardSupportTest {
    @Inject
    private ClickhouseHolder clickhouseHolder;

    @Test
    public void singleIp() {

        ClickHouseShardSupport clickHouseShardSupport = new ClickHouseShardSupport("jdbc:clickhouse://127.1.1.1:8123/default", "", "", 1, true);

        Class<ClickHouseShardSupport> clickHouseShardSupportClass = ClickHouseShardSupport.class;

        try {
            Field shardJdbcTemplateMapField = clickHouseShardSupportClass.getDeclaredField("shardJdbcTemplateMap");
            shardJdbcTemplateMapField.setAccessible(true);
            Map jdbcMap = (Map) shardJdbcTemplateMapField.get(clickHouseShardSupport);

            assert jdbcMap != null && jdbcMap.size() == 1;


            Field urlsField = clickHouseShardSupportClass.getDeclaredField("urls");
            urlsField.setAccessible(true);
            List urls = (List) urlsField.get(clickHouseShardSupport);

            assert urls != null && urls.size() == 1;


        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("获取私用变量失败");
        }
        System.out.println("success");
    }

    @Test
    public void sameIp() {

        ClickHouseShardSupport clickHouseShardSupport = new ClickHouseShardSupport("jdbc:clickhouse://192.168.1.1:8123,192.168.1.1:8123,192.168.1.1/default", "", "", 1, true);

        Class<ClickHouseShardSupport> clickHouseShardSupportClass = ClickHouseShardSupport.class;

        try {
            Field shardJdbcTemplateMapField = clickHouseShardSupportClass.getDeclaredField("shardJdbcTemplateMap");
            shardJdbcTemplateMapField.setAccessible(true);
            Map jdbcMap = (Map) shardJdbcTemplateMapField.get(clickHouseShardSupport);

            assert jdbcMap != null && jdbcMap.size() == 1;


            Field urlsField = clickHouseShardSupportClass.getDeclaredField("urls");
            urlsField.setAccessible(true);
            List urls = (List) urlsField.get(clickHouseShardSupport);

            assert urls != null && urls.size() == 1;


        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("获取私用变量失败");
        }
        System.out.println("success");
    }

    @Test
    public void diffIp() {

        ClickHouseShardSupport clickHouseShardSupport = new ClickHouseShardSupport("jdbc:clickhouse://192.168.1.1:8123,192.168.1.2:8123,192.168.1.3:8123/default", "", "", 1, true);

        Class<ClickHouseShardSupport> clickHouseShardSupportClass = ClickHouseShardSupport.class;

        try {
            Field shardJdbcTemplateMapField = clickHouseShardSupportClass.getDeclaredField("shardJdbcTemplateMap");
            shardJdbcTemplateMapField.setAccessible(true);
            Map jdbcMap = (Map) shardJdbcTemplateMapField.get(clickHouseShardSupport);

            assert jdbcMap != null && jdbcMap.size() == 3;


            Field urlsField = clickHouseShardSupportClass.getDeclaredField("urls");
            urlsField.setAccessible(true);
            List urls = (List) urlsField.get(clickHouseShardSupport);

            assert urls != null && urls.size() == 3;


        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("获取私用变量失败");
        }
        System.out.println("success");
    }


    @Test
    public void multipleClickSupport() throws InterruptedException {
        DataRuntime dataRuntime = initDataRuntime();

        int threadNumber = 10;
        CopyOnWriteArraySet<ClickHouseShardSupport> clickHouseShardSupports = new CopyOnWriteArraySet<>();
        CountDownLatch countDownLatch = new CountDownLatch(threadNumber);

        for (int i = 0; i < threadNumber; i++) {
            new Thread(new RunQuery(countDownLatch, clickHouseShardSupports, dataRuntime)).start();
        }

        countDownLatch.await();

        assert clickHouseShardSupports.size() == threadNumber;

    }

    @Test
    public void multipleClickSupport2() throws InterruptedException {
        DataRuntime dataRuntime = initDataRuntime();
        Injector inject = dataRuntime.getInstance(Injector.class);
        inject.injectMembers(this);
        assert clickhouseHolder.diffCKInstance() : "same clickHouseShardSupport instance";
    }

    public DataRuntime initDataRuntime() {
        DataBootstrap bootstrap = DataBootstrap.create("clickhouse.properties");
        bootstrap.install(new ClickHouseModule());
        return bootstrap.startRuntime();
    }

    @Singleton
    private static class ClickhouseHolder {
        @Inject
        private ClickHouseShardSupport clickHouseShardSupport;
        @Inject
        private ClickHouseShardSupport clickHouseShardSupport2;

        public boolean diffCKInstance() {
            return clickHouseShardSupport != clickHouseShardSupport2;
        }
    }

    private class RunQuery implements Runnable {

        private CountDownLatch countDownLatch;
        private CopyOnWriteArraySet<ClickHouseShardSupport> copyOnWriteArrayList;
        private DataRuntime dataRuntime;

        RunQuery(CountDownLatch countDownLatch, CopyOnWriteArraySet<ClickHouseShardSupport> copyOnWriteArrayList,
                 DataRuntime dataRuntime) {
            this.countDownLatch = countDownLatch;
            this.copyOnWriteArrayList = copyOnWriteArrayList;
            this.dataRuntime = dataRuntime;
        }

        @Override
        public void run() {
            ClickHouseShardSupport instance = dataRuntime.getInstance(ClickHouseShardSupport.class);
            copyOnWriteArrayList.add(instance);
            countDownLatch.countDown();
        }
    }

}
