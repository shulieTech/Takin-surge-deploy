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

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class PropertyPlaceholderHelper {
    private static final Map<String, String> wellKnownSimplePrefixes = new HashMap(4, 1.0F);
    private final String placeholderPrefix;
    private final String placeholderSuffix;
    private final String simplePrefix;
    private final String valueSeparator;
    private final boolean ignoreUnresolvablePlaceholders;

    public PropertyPlaceholderHelper(String placeholderPrefix, String placeholderSuffix) {
        this(placeholderPrefix, placeholderSuffix, (String) null, true);
    }

    public PropertyPlaceholderHelper(String placeholderPrefix, String placeholderSuffix, String valueSeparator, boolean ignoreUnresolvablePlaceholders) {
        if (placeholderPrefix == null) {
            throw new NullPointerException("placeholderPrefix must not be null");
        } else if (placeholderSuffix == null) {
            throw new NullPointerException("placeholderSuffix must not be null");
        } else {
            this.placeholderPrefix = placeholderPrefix;
            this.placeholderSuffix = placeholderSuffix;
            String simplePrefixForSuffix = (String) wellKnownSimplePrefixes.get(this.placeholderSuffix);
            if (simplePrefixForSuffix != null && this.placeholderPrefix.endsWith(simplePrefixForSuffix)) {
                this.simplePrefix = simplePrefixForSuffix;
            } else {
                this.simplePrefix = this.placeholderPrefix;
            }

            this.valueSeparator = valueSeparator;
            this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
        }
    }

    public String replacePlaceholders(String value, final Properties properties) throws InvocationTargetException {
        if (properties == null) {
            throw new NullPointerException("properties must not be null");
        } else {
            return this.replacePlaceholders(value, new PlaceholderResolver() {
                public String resolvePlaceholder(String placeholderName) {
                    return properties.getProperty(placeholderName);
                }
            });
        }
    }

    public String replacePlaceholders(String value, PlaceholderResolver placeholderResolver) throws InvocationTargetException {
        if (value == null) {
            throw new NullPointerException("value must not be null");
        } else {
            return this.parseStringValue(value, placeholderResolver, new HashSet());
        }
    }

    protected String parseStringValue(String strVal, PlaceholderResolver placeholderResolver, Set<String> visitedPlaceholders) throws InvocationTargetException{
        StringBuilder buf = new StringBuilder(strVal);
        int startIndex = strVal.indexOf(this.placeholderPrefix);

        while (startIndex != -1) {
            int endIndex = this.findPlaceholderEndIndex(buf, startIndex);
            if (endIndex != -1) {
                String placeholder = buf.substring(startIndex + this.placeholderPrefix.length(), endIndex);
                String originalPlaceholder = placeholder;
                if (!visitedPlaceholders.add(placeholder)) {
                    throw new IllegalArgumentException("Circular placeholder reference '" + placeholder + "' in property definitions");
                }

                placeholder = this.parseStringValue(placeholder, placeholderResolver, visitedPlaceholders);
                String propVal = placeholderResolver.resolvePlaceholder(placeholder);
                if (propVal == null && this.valueSeparator != null) {
                    int separatorIndex = placeholder.indexOf(this.valueSeparator);
                    if (separatorIndex != -1) {
                        String actualPlaceholder = placeholder.substring(0, separatorIndex);
                        String defaultValue = placeholder.substring(separatorIndex + this.valueSeparator.length());
                        propVal = placeholderResolver.resolvePlaceholder(actualPlaceholder);
                        if (propVal == null) {
                            propVal = defaultValue;
                        }
                    }
                }

                if (propVal != null) {
                    propVal = this.parseStringValue(propVal, placeholderResolver, visitedPlaceholders);
                    buf.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal);
                    startIndex = buf.indexOf(this.placeholderPrefix, startIndex + propVal.length());
                } else {
                    if (!this.ignoreUnresolvablePlaceholders) {
                        throw new IllegalArgumentException("Could not resolve placeholder '" + placeholder + "' in string value \"" + strVal + "\"");
                    }

                    startIndex = buf.indexOf(this.placeholderPrefix, endIndex + this.placeholderSuffix.length());
                }

                visitedPlaceholders.remove(originalPlaceholder);
            } else {
                startIndex = -1;
            }
        }

        return buf.toString();
    }

    private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
        int index = startIndex + this.placeholderPrefix.length();
        int withinNestedPlaceholder = 0;

        while (index < buf.length()) {
            if (substringMatch(buf, index, this.placeholderSuffix)) {
                if (withinNestedPlaceholder <= 0) {
                    return index;
                }

                --withinNestedPlaceholder;
                index += this.placeholderSuffix.length();
            } else if (substringMatch(buf, index, this.simplePrefix)) {
                ++withinNestedPlaceholder;
                index += this.simplePrefix.length();
            } else {
                ++index;
            }
        }

        return -1;
    }

    public static boolean substringMatch(CharSequence str, int index, CharSequence substring) {
        for (int j = 0; j < substring.length(); ++j) {
            int i = index + j;
            if (i >= str.length() || str.charAt(i) != substring.charAt(j)) {
                return false;
            }
        }

        return true;
    }

    static {
        wellKnownSimplePrefixes.put("}", "{");
        wellKnownSimplePrefixes.put("]", "[");
        wellKnownSimplePrefixes.put(")", "(");
    }

    public interface PlaceholderResolver {
        String resolvePlaceholder(String var1) throws InvocationTargetException;
    }
}

