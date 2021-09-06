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

package io.shulie.surge.data.sink.elasticsearch;

import io.shulie.surge.data.common.batch.CountRotationPolicy;
import io.shulie.surge.data.common.batch.RotationBatch;
import io.shulie.surge.data.common.doc.index.IndexConst;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基于 ElasticSearchSupport
 *
 * @author pamirs
 */
public class DefaultElasticSearchSupport implements ElasticSearchSupport {
    private static final Logger logger = LoggerFactory.getLogger(DefaultElasticSearchSupport.class);

    private static final String COMMA = ",";
    private RestHighLevelClient client;
    private AtomicBoolean enableBatch = new AtomicBoolean(true);
    private RotationBatch<HashMap> rotationBatch;
    private static final int DEFAULT_MAXSIZE = 500;

    /**
     * "http://192.168.1.111:9200,http://192.189.1.1:9200"
     *
     * @param elasticSearchUrl
     */
    public DefaultElasticSearchSupport(String elasticSearchUrl, String userName, String password) {
        try {
            String[] urls = elasticSearchUrl.split(COMMA);
            HttpHost[] httpHosts = new HttpHost[urls.length];
            for (int i = 0; i < urls.length; i++) {
                URL url = new URL(urls[i]);
                httpHosts[i] = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
            }
            // 增加安全验证
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));

            RestClientBuilder builder = RestClient.builder(httpHosts).setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                @Override
                public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                    requestConfigBuilder.setConnectTimeout(-1);
                    requestConfigBuilder.setSocketTimeout(-1);
                    requestConfigBuilder.setConnectionRequestTimeout(-1);
                    return requestConfigBuilder;
                }
            }).setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                @Override
                public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                    // 验证
                    httpClientBuilder.disableAuthCaching();
                    return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
            });
            client = new RestHighLevelClient(builder);

            // 处理批量新增
            if (enableBatch.get()) {
                rotationBatch = new RotationBatch();
                rotationBatch.rotationPolicy(new CountRotationPolicy(DEFAULT_MAXSIZE));
                rotationBatch.batchSaver(new RotationBatch.BatchSaver<HashMap<String, Object>>() {
                    @Override
                    public boolean saveBatch(LinkedBlockingQueue<HashMap<String, Object>> batchs) {
                        if (CollectionUtils.isNotEmpty(batchs)) {
                            final BulkRequest bulkRequest = new BulkRequest();
                            batchs.stream().forEach(batch -> {
                                Object index = batch.remove(IndexConst.DEFAULT_INDEX);
                                Object id = batch.remove(IndexConst.DEFAULT_ID);
                                UpdateRequest updateRequest = new UpdateRequest(Objects.toString(index), Objects.toString(id));
                                updateRequest.doc(batch, XContentType.JSON);
                                updateRequest.docAsUpsert(true);
                                bulkRequest.add(updateRequest);
                            });
                            try {
                                BulkResponse responses = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                                if (!responses.hasFailures()) {
                                    return true;
                                }
                            } catch (Throwable e) {
                                logger.error("bulk fail " + ExceptionUtils.getStackTrace(e));
                            }
                        }
                        return false;
                    }

                    @Override
                    public boolean shardSaveBatch(String key, LinkedBlockingQueue<HashMap<String, Object>> ObjectBatch) {
                        return false;
                    }
                });
            }
        } catch (Exception e) {
            logger.error("elasticsearch init fail" + ExceptionUtils.getStackTrace(e));
        } catch (Throwable e) {
            logger.error("elasticsearch init fail" + ExceptionUtils.getStackTrace(e));
            throw e;
        }
    }

    /**
     * 保存Map结构
     *
     * @param indexName
     * @param id
     * @param objMap
     * @throws Exception
     */
    @Override
    public void saveUpsertSync(String indexName, String id, Map<String, Object> objMap) throws Exception {
        UpdateRequest updateRequest = new UpdateRequest(indexName, id);
        updateRequest.doc(objMap, XContentType.JSON);
        updateRequest.docAsUpsert(true);

        client.update(updateRequest, RequestOptions.DEFAULT);
    }

    /**
     * 开启批量处理新增
     *
     * @param indexName
     */
    @Override
    public void saveBulkUpsertSync(String indexName, String id, HashMap<String, Object> objMap) {
        if (!enableBatch.get()) {
            throw new IllegalMonitorStateException("未开启批量,请调用saveUpsertSync");
        }
        if (StringUtils.isBlank(indexName)) {
            throw new IllegalArgumentException("indexName is null");
        }
        if (StringUtils.isBlank(id)) {
            throw new IllegalArgumentException("id is null");
        }
        objMap.put(IndexConst.DEFAULT_INDEX, indexName);
        objMap.put(IndexConst.DEFAULT_ID, id);
        rotationBatch.addBatch(objMap);
    }

    @Override
    public void stop() {
        try {
            if (enableBatch.get()) {
                rotationBatch.flush();
            }
            if (client != null) {
                client.close();
            }
        } catch (Throwable e) {
            logger.error("close elasticsearch fail " + ExceptionUtils.getStackTrace(e));
        }
    }
}
