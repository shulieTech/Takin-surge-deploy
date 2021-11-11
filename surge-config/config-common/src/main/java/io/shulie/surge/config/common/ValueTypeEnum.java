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

package io.shulie.surge.config.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ValueTypeEnum {
    STRING_PLAINTEXT("String", "字符串-明文"),
    NUMBER_CIPHERTEXT("String", "字符串-密文"),
    NUMBER("number", "数字"),
    BOOL("boolean", "布尔值：0/1");

    private final String type;
    private final String desc;

    public static ValueTypeEnum findByOrdinal(Integer ordinal) {
        if (ordinal == null) {
            return STRING_PLAINTEXT;
        }
        for (ValueTypeEnum value : values()) {
            if (value.ordinal() == ordinal) {
                return value;
            }
        }
        return STRING_PLAINTEXT;
    }
}