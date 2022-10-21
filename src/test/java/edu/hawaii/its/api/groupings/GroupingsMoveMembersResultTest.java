package edu.hawaii.its.api.groupings;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.FetchesProperties;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GroupingsMoveMembersResultTest extends FetchesProperties {
    @Test
    public void test() {
        String json = propertyValue("ws.add.member.results.success");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMembersResults addMembersResults = new AddMembersResults(wsAddMemberResults);
        assertNotNull(addMembersResults);

        json = propertyValue("ws.delete.member.results.success");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        assertNotNull(removeMembersResults);

        GroupingsMoveMembersResult groupingsMoveMembersResult =
                new GroupingsMoveMembersResult(addMembersResults, removeMembersResults);
        assertNotNull(groupingsMoveMembersResult);
    }
}
