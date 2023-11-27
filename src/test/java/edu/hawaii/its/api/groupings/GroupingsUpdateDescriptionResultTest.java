package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.GroupSaveResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;

public class GroupingsUpdateDescriptionResultTest {
    private static Properties properties;

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }

    @Test
    public void test() {
        String updatedDescription = "updatedDescription";
        String json = propertyValue("ws.group.save.results.description.updated");
        WsGroupSaveResults wsGroupSaveResults = JsonUtil.asObject(json, WsGroupSaveResults.class);
        GroupSaveResults groupSaveResults = new GroupSaveResults(wsGroupSaveResults);
        GroupingUpdateDescriptionResult result =
                new GroupingUpdateDescriptionResult(groupSaveResults, updatedDescription);
        assertNotNull(result);
        assertEquals(updatedDescription, result.getUpdatedDescription());
        assertEquals("description", result.getCurrentDescription());
        assertEquals("group-path", result.getGroupPath());
        assertEquals("SUCCESS_UPDATED", result.getResultCode());

        json = propertyValue("ws.group.save.results.description.not.updated");
        wsGroupSaveResults = JsonUtil.asObject(json, WsGroupSaveResults.class);
        groupSaveResults = new GroupSaveResults(wsGroupSaveResults);
        result = new GroupingUpdateDescriptionResult(groupSaveResults, updatedDescription);
        assertNotNull(result);
        assertEquals(updatedDescription, result.getUpdatedDescription());
        assertEquals("description", result.getCurrentDescription());
        assertEquals("group-path", result.getGroupPath());
        assertEquals("SUCCESS_NO_CHANGES_NEEDED", result.getResultCode());

        result = new GroupingUpdateDescriptionResult();
        assertNotNull(result);
        assertEquals("", result.getUpdatedDescription());
        assertEquals("", result.getCurrentDescription());
        assertEquals("", result.getGroupPath());
        assertEquals("FAILURE", result.getResultCode());
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
