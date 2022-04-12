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

package io.shulie.surge.data.deploy.pradar.report;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ReportTaskNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportTaskNotifier.class);

    private final String host;
    private final int port;
    private final String path;

    @Inject
    public ReportTaskNotifier(
        @Named("tro.url.ip") String host, @Named("tro.port") String port, @Named("tro.report.task.path") String path) {
        this.host = host;
        this.port = Integer.parseInt(port);
        this.path = path;
    }

    public void notifyTaskCompleted(String reportId) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPut request = new HttpPut(path.concat(reportId));
            CloseableHttpResponse response = httpClient.execute(new HttpHost(host, port), request);
            String result = EntityUtils.toString(response.getEntity());
            LOGGER.info("压测报告数据分析完成通知成功:  result=[{}]", result);
        } catch (Exception e) {
            LOGGER.info("压测报告数据分析完成通知异常:  message=[{}]", e.getMessage());
        }
    }
}
