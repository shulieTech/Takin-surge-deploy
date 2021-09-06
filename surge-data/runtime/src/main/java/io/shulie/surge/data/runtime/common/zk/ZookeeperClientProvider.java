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

package io.shulie.surge.data.runtime.common.zk;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 提供 ZkClient 的实现，默认注入 Log 配置，一般给 Log 自身使用
 * @author pamirs
 */
@Singleton
public class ZookeeperClientProvider implements Provider<ZooKeeper> {

	private static final Logger logger = LoggerFactory.getLogger(ZookeeperClientProvider.class);
	private ZooKeeper zooKeeper = null;

	@Inject
	public ZookeeperClientProvider(
			@Named("config.data.zk.servers") String zkServers,
			@Named("config.data.zk.sessionTimeoutMillis") int sessionTimeoutMillis) {

		try {
			zooKeeper = new ZooKeeper(zkServers, sessionTimeoutMillis, new Watcher() {
				@Override public void process(WatchedEvent event) {
					return;
				}
			});
		} catch (IOException e) {
			logger.error("init zookeeper client error.", e);
		}
	}

	@Override
	public ZooKeeper get() {
		return zooKeeper;
	}
}
