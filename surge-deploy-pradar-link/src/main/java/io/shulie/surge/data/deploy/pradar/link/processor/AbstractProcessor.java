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

package io.shulie.surge.data.deploy.pradar.link.processor;

import io.shulie.surge.deploy.pradar.common.CommonStat;

import java.util.List;

/**
 * @Author: xingchen
 * @ClassName: AbstractProcessor
 * @Package: io.shulie.surge.data.deploy.pradar.link.processor
 * @Date: 2021/7/3013:11
 * @Description:
 */
public abstract class AbstractProcessor implements Processor {
    private String dataSourceType;

    /**
     * 延迟时间
     */
    private long delayTime = System.currentTimeMillis();

    /**
     * 判断当前时间是否落在任务执行时间间隔内
     * 从而说明当前任务需要执行
     * @param intervalTime 数据时间跨度（秒）
     * @param taskExecTimeInterval 任务执行时间间隔（秒）
     * @return
     */
    public boolean isHandler(long intervalTime, long taskExecTimeInterval) {
        if (intervalTime == 0) {
            return true;
        }
        long currentTime = System.currentTimeMillis();
        return currentTime % (intervalTime * 1000) < taskExecTimeInterval * 1000;
    }

    public String getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    /**
     * 默认使用CK
     *
     * @return
     */
    public boolean isUseCk() {
        return CommonStat.isUseCk(this.dataSourceType);
    }


    @Override
    public void share(int taskId) {

    }

    @Override
    public void share() {

    }

    @Override
    public void share(List<String> taskIds, String currentTaskId) {

    }

    @Override
    public void init() {

    }
}
