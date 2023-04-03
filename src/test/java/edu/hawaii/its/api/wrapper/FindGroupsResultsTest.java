package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static edu.hawaii.its.api.util.JsonUtil.asObject;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class FindGroupsResultsTest {

    private static Properties properties;

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }

    @Test
    public void nullConstruction() {
        FindGroupsResults results = new FindGroupsResults(null);
        assertThat(results, is(notNullValue()));
        assertThat(results.getResultCode(), equalTo(null));
        assertThat(results.getDescription(), equalTo("No description given for this Grouping."));
    }

    @Test
    public void emptyConstruction() {
        FindGroupsResults results = new FindGroupsResults(new WsFindGroupsResults());
        assertThat(results.getResultCode(), equalTo(null));
        assertThat(results.getDescription(), equalTo("No description given for this Grouping."));
    }

    @Test
    public void getDescription() {
        String json = propertyValue("find.groups.results.description");
        FindGroupsResults results =
                new FindGroupsResults(asObject(json, WsFindGroupsResults.class));
        assertThat(results.getDescription(), equalTo("Test Many Groups In Basis"));
        assertThat(results.getResultCode(), equalTo("SUCCESS"));
    }

    @Test
    public void getNullDescription() {
        String json = propertyValue("find.groups.results.null.description");
        FindGroupsResults results =
                new FindGroupsResults(asObject(json, WsFindGroupsResults.class));
        assertThat(results.getDescription(), equalTo("No description given for this Grouping."));
        assertThat(results.getResultCode(), equalTo("SUCCESS"));
    }

    @Test
    public void getEmptyDescription() {
        String json = propertyValue("find.groups.results.empty.description");
        FindGroupsResults results =
                new FindGroupsResults(asObject(json, WsFindGroupsResults.class));
        assertThat(results.getDescription(), equalTo("No description given for this Grouping."));
        assertThat(results.getResultCode(), equalTo("SUCCESS"));
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
