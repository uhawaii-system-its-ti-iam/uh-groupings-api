package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;

public class GroupSaveResultsTest {
    private static Properties properties;

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }

    @Test
    public void constructor (){
        assertNotNull(new GroupSaveResults(null));

        String json = propertyValue("ws.group.save.results.description.updated");
        WsGroupSaveResults wsGroupSaveResults = JsonUtil.asObject(json, WsGroupSaveResults.class);
        GroupSaveResults groupSaveResults = new GroupSaveResults(wsGroupSaveResults);
        assertNotNull(groupSaveResults);
    }

    @Test
    public void descriptionUpdated() {
        String json = propertyValue("ws.group.save.results.description.updated");
        WsGroupSaveResults wsGroupSaveResults = JsonUtil.asObject(json, WsGroupSaveResults.class);
        GroupSaveResults groupSaveResults = new GroupSaveResults(wsGroupSaveResults);
        assertNotNull(groupSaveResults);
        assertEquals("SUCCESS_UPDATED", groupSaveResults.getResultCode());
    }

    @Test
    public void descriptionNotUpdated() {
        String json = propertyValue("ws.group.save.results.description.not.updated");
        WsGroupSaveResults wsGroupSaveResults = JsonUtil.asObject(json, WsGroupSaveResults.class);
        GroupSaveResults groupSaveResults = new GroupSaveResults(wsGroupSaveResults);
        assertNotNull(groupSaveResults);
        assertEquals("SUCCESS_NO_CHANGES_NEEDED", groupSaveResults.getResultCode());
    }

    @Test
    public void emptyGroup() {
        String json = propertyValue("ws.group.save.results.description.empty.results");
        WsGroupSaveResults wsGroupSaveResults = JsonUtil.asObject(json, WsGroupSaveResults.class);
        GroupSaveResults groupSaveResults = new GroupSaveResults(wsGroupSaveResults);
        assertNotNull(groupSaveResults);
        assertEquals("FAILURE", groupSaveResults.getResultCode());
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
