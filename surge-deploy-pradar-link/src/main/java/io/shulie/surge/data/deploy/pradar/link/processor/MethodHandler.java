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

import com.google.common.collect.Maps;
import io.shulie.surge.data.deploy.pradar.link.model.RequestMethod;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class MethodHandler {
    public static Map<String, String> methodMap = Maps.newHashMap();

    static {
        for (RequestMethod method : RequestMethod.values()) {
            methodMap.put(method.name(), method.name());
        }
    }

    public static String convert(String arg) {
        if (StringUtils.isBlank(arg)) {
            return arg;
        }
        return methodMap.containsKey(arg.toUpperCase()) ? "" : arg;
    }

    public static void main(String[] args) {
        System.out.println(convert("Server"));
        System.out.println(convert("GET"));
    }
}