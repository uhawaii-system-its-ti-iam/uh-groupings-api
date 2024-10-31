package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GroupingUpdateSyncDestResultTest {
    private static Properties properties;
    private GroupingUpdateSyncDestResult syncDestResult;

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }

    @Test
    public void testConstructor() {
        String json = propertyValue("ws.assign.attributes.results.null.assign.attribute.result");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        assertNotNull(wsAssignAttributesResults, "WsAssignAttributesResults should not be null after JSON parsing");
        AssignAttributesResults assignAttributesResults = new AssignAttributesResults(wsAssignAttributesResults);
        GroupingUpdatedAttributeResult groupingUpdatedAttributeResult = new GroupingUpdatedAttributeResult(assignAttributesResults);
        syncDestResult = new GroupingUpdateSyncDestResult(groupingUpdatedAttributeResult);
        assertNotNull(syncDestResult, "GroupingUpdateSyncDestResult should not be null");
    }

    @Test
    public void testDefaultConstructor() {
        syncDestResult = new GroupingUpdateSyncDestResult();
        assertNotNull(syncDestResult, "GroupingUpdateSyncDestResult should not be null");
        assertNotNull(syncDestResult.getGroupPath(), "Group path should not be null");
    }

    @Test
    public void accessors() {
        if (syncDestResult == null) {
            testConstructor();
        }
        assertNotNull(syncDestResult.getName(), "Name should not be null");
        assertEquals("name", syncDestResult.getName(), "Expected name to be 'name'");
        assertTrue(syncDestResult.getUpdatedStatus(), "Expected Updated Status to be true");
        assertTrue(syncDestResult.getCurrentStatus(), "Expected Current Status to be true");
        assertEquals("SUCCESS", syncDestResult.getResultCode(), "Expected Result Code to be 'SUCCESS'");
        assertEquals("group-path", syncDestResult.getGroupPath(), "Expected group path to match 'group-path'");
    }
}

