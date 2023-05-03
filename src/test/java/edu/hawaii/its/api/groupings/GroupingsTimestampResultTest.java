package edu.hawaii.its.api.groupings;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.UpdatedTimestampResult;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GroupingsTimestampResultTest {
    private static Properties properties;

    String PREVIOUS_UPDATED_TIME = "19700101T0000";
    String CURRENT_UPDATED_TIME = "19700101T0001";

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }

    @Test
    public void constructor() {
        String json = propertyValue("ws.assign.attributes.results.time.changed");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        UpdatedTimestampResult updatedTimestampResult = new UpdatedTimestampResult(wsAssignAttributesResults);
        GroupingTimestampResult groupingsTimestampResult = new GroupingTimestampResult(updatedTimestampResult);
        assertNotNull(groupingsTimestampResult);

        assertEquals("updateTimestampResult should not be null",
                assertThrows(NullPointerException.class, () -> new GroupingTimestampResult(null)).getMessage());
    }

    @Test
    public void timeWasUpdated() {
        String json = propertyValue("ws.assign.attributes.results.time.changed");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        UpdatedTimestampResult updatedTimestampResult = new UpdatedTimestampResult(wsAssignAttributesResults);

        GroupingTimestampResult groupingsTimestampResult = new GroupingTimestampResult(updatedTimestampResult);
        assertTrue(groupingsTimestampResult.isTimeUpdated());
        assertEquals("SUCCESS", groupingsTimestampResult.getResultCode());
        assertEquals(PREVIOUS_UPDATED_TIME, groupingsTimestampResult.getPreviousUpdatedTime());
        assertEquals(CURRENT_UPDATED_TIME, groupingsTimestampResult.getCurrentUpdatedTime());
        assertEquals("Timestamp was updated from 19700101T0000 to 19700101T0001.",
                groupingsTimestampResult.getResultMessage());
        assertEquals("group-path", groupingsTimestampResult.getGroupPath());
    }

    @Test
    public void timeWasNotUpdated() {
        String json = propertyValue("ws.assign.attributes.results.time.not.changed");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        UpdatedTimestampResult updatedTimestampResult = new UpdatedTimestampResult(wsAssignAttributesResults);

        GroupingTimestampResult groupingsTimestampResult = new GroupingTimestampResult(updatedTimestampResult);
        assertFalse(groupingsTimestampResult.isTimeUpdated());
        assertEquals("SUCCESS", groupingsTimestampResult.getResultCode());
        assertEquals(PREVIOUS_UPDATED_TIME, groupingsTimestampResult.getPreviousUpdatedTime());
        assertEquals(PREVIOUS_UPDATED_TIME, groupingsTimestampResult.getCurrentUpdatedTime());
        assertEquals("Timestamp of 19700101T0000 was not updated.",
                groupingsTimestampResult.getResultMessage());
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
