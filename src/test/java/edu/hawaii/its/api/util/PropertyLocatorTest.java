package edu.hawaii.its.api.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class PropertyLocatorTest {

    private static final Path RESOURCES_HOME_PATH = Paths.get("src", "test", "resources");
    private static final Path PROPERTIES_HOME_PATH = RESOURCES_HOME_PATH.resolve("PropertyLocatorTestFiles");
    private static final String PROPERTIES_HOME = PROPERTIES_HOME_PATH.toString();

    @Test
    public void testConstructions() {
        Path path = Paths.get(PROPERTIES_HOME, "test.properties");
        PropertyLocator propertyLocator = new PropertyLocator(path);
        assertThat(propertyLocator.isLoaded(), equalTo(true));
        Set<String> keys = propertyLocator.getKeys();
        assertThat((keys.size()), equalTo(31));

        String pathStr = PROPERTIES_HOME_PATH.resolve("test.properties").toString();
        propertyLocator = new PropertyLocator(pathStr);
        assertThat(propertyLocator.isLoaded(), equalTo(true));
        keys = propertyLocator.getKeys();
        assertThat((keys.size()), equalTo(31));

        path = PROPERTIES_HOME_PATH.resolve("test.properties");
        propertyLocator = new PropertyLocator(path);
        assertThat(propertyLocator.isLoaded(), equalTo(true));
        keys = propertyLocator.getKeys();
        assertThat((keys.size()), equalTo(31));
    }

    @Test
    public void find() {
        String pathStr = PROPERTIES_HOME_PATH.resolve("test.properties").toString();
        PropertyLocator propertyLocator = new PropertyLocator(pathStr);
        String value = propertyLocator.find("groupings.api.stem");
        assertThat(value, equalTo(""));

        value = propertyLocator.find("groupings.api.settings");
        assertThat(value, equalTo("uh-settings"));

        value = propertyLocator.find("groupings.api.attributes");
        assertThat(value, equalTo("${groupings.api.settings}:attributes"));

        value = propertyLocator.find("groupings.api.owners");
        assertThat(value, equalTo(":owners"));

        value = propertyLocator.find("groupings.api.documentation.contact.email");
        assertThat(value, equalTo("contact@example.com"));

        value = propertyLocator.find("TestKey");
        assertThat(value, equalTo("TestValue"));

        value = propertyLocator.find("bogus");
        assertThat(value, nullValue());

        value = propertyLocator.find("bogus\n");
        assertThat(value, nullValue());

        value = propertyLocator.find("groupings.api.settings\n");
        assertThat(value, nullValue());

        value = propertyLocator.find("\n\t\t\n");
        assertThat(value, nullValue());
    }

    @Test
    public void nonExistantfile() {
        File file = Paths.get("non-existent-file.properties").toFile();
        assertThat(file.exists(), equalTo(false));

        PropertyLocator propertyLocator = new PropertyLocator(file.getAbsolutePath());
        assertThat(propertyLocator.isLoaded(), equalTo(false));
    }

    @Test
    public void getKeys() {
        Path path = PROPERTIES_HOME_PATH.resolve("test.properties");
        PropertyLocator propertyLocator = new PropertyLocator(path);
        assertThat(propertyLocator.isLoaded(), equalTo(true));
        Set<String> keys = propertyLocator.getKeys();
        assertThat(keys.size(), equalTo(31));
        assertThat(keys, hasItem("attribute.assignment.empty.result"));
        assertThat(keys, hasItem("groupings.api.settings"));

        propertyLocator = new PropertyLocator(PROPERTIES_HOME, "Empty.properties");
        keys = propertyLocator.getKeys();
        assertThat(keys.size(), equalTo(0));
    }

    @Test
    public void loaded() {
        Path path = PROPERTIES_HOME_PATH.resolve("test.properties");

        PropertyLocator propertyLocator = new PropertyLocator(path);
        assertThat(propertyLocator.isLoaded(), equalTo(true));

        propertyLocator = new PropertyLocator(path);
        assertThat(propertyLocator.isLoaded(), equalTo(true));

        propertyLocator = new PropertyLocator("src/DoesNotExist/Invalid.properties");
        assertThat(propertyLocator.isLoaded(), equalTo(false));
    }
}
