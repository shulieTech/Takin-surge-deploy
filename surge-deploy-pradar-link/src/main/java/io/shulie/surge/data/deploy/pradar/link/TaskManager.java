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

package io.shulie.surge.data.deploy.pradar.link;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: xingchen
 * @ClassName: TaskManager
 * @Package: io.shulie.surge.data.deploy.pradar.common
 * @Date: 2021/3/1618:53
 * @Description:
 */
@Singleton
public class TaskManager<T1, T2> {
    /*
     * 平均分配任务
     */
    public Map<T1, List<T2>> allotOfAverage(List<T1> workers, List<T2> tasks) {
        Map<T1, List<T2>> allot = Maps.newHashMap();
        if (workers != null && workers.size() > 0
                && tasks != null && tasks.size() > 0) {
            for (int i = 0; i < tasks.size(); i++) {
                int j = i % workers.size();
                if (allot.containsKey(workers.get(j))) {
                    List<T2> list = allot.get(workers.get(j));
                    list.add(tasks.get(i));
                    allot.put(workers.get(j), list);
                } else {
                    List<T2> list = new ArrayList<>();
                    list.add(tasks.get(i));
                    allot.put(workers.get(j), list);
                }
            }
        }
        return allot;
    }
}
