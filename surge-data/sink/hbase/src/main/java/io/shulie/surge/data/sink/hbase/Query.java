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

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.io.TimeRange;

import java.io.IOException;

/**
 * <b>Query用完之后必须在try finally中调用close方法释放资源.</b>
 *
 * @author pamirs
 *
 */
public class Query {
	private String tableName;

	private String startRow;
	private String stopRow;

	private int maxRows = -1; //client向server抓取的总行数
	private int nextRows = 50; //每次从client端缓存抓取的行数
	private int clientCachingRows = 500; //client端预缓存行数

	private boolean resultIncludeRowKey = true; //结果集中是否包含RowKey
	private String resultMapKeyNameForRowKey = "_rowKey"; //结果集每行对应一个map，这个字段对应RowKey值的map key名称

	private Filter filter;
	private int maxVersions = 1; //Integer.MAX_VALUE;
	private TimeRange timeRange = null;

	ResultScanner rs;
	HTable table;
	int currentRowCount; //当前client总共向server抓取的行数

	public Query() {
	}

	public Query(String tableName, String startRow) {
		setTableName(tableName);
		setStartRow(startRow);
	}

	public Query(String tableName, String startRow, String stopRow) {
		setTableName(tableName);
		setStartRow(startRow);
		setStopRow(stopRow);
	}

	public void close() {
		currentRowCount = 0;
		if (rs != null) {
			try {
				rs.close();
			} catch (Throwable e) {
			}
			rs = null;
		}

		if (table != null) {
			try {
				table.close();
			} catch (Throwable e) {
			}
			table = null;
		}
	}

	public int getCurrentRowCount() {
		return currentRowCount;
	}

	public boolean isResultIncludeRowKey() {
		return resultIncludeRowKey;
	}

	public Query setResultIncludeRowKey(boolean resultIncludeRowKey) {
		this.resultIncludeRowKey = resultIncludeRowKey;
		return this;
	}

	public String getResultMapKeyNameForRowKey() {
		return resultMapKeyNameForRowKey;
	}

	public Query setResultMapKeyNameForRowKey(String resultMapKeyNameForRowKey) {
		this.resultMapKeyNameForRowKey = resultMapKeyNameForRowKey;
		return this;
	}

	public int getClientCachingRows() {
		return clientCachingRows;
	}

	public Query setClientCachingRows(int clientCachingRows) {
		this.clientCachingRows = clientCachingRows;
		return this;
	}

	public String getTableName() {
		return tableName;
	}

	public Query setTableName(String tableName) {
		this.tableName = tableName;
		return this;
	}

	public String getStartRow() {
		return startRow;
	}

	public Query setStartRow(String startRow) {
		this.startRow = startRow;
		return this;
	}

	public String getStopRow() {
		return stopRow;
	}

	public Query setStopRow(String stopRow) {
		this.stopRow = stopRow;
		return this;
	}

	public int getMaxRows() {
		return maxRows;
	}

	public Query setMaxRows(int maxRows) {
		this.maxRows = maxRows;
		if (maxRows > 0) {
			if (maxRows < nextRows) {
				this.nextRows = maxRows;
			}
			if (maxRows < clientCachingRows) {
				this.clientCachingRows = maxRows;
			}
		}
		return this;
	}

	public int getNextRows() {
		return nextRows;
	}

	public Query setNextRows(int nextRows) {
		this.nextRows = nextRows;
		return this;
	}

	public Filter getFilter() {
		return filter;
	}

	public Query setFilter(Filter filter) {
		this.filter = filter;
		return this;
	}

	public int getMaxVersions() {
		return this.maxVersions;
	}

	public Query setMaxVersions(int maxVersions) {
		if (maxVersions < 1)
			throw new IllegalArgumentException("maxVersions must >= 1");
		this.maxVersions = maxVersions;
		return this;
	}

	public Query setTimeRange(TimeRange timeRange) {
		this.timeRange = timeRange;
		return this;
	}

	public Query setTimeRange(long minStamp, long maxStamp) throws IllegalArgumentException, IOException {
		return setTimeRange(new TimeRange(minStamp, maxStamp));
	}

	public TimeRange getTimeRange() {
		return timeRange;
	}
}
