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

/**
 * @Author: xingchen
 * @ClassName: TraceFlagEnum
 * @Package: io.shulie.surge.data.deploy.pradar.common
 * @Date: 2021/3/3114:08
 * @Description:
 */
public enum TraceFlagEnum {
    /**
     * 日志类型-OK
     */
    LOG_OK("LOG_OK"),
    /**
     * 日志类型-错误
     */
    LOG_ERROR("LOG_ERROR");

    public String code;

    TraceFlagEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
