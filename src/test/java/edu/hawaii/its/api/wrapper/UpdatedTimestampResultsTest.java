package edu.hawaii.its.api.wrapper;

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

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;

public class UpdatedTimestampResultsTest {
    private static Properties properties;

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
        UpdatedTimestampResults updatedTimestampResults = new UpdatedTimestampResults(wsAssignAttributesResults);
        assertNotNull(updatedTimestampResults);
        assertEquals("SUCCESS", updatedTimestampResults.getResultCode());

        assertNotNull(new UpdatedTimestampResults());

        assertEquals("wsAssignAttributeResults should not be null",
                assertThrows(NullPointerException.class, () -> new UpdatedTimestampResults(null)).getMessage());
    }

    @Test
    public void timeWasUpdated() {
        String json = propertyValue("ws.assign.attributes.results.time.changed");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        UpdatedTimestampResults updatedTimestampResults = new UpdatedTimestampResults(wsAssignAttributesResults);
        assertNotNull(updatedTimestampResults);
        assertEquals("SUCCESS", updatedTimestampResults.getResultCode());

        for (Boolean isUpdated : updatedTimestampResults.isTimeUpdatedList()) {
            assertTrue(isUpdated);
        }

        for (AttributeAssignValueResult previousTimestampResult : updatedTimestampResults.getPreviousTimestampResults()) {
            assertEquals("19700101T0000", previousTimestampResult.getValue());
            assertTrue(previousTimestampResult.isValueRemoved());
            assertTrue(previousTimestampResult.isValueChanged());
        }

        for (AttributeAssignValueResult currentTimestampResult : updatedTimestampResults.getCurrentTimestampResults()) {
            assertEquals("19700101T0001", currentTimestampResult.getValue());
            assertFalse(currentTimestampResult.isValueRemoved());
            assertTrue(currentTimestampResult.isValueChanged());
        }
    }

    @Test
    public void multipleTimeWasUpdated() {
        String json = propertyValue("ws.assign.attributes.results.multiple.time.changed");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        UpdatedTimestampResults updatedTimestampResults = new UpdatedTimestampResults(wsAssignAttributesResults);

        assertEquals("SUCCESS", updatedTimestampResults.getResultCode());
        assertEquals(2, updatedTimestampResults.getCurrentTimestampResults().size());
        assertEquals(2, updatedTimestampResults.getPreviousTimestampResults().size());
        assertEquals(2, updatedTimestampResults.getGroups().size());

        for (AttributeAssignValueResult previousTimestampResult : updatedTimestampResults.getPreviousTimestampResults()) {
            assertEquals("20230719T1725", previousTimestampResult.getValue());
            assertTrue(previousTimestampResult.isValueRemoved());
            assertTrue(previousTimestampResult.isValueChanged());
        }

        for (AttributeAssignValueResult currentTimestampResult : updatedTimestampResults.getCurrentTimestampResults()) {
            assertEquals("20230719T1810", currentTimestampResult.getValue());
            assertFalse(currentTimestampResult.isValueRemoved());
            assertTrue(currentTimestampResult.isValueChanged());
        }
    }

    @Test
    public void timeWasNotUpdated() {
        String json = propertyValue("ws.assign.attributes.results.multiple.time.not.changed");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        UpdatedTimestampResults updatedTimestampResults = new UpdatedTimestampResults(wsAssignAttributesResults);
        assertNotNull(updatedTimestampResults);
        assertEquals("SUCCESS", updatedTimestampResults.getResultCode());

        for (Boolean isTimeUpdated : updatedTimestampResults.isTimeUpdatedList()) {
            assertFalse(isTimeUpdated);
        }

        for (AttributeAssignValueResult previousTimestampResult : updatedTimestampResults.getPreviousTimestampResults()) {
            assertEquals("19700101T0000", previousTimestampResult.getValue());
            assertFalse(previousTimestampResult.isValueRemoved());
            assertFalse(previousTimestampResult.isValueChanged());
        }

        for (AttributeAssignValueResult currentTimestampResult : updatedTimestampResults.getCurrentTimestampResults()) {
            assertEquals("19700101T0000", currentTimestampResult.getValue());
            assertFalse(currentTimestampResult.isValueRemoved());
            assertFalse(currentTimestampResult.isValueChanged());
        }
    }

    @Test
    public void emptyGroups() {
        String json = propertyValue("ws.assign.attributes.results.time.empty.groups.empty.results");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        UpdatedTimestampResults updatedTimestampResults = new UpdatedTimestampResults(wsAssignAttributesResults);
        for (Group group : updatedTimestampResults.getGroups()) {
            assertEquals("FAILURE", group.getResultCode());
            assertFalse(group.isValidPath());
            assertEquals("", group.getGroupPath());
            assertEquals("", group.getExtension());
        }
    }

    @Test
    public void emptyResults() {
        String json = propertyValue("ws.assign.attributes.results.time.empty.groups.empty.results");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        UpdatedTimestampResults updatedTimestampResults = new UpdatedTimestampResults(wsAssignAttributesResults);
        for (AttributeAssignValueResult previousTimestampResult : updatedTimestampResults.getPreviousTimestampResults()) {
            assertNotNull(previousTimestampResult);
            assertFalse(previousTimestampResult.isValueChanged());
            assertFalse(previousTimestampResult.isValueRemoved());
            assertEquals("", previousTimestampResult.getValue());
        }

        for (AttributeAssignValueResult currentTimestampResult : updatedTimestampResults.getCurrentTimestampResults()) {
            assertNotNull(currentTimestampResult);
            assertFalse(currentTimestampResult.isValueChanged());
            assertFalse(currentTimestampResult.isValueRemoved());
            assertEquals("", currentTimestampResult.getValue());
        }
    }

    @Test
    public void emptyValues() {
        String json = propertyValue("ws.assign.attributes.results.time.empty.values");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        UpdatedTimestampResults updatedTimestampResults = new UpdatedTimestampResults(wsAssignAttributesResults);

        for (AttributeAssignValueResult previousTimestampResult : updatedTimestampResults.getPreviousTimestampResults()) {
            assertNotNull(previousTimestampResult);
            assertFalse(previousTimestampResult.isValueChanged());
            assertFalse(previousTimestampResult.isValueRemoved());
            assertEquals("", previousTimestampResult.getValue());
        }

        for (AttributeAssignValueResult currentTimestampResult : updatedTimestampResults.getCurrentTimestampResults()) {
            assertNotNull(currentTimestampResult);
            assertFalse(currentTimestampResult.isValueChanged());
            assertFalse(currentTimestampResult.isValueRemoved());
            assertEquals("", currentTimestampResult.getValue());
        }
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
