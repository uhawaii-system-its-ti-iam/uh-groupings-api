package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RemoveMembersResultsTest extends FetchesProperties {

    @Test
    public void construction() {
        String json = propertyValue("ws.delete.member.results.failure");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        assertNotNull(removeMembersResults);
        removeMembersResults = new RemoveMembersResults(null);
        assertNotNull(removeMembersResults);
    }

    @Test
    public void successfulResultsTest() {
        String json = propertyValue("ws.delete.member.results.success");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        assertNotNull(removeMembersResults);
        assertEquals("SUCCESS", removeMembersResults.getResultCode());
        assertEquals("group-path", removeMembersResults.getGroupPath());
        assertNotNull(removeMembersResults.getResults());
    }

    @Test
    public void failedResultsTest() {
        String json = propertyValue("ws.delete.member.results.failure");
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
        assertNotNull(removeMembersResults);
        assertEquals("FAILURE", removeMembersResults.getResultCode());
        assertEquals("group-path", removeMembersResults.getGroupPath());
        assertEquals(0, removeMembersResults.getResults().size());
    }

    @Test
    public void nullWsGroupResultsTest() {
        String json = propertyValue("ws.delete.member.results.null.ws.group");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        assertNotNull(removeMembersResults);
        assertEquals("FAILURE", removeMembersResults.getResultCode());
        assertEquals("", removeMembersResults.getGroupPath());
    }

    @Test
    public void nullGroupPathResultsTest() {
        String json = propertyValue("ws.delete.member.results.null.group.path");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        assertNotNull(removeMembersResults);
        assertEquals("FAILURE", removeMembersResults.getResultCode());
        assertEquals("", removeMembersResults.getGroupPath());
    }
}
