package edu.hawaii.its.api.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Set;

public class PropertyLocator {

    private static final Log logger = LogFactory.getLog(PropertyLocator.class);
    private final Properties properties = new Properties();
    private boolean loaded = false;

    public PropertyLocator(Path path) {
        try {
            File file = path.toFile();
            properties.load(new FileInputStream(file));
            loaded = true;
        } catch (IOException e) {
            logger.error("Error: " + e);
        }
    }

    public PropertyLocator(String pathStr) {
        this(Paths.get(pathStr));
    }

    public PropertyLocator(String parent, String filename) {
        this(Paths.get(parent, filename));
    }

    public String find(String key) {
        return properties.getProperty(key);
    }

    public Set<String> getKeys() {
        return properties.stringPropertyNames();
    }

    public boolean isLoaded() {
        return this.loaded;
        }
}