package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public class GroupAttributeResultsTest {
    private static final String TRIO = "uh-settings:attributes:for-groups:uh-grouping:is-trio";
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
    public void constructor() {
        assertNotNull(new GroupAttributeResults(null));
    }

    @Test
    public void successfulResults() {
        String json = propertyValue("ws.get.attribute.assignment.results.success");
        WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults =
                JsonUtil.asObject(json, WsGetAttributeAssignmentsResults.class);
        assertNotNull(wsGetAttributeAssignmentsResults);
        GroupAttributeResults groupAttributeResults = new GroupAttributeResults(wsGetAttributeAssignmentsResults);
        assertNotNull(groupAttributeResults);
        assertEquals(SUCCESS, groupAttributeResults.getResultCode());
        List<GroupAttribute> groupAttributes = groupAttributeResults.getGroupAttributes();
        assertFalse(groupAttributes.isEmpty());
        for (GroupAttribute groupAttribute : groupAttributes) {
            assertEquals(TRIO, groupAttribute.getAttributeName());
            assertEquals(SUCCESS, groupAttribute.getResultCode());
            assertEquals("group-path", groupAttribute.getGroupPath());
        }
    }

    @Test
    public void failedResults() {
        String json = propertyValue("ws.get.attribute.assignment.results.failure");
        WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults =
                JsonUtil.asObject(json, WsGetAttributeAssignmentsResults.class);
        assertNotNull(wsGetAttributeAssignmentsResults);
        GroupAttributeResults groupAttributeResults = new GroupAttributeResults(wsGetAttributeAssignmentsResults);
        assertNotNull(groupAttributeResults);
        assertEquals(FAILURE, groupAttributeResults.getResultCode());
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
