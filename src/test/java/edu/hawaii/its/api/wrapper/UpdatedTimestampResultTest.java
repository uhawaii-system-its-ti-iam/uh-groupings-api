package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;

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

public class UpdatedTimestampResultTest {
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
        UpdatedTimestampResult updatedTimestampResult = new UpdatedTimestampResult(wsAssignAttributesResults);
        assertNotNull(updatedTimestampResult);
        assertEquals("SUCCESS", updatedTimestampResult.getResultCode());

        assertNotNull(new UpdatedTimestampResult());

        assertEquals("wsAssignAttributeResults should not be null",
                assertThrows(NullPointerException.class, () -> new UpdatedTimestampResult(null)).getMessage());
    }

    @Test
    public void timeWasUpdated() {
        String json = propertyValue("ws.assign.attributes.results.time.changed");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        UpdatedTimestampResult updatedTimestampResult = new UpdatedTimestampResult(wsAssignAttributesResults);
        assertNotNull(updatedTimestampResult);
        assertEquals("SUCCESS", updatedTimestampResult.getResultCode());

        assertTrue(updatedTimestampResult.isTimeUpdated());

        AttributeAssignValueResult previousTimestampResult = updatedTimestampResult.getPreviousTimestampResult();
        assertEquals("19700101T0000", previousTimestampResult.getValue());
        assertTrue(previousTimestampResult.isValueRemoved());
        assertTrue(previousTimestampResult.isValueChanged());

        AttributeAssignValueResult currentTimestampResult = updatedTimestampResult.getCurrentTimestampResult();
        assertEquals("19700101T0001", currentTimestampResult.getValue());
        assertFalse(currentTimestampResult.isValueRemoved());
        assertTrue(currentTimestampResult.isValueChanged());

    }

    @Test
    public void timeWasNotUpdated() {
        String json = propertyValue("ws.assign.attributes.results.time.not.changed");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        UpdatedTimestampResult updatedTimestampResult = new UpdatedTimestampResult(wsAssignAttributesResults);
        assertNotNull(updatedTimestampResult);
        assertEquals("SUCCESS", updatedTimestampResult.getResultCode());

        assertFalse(updatedTimestampResult.isTimeUpdated());

        // Previous timestamp
        AttributeAssignValueResult previousTimestampResult = updatedTimestampResult.getPreviousTimestampResult();
        assertEquals("19700101T0000", previousTimestampResult.getValue());
        assertFalse(previousTimestampResult.isValueRemoved());
        assertFalse(previousTimestampResult.isValueChanged());
        // If the timestamp was not updated, result should be identical.
        // Current timestamp
        AttributeAssignValueResult currentTimestampResult = updatedTimestampResult.getCurrentTimestampResult();
        assertEquals("19700101T0000", currentTimestampResult.getValue());
        assertFalse(previousTimestampResult.isValueRemoved());
        assertFalse(previousTimestampResult.isValueChanged());
    }

    @Test
    public void emptyGroups() {
        String json = propertyValue("ws.assign.attributes.results.time.empty.groups.empty.results");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        UpdatedTimestampResult updatedTimestampResult = new UpdatedTimestampResult(wsAssignAttributesResults);
        assertEquals("FAILURE", updatedTimestampResult.getGroup().getResultCode());
        assertFalse(updatedTimestampResult.getGroup().isValidPath());
        assertEquals("", updatedTimestampResult.getGroup().getGroupPath());
        assertEquals("", updatedTimestampResult.getGroup().getExtension());
    }

    @Test
    public void emptyResults() {
        String json = propertyValue("ws.assign.attributes.results.time.empty.groups.empty.results");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        UpdatedTimestampResult updatedTimestampResult = new UpdatedTimestampResult(wsAssignAttributesResults);
        AttributeAssignValueResult result = updatedTimestampResult.getPreviousTimestampResult();
        assertNotNull(result);
        assertFalse(result.isValueChanged());
        assertFalse(result.isValueRemoved());
        assertEquals("", result.getValue());
        result = updatedTimestampResult.getCurrentTimestampResult();
        assertNotNull(result);
        assertFalse(result.isValueChanged());
        assertFalse(result.isValueRemoved());
        assertEquals("", result.getValue());
    }

    @Test
    public void emptyValues() {
        String json = propertyValue("ws.assign.attributes.results.time.empty.values");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        UpdatedTimestampResult updatedTimestampResult = new UpdatedTimestampResult(wsAssignAttributesResults);
        AttributeAssignValueResult result = updatedTimestampResult.getPreviousTimestampResult();
        assertNotNull(result);
        assertFalse(result.isValueChanged());
        assertFalse(result.isValueRemoved());
        assertEquals("", result.getValue());
        result = updatedTimestampResult.getCurrentTimestampResult();
        assertNotNull(result);
        assertFalse(result.isValueChanged());
        assertFalse(result.isValueRemoved());
        assertEquals("", result.getValue());
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
