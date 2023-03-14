package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GetGroupsResultsTest {
    private static Properties properties;

    final static private String SUCCESS = "SUCCESS";

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }

    @Test
    public void constructor() {
        assertNotNull(new GetGroupsResults(null));
        String json = propertyValue("ws.get.groups.results.success");
        WsGetGroupsResults wsGetGroupsResults = JsonUtil.asObject(json, WsGetGroupsResults.class);
        GetGroupsResults getGroupsResults = new GetGroupsResults(wsGetGroupsResults);
        assertNotNull(getGroupsResults);
    }

    @Test
    public void successfulResults() {
        String json = propertyValue("ws.get.groups.results.success");
        WsGetGroupsResults wsGetGroupsResults = JsonUtil.asObject(json, WsGetGroupsResults.class);
        GetGroupsResults getGroupsResults = new GetGroupsResults(wsGetGroupsResults);
        assertNotNull(getGroupsResults);
        assertNotNull(getGroupsResults.getGroups());
        assertEquals(2, getGroupsResults.getGroups().size());
        assertNotNull(getGroupsResults.getSubject());
        assertEquals(SUCCESS, getGroupsResults.getResultCode());
    }

    @Test
    public void emptyGroups() {
        String json = propertyValue("ws.get.groups.results.empty.groups");
        WsGetGroupsResults wsGetGroupsResults = JsonUtil.asObject(json, WsGetGroupsResults.class);
        GetGroupsResults getGroupsResults = new GetGroupsResults(wsGetGroupsResults);
        assertNotNull(getGroupsResults);
        assertNotNull(getGroupsResults.getGroups());
        assertNotNull(getGroupsResults.getSubject());
        assertEquals("FAILURE", getGroupsResults.getResultCode());
    }
    @Test
    public void emptyResults() {
        String json = propertyValue("ws.empty.results");
        WsGetGroupsResults wsGetGroupsResults = JsonUtil.asObject(json, WsGetGroupsResults.class);
        GetGroupsResults getGroupsResults = new GetGroupsResults(wsGetGroupsResults);
        assertNotNull(getGroupsResults);
        assertNotNull(getGroupsResults.getGroups());
        assertNotNull(getGroupsResults.getSubject());
        assertEquals("FAILURE", getGroupsResults.getResultCode());
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
