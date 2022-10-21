package edu.hawaii.its.api.groupings;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.FetchesProperties;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GroupingsRemoveResultsTest extends FetchesProperties {
    @Test
    public void test() {
        String json = propertyValue("ws.delete.member.results.success");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        GroupingsRemoveResults groupingsRemoveResults = new GroupingsRemoveResults(removeMembersResults);
        assertNotNull(groupingsRemoveResults);
        List<GroupingsRemoveResult> results = groupingsRemoveResults.getResults();
        assertNotNull(results);
    }
}
