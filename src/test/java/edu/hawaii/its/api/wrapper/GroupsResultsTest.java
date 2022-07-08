package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GroupsResultsTest {

    private static Properties properties;

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }

    @Test
    public void construction() {
        String json = propertyValue("groups.results");
        WsGetGroupsResults wsGetGroupsResults = JsonUtil.asObject(json, WsGetGroupsResults.class);
        GroupsResults groupsResults = new GroupsResults(wsGetGroupsResults);
        assertNotNull(wsGetGroupsResults);
        assertNotNull(groupsResults);

        groupsResults = new GroupsResults(null);
        assertNotNull(groupsResults);
    }

    @Test
    public void groupPathsTest() {
        String json = propertyValue("groups.results");
        WsGetGroupsResults wsGetGroupsResults = JsonUtil.asObject(json, WsGetGroupsResults.class);
        GroupsResults groupsResults = new GroupsResults(wsGetGroupsResults);
        List<String> groupPaths = groupsResults.groupPaths();
        assertNotNull(wsGetGroupsResults);
        assertNotNull(groupsResults);
        assertNotNull(groupPaths);
        assertFalse(groupPaths.isEmpty());

        groupsResults = new GroupsResults(null);
        groupPaths = groupsResults.groupPaths();
        assertNotNull(groupPaths);
        assertTrue(groupPaths.isEmpty());

        json = propertyValue("groups.results.empty.results");
        wsGetGroupsResults = JsonUtil.asObject(json, WsGetGroupsResults.class);
        groupsResults = new GroupsResults(wsGetGroupsResults);
        groupPaths = groupsResults.groupPaths();
        assertNotNull(wsGetGroupsResults);
        assertNotNull(groupsResults);
        assertNotNull(groupPaths);
        assertTrue(groupPaths.isEmpty());

        json = propertyValue("groups.results.empty.groups");
        wsGetGroupsResults = JsonUtil.asObject(json, WsGetGroupsResults.class);
        groupsResults = new GroupsResults(wsGetGroupsResults);
        groupPaths = groupsResults.groupPaths();
        assertNotNull(wsGetGroupsResults);
        assertNotNull(groupsResults);
        assertNotNull(groupPaths);
        assertTrue(groupPaths.isEmpty());
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
