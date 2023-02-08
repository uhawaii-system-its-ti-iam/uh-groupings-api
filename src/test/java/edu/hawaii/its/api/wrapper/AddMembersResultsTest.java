package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AddMembersResultsTest {

    private static Properties properties;
    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }

    @Test
    public void construction() {
        String json = propertyValue("ws.add.member.results.success");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMembersResults addMembersResults = new AddMembersResults(wsAddMemberResults);
        assertNotNull(addMembersResults);
        addMembersResults = new AddMembersResults(null);
        assertNotNull(addMembersResults);
    }

    @Test
    public void addSuccessResultsTest() {
        String json = propertyValue("ws.add.member.results.success");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMembersResults addMembersResults = new AddMembersResults(wsAddMemberResults);
        assertNotNull(addMembersResults);
        assertEquals("SUCCESS", addMembersResults.getResultCode());
        assertEquals("group-path", addMembersResults.getGroupPath());
        assertNotNull(addMembersResults.getResults());
    }

    @Test
    public void failedResultsTest() {
        String json = propertyValue("ws.add.member.results.failure");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMembersResults addMembersResults = new AddMembersResults(wsAddMemberResults);
        assertNotNull(addMembersResults);
        assertEquals("FAILURE", addMembersResults.getResultCode());
        assertEquals("group-path", addMembersResults.getGroupPath());
        assertNotNull(addMembersResults.getResults());
    }

    @Test
    public void emptyResultsTest() {
        String json = propertyValue("ws.add.member.results.empty");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMembersResults addMembersResults = new AddMembersResults(wsAddMemberResults);
        assertNotNull(addMembersResults);
        assertEquals("FAILURE", addMembersResults.getResultCode());
        assertEquals("group-path", addMembersResults.getGroupPath());
        assertEquals(0, addMembersResults.getResults().size());
    }

    @Test
    public void nullWsGroupResultsTest() {
        String json = propertyValue("ws.add.member.results.null.ws.group");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMembersResults addMembersResults = new AddMembersResults(wsAddMemberResults);
        assertNotNull(addMembersResults);
        assertEquals("FAILURE", addMembersResults.getResultCode());
        assertEquals("", addMembersResults.getGroupPath());
    }

    @Test
    public void nullGroupPathResultsTest() {
        String json = propertyValue("ws.add.member.results.null.group.path");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMembersResults addMembersResults = new AddMembersResults(wsAddMemberResults);
        assertNotNull(addMembersResults);
        assertEquals("FAILURE", addMembersResults.getResultCode());
        assertEquals("", addMembersResults.getGroupPath());
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
