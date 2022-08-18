package edu.hawaii.its.api.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;

class PropertyLocatorTest {

    private static final String PROPERTIES_HOME = "src/test/resources/PropertyLocatorTestFiles/";

    @Test
    public void testconstructions() {
        PropertyLocator propertyLocator = new PropertyLocator(Paths.get(PROPERTIES_HOME + "test.properties"));
        assertThat(propertyLocator.isLoaded(), equalTo(true));
        Set<String> keys = propertyLocator.getKeys();
        assertThat((keys.size()), equalTo(31));

        propertyLocator = new PropertyLocator(PROPERTIES_HOME + "test.properties");
        assertThat(propertyLocator.isLoaded(), equalTo(true));
        keys = propertyLocator.getKeys();
        assertThat((keys.size()), equalTo(31));

        propertyLocator = new PropertyLocator(PROPERTIES_HOME, "test.properties");
        assertThat(propertyLocator.isLoaded(), equalTo(true));
        keys = propertyLocator.getKeys();
        assertThat((keys.size()), equalTo(31));
    }

    @Test
    public void find() {
        PropertyLocator propertyLocator = new PropertyLocator(PROPERTIES_HOME + "test.properties");
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
    public void nonexistantfile() {
        File file = Paths.get("non-existent-file.properties").toFile();
        assertThat(file.exists(), equalTo(false));

        PropertyLocator propertyLocator = new PropertyLocator(file.getAbsolutePath());
        assertThat(propertyLocator.isLoaded(), equalTo(false));
    }

    @Test
    public void getkeys() {
        PropertyLocator propertyLocator = new PropertyLocator(PROPERTIES_HOME + "test.properties");
        Set<String> keys = propertyLocator.getKeys();
        assertThat((keys.size()), equalTo(31));
        assertThat(keys, hasItem("attribute.assignment.empty.result"));
        assertThat(keys, hasItem("groupings.api.settings"));

        propertyLocator = new PropertyLocator(PROPERTIES_HOME + "Empty.properties");
        keys = propertyLocator.getKeys();
        assertThat((keys.size()), equalTo(0));
    }

    @Test
    public void loaded() {
        PropertyLocator propertyLocator = new PropertyLocator(PROPERTIES_HOME + "test.properties");
        assertThat(propertyLocator.isLoaded(), equalTo(true));

        propertyLocator = new PropertyLocator(PROPERTIES_HOME + "Empty.properties");
        assertThat(propertyLocator.isLoaded(), equalTo(true));

        propertyLocator = new PropertyLocator("src/DoesNotExist/Invalid.properties");
        assertThat(propertyLocator.isLoaded(), equalTo(false));
    }
}
