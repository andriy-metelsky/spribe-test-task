package com.spribe.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
    private static final String PROP_FILE = "env.properties";
    private static final Properties properties;

    static {
        properties = new Properties();
        try (InputStream is = ConfigManager.class
                .getClassLoader()
                .getResourceAsStream(PROP_FILE)) {
            if (is == null) {
                throw new RuntimeException("Could not find " + PROP_FILE + " in classpath");
            }
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load property file: " + PROP_FILE, e);
        }
    }

    public static String getProperty(String key) {
        String systemProp = System.getProperty(key);
        if (systemProp != null) return systemProp;

        String prop = properties.getProperty(key);
        if (prop != null) return prop;

        throw new RuntimeException("Property '" + key + "' not specified in config or system properties.");
    }

    public static String getProperty(String key, String defaultValue) {
        String systemProp = System.getProperty(key);
        if (systemProp != null) return systemProp;

        String prop = properties.getProperty(key);
        if (prop != null) return prop;

        return defaultValue;
    }

    public static String getBaseUrl() {
        return getProperty("base.url");
    }

    public static int getThreads() {
        String value = System.getProperty("threads");
        if (value == null) {
            value = properties.getProperty("threads", "1");
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid integer value for 'threads': " + value);
        }
    }

    public static String getParallelMode() {
        return getProperty("parallel", "methods");
    }
}