package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static edu.hawaii.its.api.util.JsonUtil.asObject;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class FindGroupsResultsTest {

    private static Properties properties;
    private static final String SUCCESS = "SUCCESS";
    private static final String FAILURE = "FAILURE";

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
        assertThat(results.getGroup().getResultCode(), equalTo(FAILURE));
        assertThat(results.getGroup().getDescription(), equalTo(""));
    }

    @Test
    public void emptyConstruction() {
        FindGroupsResults results = new FindGroupsResults(new WsFindGroupsResults());
        assertThat(results.getGroup().getResultCode(), equalTo(FAILURE));
        assertThat(results.getGroup().getDescription(), equalTo(""));
    }

    @Test
    public void getDescription() {
        String json = propertyValue("find.groups.results.description");
        FindGroupsResults results =
                new FindGroupsResults(asObject(json, WsFindGroupsResults.class));
        assertThat(results.getGroup().getDescription(), equalTo("Test Many Groups In Basis"));
        assertThat(results.getGroup().getResultCode(), equalTo(SUCCESS));
    }

    @Test
    public void getNullDescription() {
        String json = propertyValue("find.groups.results.null.description");
        FindGroupsResults results =
                new FindGroupsResults(asObject(json, WsFindGroupsResults.class));
        assertThat(results.getGroup().getDescription(), equalTo(""));
        assertThat(results.getGroup().getResultCode(), equalTo(SUCCESS));
    }

    @Test
    public void getEmptyDescription() {
        String json = propertyValue("find.groups.results.empty.description");
        FindGroupsResults results =
                new FindGroupsResults(asObject(json, WsFindGroupsResults.class));
        assertThat(results.getGroup().getDescription(), equalTo(""));
        assertThat(results.getGroup().getResultCode(), equalTo(SUCCESS));
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
