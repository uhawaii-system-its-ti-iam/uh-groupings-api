package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RemoveMembersResultsTest {

    private static Properties properties;
    final static private String SUCCESS = "SUCCESS";
    final static private String SUBJECT_NOT_FOUND = "SUBJECT_NOT_FOUND";

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }

    @Test
    public void construction() {
        String json = propertyValue("ws.delete.member.results.failure.attributes");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        assertNotNull(removeMembersResults);
        removeMembersResults = new RemoveMembersResults(null);
        assertNotNull(removeMembersResults);
    }

    @Test
    public void successfulResultsTest() {
        String json = propertyValue("ws.delete.member.results.success.attributes");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        assertNotNull(removeMembersResults);
        assertEquals(SUCCESS, removeMembersResults.getResultCode());
        assertEquals("group-path", removeMembersResults.getGroupPath());
        assertNotNull(removeMembersResults.getResults());
    }

    @Test
    public void failedResultsTest() {
        String json = propertyValue("ws.delete.member.results.failure.attributes");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        assertNotNull(removeMembersResults);
        assertEquals("FAILURE", removeMembersResults.getResultCode());
        assertEquals("group-path", removeMembersResults.getGroupPath());
        assertNotNull(removeMembersResults.getResults());
    }

    @Test
    public void emptyResultsTest() {
        String json = propertyValue("ws.delete.member.results.empty");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        assertEquals("FAILURE", removeMembersResults.getResultCode());
        assertEquals("group-path", removeMembersResults.getGroupPath());
        assertEquals(0, removeMembersResults.getResults().size());
    }

    @Test
    public void nullWsGroupResultsTest() {
        String json = propertyValue("ws.delete.member.results.null.ws.group");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        assertEquals("FAILURE", removeMembersResults.getResultCode());
        assertEquals("", removeMembersResults.getGroupPath());
    }

    @Test
    public void nullGroupPathResultsTest() {
        String json = propertyValue("ws.delete.member.results.null.group.path");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        assertEquals("FAILURE", removeMembersResults.getResultCode());
        assertEquals("", removeMembersResults.getGroupPath());
    }


    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
