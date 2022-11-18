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
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.shulie.surge.data.common.aggregation.Scheduler;
import io.shulie.surge.data.common.utils.DateUtils;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 任务管理器
 *
 * @Author: xingchen
 * @ClassName: TaskManager
 * @Package: io.shulie.surge.data.deploy.pradar.common
 * @Date: 2021/3/1618:53
 * @Description:
 */
@Singleton
public class TaskManager<T2> implements Serializable {

    @Inject
    private MysqlSupport mysqlSupport;

    private List<TaskNode> workers;

    private TaskNode taskNode = new TaskNode();


    //注册节点
    @Inject
    public void init() {
        Scheduler scheduler = new Scheduler(1);
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                //注册节点
                mysqlSupport.update("insert into t_amdb_link_task_node(uuid,pid,ip,host) values(?,?,?,?) ON DUPLICATE KEY UPDATE gmt_modify = now()", new Object[]{taskNode.getUuid(), taskNode.getPid(), taskNode.getIp(), taskNode.getHost()});
                //查询注册的节点信息
                Date date = DateUtils.addSeconds(new Date(), -30);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String modifyDate = formatter.format(date);
                List<TaskNode> list = mysqlSupport.query("select id,uuid,pid,ip,host from t_amdb_link_task_node where gmt_modify>= '" + modifyDate + "'", new BeanPropertyRowMapper<TaskNode>(TaskNode.class));
                workers = list;
            }
        }, 0, 30, TimeUnit.SECONDS);
    }


    /*
     * 平均分配任务
     */
    public List<T2> allotOfAverage(List<T2> tasks) {
        Map<String, List<T2>> allot = Maps.newHashMap();
        if (workers != null && workers.size() > 0 && tasks != null && tasks.size() > 0) {
            for (int i = 0; i < tasks.size(); i++) {
                int j = i % workers.size();
                if (allot.containsKey(workers.get(j).getUuid())) {
                    List<T2> list = allot.get(workers.get(j));
                    list.add(tasks.get(i));
                    allot.put(workers.get(j).getUuid(), list);
                } else {
                    List<T2> list = new ArrayList<>();
                    list.add(tasks.get(i));
                    allot.put(workers.get(j).getUuid(), list);
                }
            }
        }
        return allot.get(taskNode.getUuid());
    }

}
