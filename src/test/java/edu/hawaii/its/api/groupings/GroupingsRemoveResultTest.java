package edu.hawaii.its.api.groupings;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.FetchesProperties;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GroupingsRemoveResultTest extends FetchesProperties {

    @Test
    public void test() {
        String json = propertyValue("ws.delete.member.results.success.single.result");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMemberResult removeMemberResult = new RemoveMemberResult(wsDeleteMemberResults);
        GroupingsRemoveResult groupingsRemoveResult = new GroupingsRemoveResult(removeMemberResult);
        assertNotNull(groupingsRemoveResult);

        assertEquals("SUCCESS", groupingsRemoveResult.resultCode);
        assertEquals("uid", groupingsRemoveResult.uid);
        assertEquals("uhUuid", groupingsRemoveResult.getUhUuid());
        assertEquals("name", groupingsRemoveResult.getName());
    }
}
