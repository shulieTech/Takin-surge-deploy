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
import com.google.inject.name.Named;
import io.shulie.surge.data.common.utils.Bytes;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.TimeRange;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.apache.hadoop.hbase.util.Bytes.toBytes;

/**
 * 从 HBase 中读写数据
 *
 * @author pamirs
 * @author pamirs
 */
public class HBaseDAO {

    private Configuration conf;

    private StringColumnInterpreter sci;
    private HConnection connection;

    @Inject
    private HBaseDAO(@Named("config.hbase.zk") String zkServers,
                     @Named("config.hbase.zk.rootNode") String zkRootNode) {
        this.conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", zkServers);
        conf.set("zookeeper.znode.parent", zkRootNode);
        conf.set("hbase.zookeeper.property.clientPort", "2181"); // 客户端超时时间，默认1000
        conf.set("hbase.client.pause", "10000"); // 客户端超时时间，默认1000
        conf.set("hbase.client.retries.number", "5");// 超时失败的重试次数

        this.conf = conf;
        this.sci = new StringColumnInterpreter();
        try {
            this.connection = HConnectionManager.createConnection(conf, new ThreadPoolExecutor(0, conf.getInt("hbase.htable.pool.max", 100),
                    60L, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(),
                    new ThreadFactory() {
                        @Override
                        public Thread newThread(Runnable r) {
                            Thread t = new Thread(r);
                            t.setName("pradar-hbase-thread-pool");
                            return t;
                        }
                    }));
        } catch (ZooKeeperConnectionException e) {
            throw new RuntimeException("无法连接hbase的zookeeper集群", e);
        } catch (IOException e) {
            throw new RuntimeException("无法建立hbase连接,IO异常", e);
        } catch (Exception e)
        {
            throw new RuntimeException("无法建立hbase连接", e);
        }
    }

    public void insert(String tableName, String family, String rowKey, Map<String, String> keyValues)
            throws IOException {
        HTable table = getHTable(tableName);
        try {
            table.put(getRowPut(family, rowKey, keyValues));
        } finally {
            table.close();
        }
    }

    public void insert(String tableName, String family, List<String> rowKeys,
                       List<Map<String, String>> keyValueList) throws IOException {
        HTable table = getHTable(tableName);
        List<Put> rows = new ArrayList<Put>();

        byte[] f = toBytes(family);
        for (int i = 0; i < rowKeys.size(); i++)
            rows.add(getRowPut(f, rowKeys.get(i), keyValueList.get(i)));

        try {
            table.put(rows);
        } finally {
            table.close();
        }
    }

    public Put getRowPut(String family, String rowKey, Map<String, String> keyValues) throws IOException {
        byte[] f = toBytes(family);
        return getRowPut(f, rowKey, keyValues);
    }

    public Put getRowPut(byte[] family, String rowKey, Map<String, String> keyValues) throws IOException {
        Put row = new Put(toBytes(rowKey));

        for (Map.Entry<String, String> e : keyValues.entrySet()) {
            String v = e.getValue();
            if (v == null)
                v = "";
            row.add(family, toBytes(e.getKey()), toBytes(v));
        }

        return row;
    }

    /**
     * 使用模式:
     * <code>
     * <pre>
     * Query query = new Query();
     * query.setXXX();
     * .....
     * try {
     *    List&lt;Map&lt;String, String&gt;&gt; list = null;
     *    while((list = HBaseUtils.find(query)) != null) {
     *        ...do something with list....
     *    }
     * } finally {
     *    query.close();
     * }
     * </pre>
     * </code>
     *
     * @param query
     * @return 如果返回null，说明已经没有记录了，可以结束查找
     * @throws IOException
     */
    public List<Map<String, String>> find(Query query) throws IOException {
        if (query.getMaxRows() > 0 && query.currentRowCount > query.getMaxRows())
            return null;

        ResultScanner rs = getResultScanner(query);
        boolean resultIncludeRowKey = query.isResultIncludeRowKey();
        String resultMapKeyNameForRowKey = query.getResultMapKeyNameForRowKey();
        List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
        int nextRows = query.getNextRows();
        int maxVersions = query.getMaxVersions();

        Result[] results = rs.next(nextRows);
        if (results == null || results.length == 0) {
            return null;
        }

        query.currentRowCount += results.length;

        Map<String, String> map = null;
        //如果maxVersions等于1，说明同一个rowKey不会存在多条记录，所以不用区分timestamp
        if (maxVersions == 1) {
            for (Result result : results) {
                if (resultIncludeRowKey) {
                    map = new LinkedHashMap<String, String>(result.size() + 1);
                    map.put(resultMapKeyNameForRowKey, io.shulie.surge.data.common.utils.Bytes.toString(result.getRow()));
                } else {
                    map = new LinkedHashMap<String, String>(result.size());
                }

                for (KeyValue kv : result.raw()) {
                    map.put(io.shulie.surge.data.common.utils.Bytes.toString(kv.getQualifier()), io.shulie.surge.data.common.utils.Bytes.toString(kv.getValue()));
                }

                resultList.add(map);
            }
        } else { //多Version的情况，同一个rowKey会存在多条记录，相同timestamp的KeyValue会归到一组
            Map<Long, Map<String, String>> timestampMap;
            long timestamp;
            String rowKey;
            for (Result result : results) {
                timestampMap = new LinkedHashMap<Long, Map<String, String>>(maxVersions);
                rowKey = io.shulie.surge.data.common.utils.Bytes.toString(result.getRow());

                for (KeyValue kv : result.raw()) {
                    timestamp = kv.getTimestamp();
                    map = timestampMap.get(timestamp);
                    if (map == null) {
                        if (resultIncludeRowKey) {
                            map = new LinkedHashMap<String, String>(result.size() + 1);
                            map.put(resultMapKeyNameForRowKey, rowKey);
                        } else {
                            map = new LinkedHashMap<String, String>(result.size());
                        }

                        timestampMap.put(timestamp, map);
                        resultList.add(map);
                    }

                    map.put(io.shulie.surge.data.common.utils.Bytes.toString(kv.getQualifier()), Bytes.toString(kv.getValue()));
                }
            }
        }

        return resultList;
    }

