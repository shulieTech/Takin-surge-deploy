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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.shulie.surge.data.common.batch.RotationBatch;
import org.apache.log4j.Logger;


/**
 * 返回单例的 {@link ElasticSearchProvider} 对象
 *
 * @author pamirs
 */
@Singleton
public class ElasticSearchProvider implements Provider<ElasticSearchSupport> {
    private static final Logger logger = Logger.getLogger(ElasticSearchProvider.class);

    private ElasticSearchSupport singleton;

    @Inject
    public ElasticSearchProvider(@Named("config.elasticsearch.url") String esUrl,
                                 @Named("config.elasticsearch.userName") String userName,
                                 @Named("config.elasticsearch.password") String password) {
        try {
            this.singleton = new DefaultElasticSearchSupport(esUrl, userName, password);
        } catch (Exception e) {
            logger.warn("ElasticSearchProvider init fail", e);
            throw e;
        }
    }

    @Override
    public ElasticSearchSupport get() {
        return this.singleton;
    }
}
