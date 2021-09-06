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

package io.shulie.surge.data.deploy.pradar.common;

import io.shulie.surge.data.runtime.common.DataBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;

/**
 * @author anjone
 * @data 2021/8/4
 */
public class DataBootstrapEnhancer {

    private static Logger logger = LoggerFactory.getLogger(DataBootstrapEnhancer.class);

    private DataBootstrapEnhancer() {
    }

    public static void enhancer(DataBootstrap dataBootstrap) {
        Objects.requireNonNull(dataBootstrap);

        Properties externalProperties = loadExternalProperties();

        if (externalProperties == null || externalProperties.size() == 0) {
            logger.warn("externalProperties is empty : ");
            return;
        }

        aliasProperties(externalProperties);

        dataBootstrap.getProperties().putAll(externalProperties);
    }

    private static void aliasProperties(Properties externalProperties) {

        String aliasFile = PradarStormConfigHolder.getProperties(ParamUtil.EXTERNAL_ALIAS_FILE_KEY);
        if (aliasFile == null || aliasFile.length() == 0) {
            logger.info("aliasFile is null");
            return;
        }
        Properties aliasProperties = DataBootstrap.readConfig(aliasFile);
        if (aliasProperties.size() == 0) {
            logger.info("aliasProperties is empty");
            return;
        }
        changePlaceholder(aliasProperties, externalProperties);
        externalProperties.putAll(aliasProperties);
    }

    /**
     * 获取外部配置文件
     *
     * @return
     */
    private static Properties loadExternalProperties() {
        String externalPaths = PradarStormConfigHolder.getProperties(ParamUtil.EXTERNAL_PROPERTIES_PATHS_KEY);

        if (externalPaths == null || externalPaths.isEmpty()) {
            logger.info("SURGE_EXTERNAL_PATH_KEY is empty");
            return null;
        }

        Properties ret = new Properties();

        for (String externalPath : externalPaths.split(",")) {
            try {
                Properties externalProperties = new Properties();
                BufferedReader bufferedReader = new BufferedReader(new FileReader(externalPath));
                externalProperties.load(bufferedReader);
                ret.putAll(externalProperties);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return ret;
    }

    /**
     * 占位符替换
     *
     * @param originalProperties
     * @param properties
     */
    public static void changePlaceholder(Properties originalProperties, Properties properties) {
        PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper("${", "}");
        Enumeration<Object> keys = originalProperties.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            String value = originalProperties.get(key).toString();
            try {
                String newValue = propertyPlaceholderHelper.replacePlaceholders(value, properties);
                originalProperties.setProperty(key.toString(), newValue);
            } catch (Exception e) {
                logger.error("error replace placeholders :" + value);
                throw new RuntimeException("error replace placeholders :" + value, e);
            }
        }
    }
}
