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

package io.shulie.surge.data.deploy.pradar.digester;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.pamirs.pradar.log.parser.metrics.AppStatLogBased;
import io.shulie.surge.data.common.aggregation.AggregateSlot;
import io.shulie.surge.data.common.aggregation.DefaultAggregator;
import io.shulie.surge.data.common.aggregation.Scheduler;
import io.shulie.surge.data.common.aggregation.metrics.CallStat;
import io.shulie.surge.data.common.aggregation.metrics.Metric;
import io.shulie.surge.data.deploy.pradar.listener.AppStatLogResultListener;
import io.shulie.surge.data.runtime.common.remote.DefaultValue;
import io.shulie.surge.data.runtime.common.remote.Remote;
import io.shulie.surge.data.runtime.digest.DataDigester;
import io.shulie.surge.data.runtime.digest.DigestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: xingchen
 * @ClassName: MetricsDigester
 * @Package: io.shulie.surge.data.deploy.pradar.digester
 * @Date: 2020/11/1614:40
 * @Description:
 */
@Singleton
public class AppStatLogDigester implements DataDigester<AppStatLogBased> {
    private static Logger logger = LoggerFactory.getLogger(AppStatLogDigester.class);

    private static final String METRICS_ID = "app_stat_log";

    @Inject
    @DefaultValue("true")
    @Named("/pradar/config/rt/appStatLogMetricsDisable")
    private Remote<Boolean> appStatLogMetricsDisable;

    @Inject
    private AppStatLogResultListener appStatLogResultListener;

    private transient AtomicBoolean isRunning = new AtomicBoolean(false);

    private DefaultAggregator defaultAggregator;

    private Scheduler scheduler = new Scheduler(1);

    @Override
    public void digest(DigestContext<AppStatLogBased> context) {
        if (appStatLogMetricsDisable.get()) {
            return;
        }
        if (isRunning.compareAndSet(false, true)) {
            try {
                defaultAggregator = new DefaultAggregator(5, 60, scheduler);
                defaultAggregator.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        AppStatLogBased appStatLogBased = context.getContent();
        if (appStatLogBased == null) {
            logger.warn("parse AppStatLogBased is null " + context.getContent());
            return;
        }
        // 拼接唯一值
        String[] tags = new String[]{appStatLogBased.getAppName(), appStatLogBased.getLog(), appStatLogBased.getAgentId()};
        long timeStamp = appStatLogBased.getLogTime();
        AggregateSlot<Metric, CallStat> slot = defaultAggregator.getSlotByTimestamp(timeStamp);
        //
        //
        CallStat callStat = new CallStat(
                appStatLogBased.getErrorType(),
                appStatLogBased.getErrorContent(),
                appStatLogBased.getErrorCount()
        );

        slot.addToSlot(Metric.of(METRICS_ID, tags, "", new String[]{}), callStat);
        defaultAggregator.addListener(METRICS_ID, appStatLogResultListener);
    }

    @Override
    public int threadCount() {
        return 1;
    }

    @Override
    public void stop() throws Exception {
        defaultAggregator.stop();
    }
}
