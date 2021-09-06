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


import io.shulie.surge.data.common.utils.Bytes;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.coprocessor.LongColumnInterpreter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class StringColumnInterpreter extends LongColumnInterpreter {
	private Set<String> errorColumns = new HashSet<String>();

	private static class StringColumnInterpreterException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		private StringColumnInterpreterException(String column, Throwable t) {
			super("Column " + column + " not number type", t);
		}
	}

	@Override
	public Long getValue(byte[] colFamily, byte[] colQualifier, Cell cell) throws IOException {
		if (cell == null)
			//if (kv == null || kv.getValue().length != Bytes.SIZEOF_LONG)
			return null;

		Long value = 0L;
		try {
			value = Long.parseLong(Bytes.toString(cell.getValue()));
		} catch (NumberFormatException e) {
			String column = Bytes.toString(colFamily) + ":" + Bytes.toString(colQualifier);
			if (!errorColumns.contains(column)) {
				errorColumns.add(column);
				new StringColumnInterpreterException(column, e).printStackTrace();
			}
		}
		return value;
	}
}