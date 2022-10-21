package edu.hawaii.its.api.groupings;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.FetchesProperties;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GroupingsMoveMemberResultTest extends FetchesProperties {
    @Test
    public void test() {
        String json = propertyValue("ws.add.member.results.success.single.result");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMemberResult addMemberResult = new AddMemberResult(wsAddMemberResults);
        assertNotNull(addMemberResult);

        json = propertyValue("ws.delete.member.results.success.single.result");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMemberResult removeMemberResult = new RemoveMemberResult(wsDeleteMemberResults);
        assertNotNull(removeMemberResult);

        GroupingsMoveMemberResult groupingsMoveMemberResult =
                new GroupingsMoveMemberResult(addMemberResult, removeMemberResult);
        assertNotNull(groupingsMoveMemberResult);
    }
}
