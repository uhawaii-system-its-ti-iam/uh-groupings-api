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
import static org.junit.jupiter.api.Assertions.assertFalse;

public class GroupingUpdatedAttributeResultTest {
    private static Properties properties;
    private GroupingUpdatedAttributeResult groupingUpdatedAttributeResult;

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
        WsAssignAttributesResults wsAssignAttributesResults =
                JsonUtil.asObject(json, WsAssignAttributesResults.class);
        assertNotNull(wsAssignAttributesResults, "WsAssignAttributesResults should not be null after JSON parsing");
        AssignAttributesResults assignAttributesResults = new AssignAttributesResults(wsAssignAttributesResults);
        groupingUpdatedAttributeResult = new GroupingUpdatedAttributeResult(assignAttributesResults);
    }

    @Test
    public void testDefaultConstructor() {
        groupingUpdatedAttributeResult = new GroupingUpdatedAttributeResult();
        assertNotNull(groupingUpdatedAttributeResult, "GroupingUpdatedAttributeResult should not be null");
        assertNotNull(groupingUpdatedAttributeResult.getGroupPath(), "Group path should not be null");
    }

    @Test
    public void testSetUpdatedStatusDeletedTrue() {
        String json = propertyValue("ws.assign.attributes.results.deleted.true");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        assertNotNull(wsAssignAttributesResults, "WsAssignAttributesResults should not be null after JSON parsing");
        AssignAttributesResults assignAttributesResults = new AssignAttributesResults(wsAssignAttributesResults);
        groupingUpdatedAttributeResult = new GroupingUpdatedAttributeResult(assignAttributesResults);
        assertFalse(groupingUpdatedAttributeResult.getUpdatedStatus(), "Expected updated status to be false when deleted is true");
    }

    @Test
    public void testSetUpdatedStatusDeletedFalse() {
        String json = propertyValue("ws.assign.attributes.results.deleted.false");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        assertNotNull(wsAssignAttributesResults, "WsAssignAttributesResults should not be null after JSON parsing");
        AssignAttributesResults assignAttributesResults = new AssignAttributesResults(wsAssignAttributesResults);
        groupingUpdatedAttributeResult = new GroupingUpdatedAttributeResult(assignAttributesResults);
        assertTrue(groupingUpdatedAttributeResult.getUpdatedStatus(), "Expected updated status to be true when deleted is false");
    }

    @Test
    public void testSetCurrentStatusChangedTrue() {
        String json = propertyValue("ws.assign.attributes.results.changed.true");
        WsAssignAttributesResults wsAssignAttributesResults =
                JsonUtil.asObject(json, WsAssignAttributesResults.class);
        assertNotNull(wsAssignAttributesResults, "WsAssignAttributesResults should not be null after JSON parsing");
        AssignAttributesResults assignAttributesResults = new AssignAttributesResults(wsAssignAttributesResults);
        groupingUpdatedAttributeResult = new GroupingUpdatedAttributeResult(assignAttributesResults);
        assertFalse(groupingUpdatedAttributeResult.getCurrentStatus(), "Expected current status to be true");
    }
    @Test
    public void testSetCurrentStatusChangedFalse() {
        String json = propertyValue("ws.assign.attributes.results.changed.false");
        WsAssignAttributesResults wsAssignAttributesResults =
                JsonUtil.asObject(json, WsAssignAttributesResults.class);
        assertNotNull(wsAssignAttributesResults, "WsAssignAttributesResults should not be null after JSON parsing");
        AssignAttributesResults assignAttributesResults = new AssignAttributesResults(wsAssignAttributesResults);
        groupingUpdatedAttributeResult = new GroupingUpdatedAttributeResult(assignAttributesResults);
        assertTrue(groupingUpdatedAttributeResult.getCurrentStatus(), "Expected current status to be false");
    }

    @Test
    public void accessors() {
        if (groupingUpdatedAttributeResult == null) {
            testConstructor();
        }
        assertNotNull(groupingUpdatedAttributeResult.getName(), "Name should not be null");
        assertEquals("name", groupingUpdatedAttributeResult.getName(), "Expected name to match");
        assertTrue(groupingUpdatedAttributeResult.getUpdatedStatus(), "Expected updated status to be true");
        assertTrue(groupingUpdatedAttributeResult.getCurrentStatus(), "Expected current status to be false");
        assertEquals("SUCCESS", groupingUpdatedAttributeResult.getResultCode(), "Expected result code to be 'SUCCESS'");
        assertNotNull(groupingUpdatedAttributeResult.getGroupPath(), "Group path should not be null");
        assertEquals("group-path", groupingUpdatedAttributeResult.getGroupPath(), "Expected group path to match 'group-path'");
    }
}



