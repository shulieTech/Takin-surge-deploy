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

import io.shulie.surge.deploy.pradar.common.CommonStat;

import java.util.Map;

/**
 * @Author: xingchen
 * @ClassName: ParamUtil
 * @Package: io.shulie.surge.data.deploy.pradar.common
 * @Date: 2021/7/3014:01
 * @Description:
 */
public class ParamUtil {
    //ringbuffer的线程数,此参数默认获取当前机器的cpu核数*2
    public static final String CORE_SIZE = "-DCoreSize=";
    public static final String REGISTERZK = "-DRegisterZK=";
    // workers数量
    public static final String WORKERS = "-DWorkers=";
    // 主机名和ip的映射-DHostName='{\"hostName1\":\"192,168,1,1\",\"hostName2\":\"192.168.1.3\"}'
    public static final String HOSTNAME = "-DHostName=";
    // 内外网映射-DNet='{\"192.189.1:192.2.3.22\",\"168,1,1.1\":\"10.1.1.1\"}'
    public static final String NET = "-DNet=";
    // 数据源类型切换
    public static final String DATA_SOURCE_TYPE = "-DSourceType=";
    // ip对应的端口段  -DPORTS='{"192.168.0.5":"[299900,29995]","192.168.0.6":"[29900,29995]","192.168.0.7":"[29900,29995]"}
    public static final String PORTS = "-DPORTS=";
    // 是否通用版本,默认是
    public static final String GENERAL_VERSION = "-DGeneralVersion";
    //外部配置文件 传入绝对路径 多个路径用,分割
    public static final String EXTERNAL_PROPERTIES_PATHS_KEY = "-DExternal.properties.paths";
    //配置文件别名映射 classpath路径下
    public static final String EXTERNAL_ALIAS_FILE_KEY = "-DExternal.alias.file";

    /**
     * 读取系统参数，是否设置ip映射
     */
    public static void parseInputParam(Map<String, String> conf, String[] args) {
        // 设置默认值
        conf.put(ParamUtil.REGISTERZK, CommonStat.TRUE);
        conf.put(ParamUtil.CORE_SIZE, "0");
        conf.put(ParamUtil.DATA_SOURCE_TYPE, CommonStat.CLICKHOUSE);
        conf.put(ParamUtil.WORKERS, CommonStat.WORKERS);
        conf.put(ParamUtil.GENERAL_VERSION, CommonStat.TRUE);

        if (args != null && args.length == 1 && !args[0].startsWith("-D")) {
            // 兼容只设置worker的情况
            conf.put(ParamUtil.WORKERS, args[0]);
        } else if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String param = args[i];
                if (param.startsWith(ParamUtil.CORE_SIZE)) {
                    conf.put(ParamUtil.CORE_SIZE, param.replace(ParamUtil.CORE_SIZE, ""));
                } else if (param.startsWith(ParamUtil.WORKERS)) {
                    conf.put(ParamUtil.WORKERS, param.replace(ParamUtil.WORKERS, ""));
                } else if (param.startsWith(ParamUtil.HOSTNAME)) {
                    conf.put(ParamUtil.HOSTNAME, param.replace(ParamUtil.HOSTNAME, ""));
                } else if (param.startsWith(ParamUtil.NET)) {
                    conf.put(ParamUtil.NET, param.replace(ParamUtil.NET, ""));
                } else if (param.startsWith(ParamUtil.DATA_SOURCE_TYPE)) {
                    conf.put(ParamUtil.DATA_SOURCE_TYPE, param.replace(ParamUtil.DATA_SOURCE_TYPE, ""));
                } else if (param.startsWith(ParamUtil.PORTS)) {
                    conf.put(ParamUtil.PORTS, param.replace(ParamUtil.PORTS, ""));
                } else if (param.startsWith("-D")) {
                    String[] split = param.split("=");
                    conf.put(split[0], split[1]);
                }
            }
        }
    }
}