    public ResultScanner getResultScanner(Query query) throws IOException {
        if (query.rs == null) {
            query.table = getHTable(query.getTableName());

            Scan scan = new Scan();
            if (query.getStartRow() != null)
                scan.setStartRow(toBytes(query.getStartRow()));
            if (query.getStopRow() != null)
                scan.setStopRow(toBytes(query.getStopRow()));

            if (query.getFilter() != null)
                scan.setFilter(query.getFilter());

            scan.setCaching(query.getClientCachingRows());
            scan.setMaxVersions(query.getMaxVersions());

            TimeRange tr = query.getTimeRange();
            if (tr != null)
                scan.setTimeRange(tr.getMin(), tr.getMax());
            query.rs = query.table.getScanner(scan);
        }
        return query.rs;
    }

    public void createTable(String tableName, String... familyNames) throws IOException {
        createTable(tableName, Compression.Algorithm.GZ, familyNames);
    }

    public void createTable(String tableName, Compression.Algorithm compressionType, String... familyNames)
            throws IOException {
        HBaseAdmin admin = new HBaseAdmin(conf);
        HTableDescriptor htd = new HTableDescriptor(tableName);

        for (String familyName : familyNames) {
            HColumnDescriptor hcd = new HColumnDescriptor(familyName);
            hcd.setCompressionType(compressionType);
            hcd.setDataBlockEncoding(DataBlockEncoding.FAST_DIFF);
            htd.addFamily(hcd);
        }

        if (!admin.tableExists(htd.getName())) {
            admin.createTable(htd);
        }
    }

    public void createTable(String tableName, HColumnDescriptor... columnDescriptors) throws IOException {
        HBaseAdmin admin = new HBaseAdmin(conf);
        HTableDescriptor htd = new HTableDescriptor(tableName);

        for (HColumnDescriptor columnDescriptor : columnDescriptors) {
            if (columnDescriptor.getDataBlockEncoding() == DataBlockEncoding.NONE)
                columnDescriptor.setDataBlockEncoding(DataBlockEncoding.FAST_DIFF);
            htd.addFamily(columnDescriptor);
        }

        if (!admin.tableExists(htd.getName())) {
            admin.createTable(htd);
        }
    }

    public void createTable(String tableName, byte[][] splitKeys, HColumnDescriptor... columnDescriptors)
            throws IOException {
        HBaseAdmin admin = new HBaseAdmin(conf);
        HTableDescriptor htd = new HTableDescriptor(tableName);

        for (HColumnDescriptor columnDescriptor : columnDescriptors) {
            if (columnDescriptor.getDataBlockEncoding() == DataBlockEncoding.NONE)
                columnDescriptor.setDataBlockEncoding(DataBlockEncoding.FAST_DIFF);
            htd.addFamily(columnDescriptor);
        }

        if (!admin.tableExists(htd.getName())) {
            admin.createTable(htd, splitKeys);
        }
    }

    public void delete(String tableName, String rowKey) throws IOException {
        HTable table = getHTable(tableName);
        try {
            table.delete(new Delete(toBytes(rowKey)));
        } finally {
            table.close();
        }
    }

    public void deleteTable(String tableName) throws IOException {
        HBaseAdmin admin = new HBaseAdmin(conf);
        if (!admin.tableExists(tableName)) {
            return;
        }
        admin.disableTable(tableName);
        admin.deleteTable(tableName);
    }

    public Configuration getConfiguration() {
        return HBaseConfiguration.create();
    }

    public HTable getHTable(String tableName) throws IOException {
        return (HTable) connection.getTable(tableName);
    }

    public void deleteAllConnections() {
        HConnectionManager.deleteAllConnections(true);
    }

}
