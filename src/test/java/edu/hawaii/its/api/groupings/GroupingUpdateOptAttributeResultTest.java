package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.hawaii.its.api.wrapper.AssignGrouperPrivilegesResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class GroupingUpdateOptAttributeResultTest {

    private static Properties properties;
    private GroupingUpdateOptAttributeResult groupingUpdateOptAttributeResult;

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
        GroupingUpdatedAttributeResult groupingUpdatedAttributeResult =
                new GroupingUpdatedAttributeResult(assignAttributesResults);
        GroupingPrivilegeResult optInPrivilegeResult = new GroupingPrivilegeResult(new AssignGrouperPrivilegesResult());
        GroupingPrivilegeResult optOutPrivilegeResult = new GroupingPrivilegeResult(new AssignGrouperPrivilegesResult());
        groupingUpdateOptAttributeResult = new GroupingUpdateOptAttributeResult(
                groupingUpdatedAttributeResult, optInPrivilegeResult, optOutPrivilegeResult);
        assertNotNull(groupingUpdateOptAttributeResult, "GroupingUpdateOptAttributeResult should not be null");
        assertEquals(groupingUpdatedAttributeResult.getGroupPath(), groupingUpdateOptAttributeResult.getGroupPath());
    }

    @Test
    public void accessors() {
        if (groupingUpdateOptAttributeResult == null) {
            testConstructor();
        }
        assertNotNull(groupingUpdateOptAttributeResult.getOptInPrivilegeResult(), "OptInPrivilegeResult should not be null");
        assertEquals("name", groupingUpdateOptAttributeResult.getName(), "Expected name to be 'name'");
        assertTrue(groupingUpdateOptAttributeResult.getUpdatedStatus(), "Expected Updated Status to be true");
        assertTrue(groupingUpdateOptAttributeResult.getCurrentStatus(), "Expected Current Status to be true");
        assertEquals("SUCCESS", groupingUpdateOptAttributeResult.getResultCode(), "Expected Result Code to be 'SUCCESS'");
        assertEquals("group-path", groupingUpdateOptAttributeResult.getGroupPath(), "Expected Group Path to be 'group-path'");
    }
}