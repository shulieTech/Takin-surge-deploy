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

package io.shulie.surge.deploy.pradar.spi;

/**
 * @Author: xingchen
 * @ClassName: Strategy
 * @Package: io.shulie.surge.deploy.pradar.spi
 * @Date: 2021/8/1221:40
 * @Description:
 */
public class Strategy {
    private String directory;

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public Strategy(String directory) {
        this.directory = directory;
    }

    /**
     * 默认加载/META-INF/system 和 META-INF/users目录下的
     */
    public static Strategy[] defaultStrategy() {
        Strategy[] strategies = new Strategy[]{
                new Strategy("META-INF/system/"),
                new Strategy("META-INF/users/")};
        return strategies;
    }
}
