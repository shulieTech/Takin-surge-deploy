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

package io.shulie.surge.data.common.aggregation.metrics;

import io.shulie.surge.data.common.aggregation.AggregatableRecord;
import io.shulie.surge.data.common.aggregation.TimestampSupport;
import io.shulie.surge.data.common.utils.Bytes;
import io.shulie.surge.data.common.utils.FormatUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Arrays;

/**
 * 实时调用统计
 *
 * @author pamirs
 */
public class CallStat implements AggregatableRecord<CallStat>, TimestampSupport {

    private static final long serialVersionUID = -2763638750561198315L;
    private static final long[] EMPTY_ARRAY = new long[0];

    private transient long timestamp;
    private long[] values;
    private String traceId;
    private String sqlStatement;

    public CallStat() {
        this("", EMPTY_ARRAY);
    }

    public CallStat(long... values) {
        this.values = values;
    }

    public CallStat(String traceId, long... values) {
        this.traceId = traceId;
        this.values = values;
    }

    public CallStat(String traceId, String sqlStatement, long... values) {
        this.traceId = traceId;
        this.sqlStatement = sqlStatement;
        this.values = values;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long get(int pos) {
        return pos >= values.length ? 0 : values[pos];
    }

    public void set(int pos, long value) {
        if (pos < values.length) {
            values[pos] = value;
        }
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSqlStatement() {
        return sqlStatement;
    }

    public void setSqlStatement(String sqlStatement) {
        this.sqlStatement = sqlStatement;
    }

    public void reset() {
        for (int i = 0; i < values.length; i++) {
            values[i] = 0;
        }
    }

    public int length() {
        return values.length;
    }

    @Override
    public void aggregateFrom(CallStat other) {
        final long[] values2 = other.values;
        final int len = values2.length;
        if (len != this.values.length) {
            if (this.values.length == 0) {
                // 刚初始化出来
                this.values = Arrays.copyOf(values2, len);
                this.traceId = other.traceId;
                //区分e2e断言指标和链路指标,不能使用isnotBlank,因为部分sql语句设置的为"null"
                if (other.sqlStatement != null) {
                    this.sqlStatement = other.sqlStatement;
                }
            }
            return;
        }

//        if (values2.length > 3 && other.values.length > 3) {
//            if (values[2] > 0 && values2[2] > 0) {
//                //第一次过来,初始化
//                if (StringUtils.isBlank(traceId)) {
//                    traceId = other.traceId;
//                }
//            }
//        }
        //如果长度为5,代表是断言指标数据,每次更新traceId为最新的
        if (len == 5) {
            traceId = other.traceId;
        }
        if (len <= 10) {
            switch (len) {
                //最大耗时计算
                case 10:
                    values[9] = Math.max(values[9], values2[9]);
                    if (values2[9] > values[9]) {
                        //计算最大耗时对应的traceId,原先的逻辑存在问题
                        traceId = other.traceId;
                        //保存sql语句
                        sqlStatement = other.sqlStatement;
                    }
                case 9:
                    values[8] += values2[8];
                case 8:
                    values[7] += values2[7];
                case 7:
                    values[6] += values2[6];
                case 6:
                    values[5] += values2[5];
                case 5:
                    values[4] += values2[4];
                case 4:
                    values[3] += values2[3];
                case 3:
                    values[2] += values2[2];
                case 2:
                    values[1] += values2[1];
                case 1:
                    values[0] += values2[0];
                case 0:
            }
        } else {
            for (int i = 0; i < len; ++i) {
                values[i] += values2[i];
            }
        }
    }

    @Override
    public byte[] toBytes() throws IOException {
        return Bytes.toBytes(FormatUtils.join(values, "|") + '|' + traceId);
    }

    @Override
    public void fromBytes(byte[] bytes) throws IOException {
        String str = Bytes.toString(bytes);
        String[] splits = StringUtils.split(str, '|');
        final int len = splits.length - 1;
        long[] values = new long[len];
        for (int i = 0; i < len; ++i) {
            values[i] = Long.parseLong(splits[i]);
        }
        this.values = values;
        this.traceId = splits[splits.length - 1];
    }

    @Override
    public String toString() {
        return "{" + FormatUtils.toSecondTimeString(timestamp) + ": " + FormatUtils.join(values, ",") + ',' + traceId + "}";
    }
}
