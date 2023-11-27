package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.UpdatedTimestampResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;

public class GroupingsTimestampResultsTest {
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
        UpdatedTimestampResults updatedTimestampResult = new UpdatedTimestampResults(wsAssignAttributesResults);
        GroupingTimestampResults groupingTimestampResults = new GroupingTimestampResults(updatedTimestampResult);
        assertNotNull(groupingTimestampResults);

        assertEquals("updateTimestampResult should not be null",
                assertThrows(NullPointerException.class, () -> new GroupingTimestampResults(null)).getMessage());
    }

    @Test
    public void timeWasUpdated() {
        String json = propertyValue("ws.assign.attributes.results.time.changed");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        UpdatedTimestampResults updatedTimestampResult = new UpdatedTimestampResults(wsAssignAttributesResults);

        GroupingTimestampResults groupingTimestampResults = new GroupingTimestampResults(updatedTimestampResult);
        assertEquals("SUCCESS", groupingTimestampResults.getResultCode());
        for (Boolean isUpdated : groupingTimestampResults.isTimeUpdatedList()) {
            assertTrue(isUpdated);
        }
        for (String previousUpdatedTime : groupingTimestampResults.getPreviousUpdatedTimeList()) {
            assertEquals(PREVIOUS_UPDATED_TIME, previousUpdatedTime);
        }
        for (String currentUpdatedTime : groupingTimestampResults.getCurrentUpdatedTimeList()) {
            assertEquals(CURRENT_UPDATED_TIME, currentUpdatedTime);
        }
        for (String resultMessage : groupingTimestampResults.getResultMessages()) {
            assertEquals("Timestamp was updated from 19700101T0000 to 19700101T0001.", resultMessage);
        }
        for (String groupPath : groupingTimestampResults.getGroupPaths()) {
            assertEquals("group-path", groupPath);
        }

    }

    @Test
    public void timeWasNotUpdated() {
        String json = propertyValue("ws.assign.attributes.results.time.not.changed");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        UpdatedTimestampResults updatedTimestampResult = new UpdatedTimestampResults(wsAssignAttributesResults);

        GroupingTimestampResults groupingTimestampResults = new GroupingTimestampResults(updatedTimestampResult);
        assertEquals("SUCCESS", groupingTimestampResults.getResultCode());
        for (Boolean isUpdated : groupingTimestampResults.isTimeUpdatedList()) {
            assertFalse(isUpdated);
        }
        for (String previousUpdatedTime : groupingTimestampResults.getPreviousUpdatedTimeList()) {
            assertEquals(PREVIOUS_UPDATED_TIME, previousUpdatedTime);
        }
        for (String currentUpdatedTime : groupingTimestampResults.getCurrentUpdatedTimeList()) {
            assertEquals(PREVIOUS_UPDATED_TIME, currentUpdatedTime);
        }
        for (String resultMessage : groupingTimestampResults.getResultMessages()) {
            assertEquals("Timestamp of 19700101T0000 was not updated.", resultMessage);
        }
        for (String groupPath : groupingTimestampResults.getGroupPaths()) {
            assertEquals("group-path", groupPath);
        }
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
