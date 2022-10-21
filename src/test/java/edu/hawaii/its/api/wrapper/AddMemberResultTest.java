package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AddMemberResultTest extends FetchesProperties {

    @Test
    public void construction() {
        String json = propertyValue("ws.add.member.results.success.single.result");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMemberResult addMemberResult = new AddMemberResult(wsAddMemberResults);
        assertNotNull(addMemberResult);
        addMemberResult = new AddMemberResult(null);
        assertNotNull(addMemberResult);
    }

    @Test
    public void memberAddSuccessTest() {
        String json = propertyValue("ws.add.member.results.success.single.result");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMemberResult addMemberResult = new AddMemberResult(wsAddMemberResults);
        assertNotNull(addMemberResult);
        assertNotNull(addMemberResult.getResultCode());
        assertNotNull(addMemberResult.getGroupPath());
        assertNotNull(addMemberResult.getUid());
        assertNotNull(addMemberResult.getUhUuid());
        assertNotNull(addMemberResult.getName());
        assertEquals("SUCCESS", addMemberResult.getResultCode());
        assertEquals("group-path", addMemberResult.getGroupPath());
        assertEquals("uid", addMemberResult.getUid());
        assertEquals("uhUuid", addMemberResult.getUhUuid());
        assertEquals("name", addMemberResult.getName());
    }

    @Test
    public void memberAlreadyExistedTest() {
        String json = propertyValue("ws.add.member.results.already.existed.single.result");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMemberResult addMemberResult = new AddMemberResult(wsAddMemberResults);
        assertNotNull(addMemberResult);
        assertNotNull(addMemberResult.getResultCode());
        assertNotNull(addMemberResult.getGroupPath());
        assertNotNull(addMemberResult.getUid());
        assertNotNull(addMemberResult.getUhUuid());
        assertNotNull(addMemberResult.getName());
        assertEquals("SUCCESS_ALREADY_EXISTED", addMemberResult.getResultCode());
        assertEquals("group-path", addMemberResult.getGroupPath());
        assertEquals("uid", addMemberResult.getUid());
        assertEquals("uhUuid", addMemberResult.getUhUuid());
        assertEquals("name", addMemberResult.getName());
    }

}
