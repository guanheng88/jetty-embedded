package com.eros.demo.utils;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.xml.DOMConfigurator;

public abstract class Configuration {

    private static Properties properties;

    public static Properties getProperties() {
        return properties;
    }

    public static void loadProperties(String propertiesFilePath) throws Exception {
        if (properties == null) {
            properties = new Properties();
        }
        InputStream is = null;
        try {
            is = FileUtils.openInputStream(new File(propertiesFilePath));
            properties.load(is);
        }
        catch (Exception e) {
            System.err.println("Cannot load file: " + propertiesFilePath);
            throw new RuntimeException(e);
        }
        finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static void loadLog4j(String xmlFilePath) {
        DOMConfigurator.configure(xmlFilePath);
    }

    public static String getString(String name) {
        String valueString = properties.getProperty(name);

        if (valueString == null) {
            throw new RuntimeException("Cannot find property: " + name);
        }
        return valueString;
    }

    public static int getInteger(String name) {
        String valueString = getString(name);

        return Integer.parseInt(valueString);
    }
}
