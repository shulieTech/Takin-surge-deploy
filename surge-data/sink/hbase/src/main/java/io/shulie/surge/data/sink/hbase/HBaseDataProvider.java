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

package io.shulie.surge.data.sink.hbase;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Singleton
public class HBaseDataProvider {

    public static final int MAX_RESULT_SIZE = 80000;

    @Inject
    private HBaseDAO hbaseDAO;

    public Result[] scan(String table, Scan scan) throws IOException {
        HTable hTable = hbaseDAO.getHTable(table);
        if (hTable == null) {
            return new Result[0];
        }

        ResultScanner scanner = null;
        int resultSize;
        int setMaxSize = 0;//= (int) scan.getMaxResultSize();
        if (setMaxSize <= 0) {
            resultSize = MAX_RESULT_SIZE;
            //scan.setMaxResultSize(resultSize);
        } else {
            resultSize = setMaxSize;
        }
        scan.setCaching(resultSize);

        try {
            scanner = hTable.getScanner(scan);
            Result[] ret = scanner.next(resultSize);
            return ret;
        } finally {
            if (scanner != null) {
                try {
                    scanner.close();
                } catch (Throwable e) {
                }
            }
            if (hTable != null) {
                try {
                    hTable.close();
                } catch (Throwable e) {
                }
            }
        }
    }

    public List<Map<String, String>> find(Query query) throws IOException {
        return hbaseDAO.find(query);
    }

    public Result get(String table, Get get) throws IOException {

        HTable hTable = hbaseDAO.getHTable(table);
        if (hTable == null) {
            return new Result();
        }

        try {
            return hTable.get(get);
        } finally {
            try {
                hTable.close();
            } catch (Throwable e) {
            }
        }
    }

    public Result[] get(String table, List<Get> gets) throws IOException {
        HTable hTable = hbaseDAO.getHTable(table);
        if (hTable == null) {
            return new Result[0];
        }

        try {
            return hTable.get(gets);
        } finally {
            try {
                hTable.close();
            } catch (Throwable e) {
            }
        }
    }
}
