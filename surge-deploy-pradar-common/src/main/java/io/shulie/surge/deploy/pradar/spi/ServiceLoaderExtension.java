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

package io.shulie.surge.deploy.pradar.spi;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * spi扩展类，扩展了下serviceLoader处理
 *
 * @param <T>
 * @Author: xingchen
 * @Date @Date 2021/8/13 11:34
 */
public class ServiceLoaderExtension<T> {
    private static Logger logger = LoggerFactory.getLogger(ServiceLoaderExtension.class);
    private static final ConcurrentMap<Class<?>, ServiceLoaderExtension<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>(64);
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();
    private final Class<?> type;
    Strategy[] strategies = Strategy.defaultStrategy();

    public ServiceLoaderExtension(Class<T> type) {
        this.type = type;
    }

    public static <T> ServiceLoaderExtension<T> getExtensionLoader(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type == null");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type (" + type + ") is not an interface!");
        }
        ServiceLoaderExtension<T> loader = (ServiceLoaderExtension<T>) EXTENSION_LOADERS.get(type);
        if (loader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ServiceLoaderExtension<T>(type));
            loader = (ServiceLoaderExtension<T>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }

    public Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    private Map<String, Class<?>> loadExtensionClasses() {
        Map<String, Class<?>> extensionClasses = new HashMap<>();
        for (Strategy strategy : strategies) {
            loadDirectory(extensionClasses, strategy.getDirectory());
        }
        return extensionClasses;
    }

    public T createExtension(Class<?> clazz) {
        try {
            return (T) clazz.newInstance();
        } catch (Throwable t) {
            throw new IllegalStateException("Extension instance(class: " +
                    type + ")  could not be instantiated: " + t.getMessage(), t);
        }
    }

    private void loadDirectory(Map<String, Class<?>> extensionClasses, String dir) {
        String fileName = dir + type.getName();
        try {
            ClassLoader classLoader = ServiceLoaderExtension.class.getClassLoader();
            Enumeration<URL> urls = classLoader.getResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    java.net.URL resourceURL = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceURL);
                }
            }
        } catch (Throwable t) {
            logger.error("Exception occurred when loading extension class (interface: " +
                    type + ", description file: " + fileName + ").", t);
        }
    }

    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, java.net.URL resourceURL) {
        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceURL.openStream(), StandardCharsets.UTF_8))) {
                String line;
                String clazz = null;
                while ((line = reader.readLine()) != null) {
                    final int ci = line.indexOf('#');
                    if (ci >= 0) {
                        line = line.substring(0, ci);
                    }
                    line = line.trim();
                    if (line.length() > 0) {
                        try {
                            String name = "";
                            int i = line.indexOf('=');
                            if (i > 0) {
                                name = line.substring(0, i).trim();
                                clazz = line.substring(i + 1).trim();
                            } else {
                                // TODO 非键值对的暂时不处理
                                logger.warn("config is error {}" + line);
                            }
                            if (StringUtils.isNotBlank(clazz)) {
                                extensionClasses.put(name, Class.forName(clazz, true, classLoader));
                            }
                        } catch (Throwable t) {
                            logger.error("Failed to load extension class (interface: " + type + ", class line: " + line + ") in " + resourceURL + ", cause: " + t.getMessage());
                        }
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("Exception occurred when loading extension class (interface: " +
                    type + ", class file: " + resourceURL + ") in " + resourceURL, t);
        }
    }
}