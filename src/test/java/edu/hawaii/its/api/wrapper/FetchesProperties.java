package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeAll;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public abstract class FetchesProperties {
    protected static Properties properties;
    @BeforeAll
    protected static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }
    protected String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
