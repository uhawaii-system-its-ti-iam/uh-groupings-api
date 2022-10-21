package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AddResultTest extends FetchesProperties {
    @Test
    public void construction() {
        String json = propertyValue("ws.add.member.result.success");
        WsAddMemberResult wsAddMemberResult = JsonUtil.asObject(json, WsAddMemberResult.class);
        AddResult addResult = new AddResult(wsAddMemberResult);
        assertNotNull(addResult);
        addResult = new AddResult(null);
        assertNotNull(addResult);
    }

    @Test
    public void successfulAddResultTest() {
        String json = propertyValue("ws.add.member.result.success");
        WsAddMemberResult wsAddMemberResult = JsonUtil.asObject(json, WsAddMemberResult .class);
        AddResult addResult = new AddResult(wsAddMemberResult);
        assertNotNull(addResult);
        assertEquals("SUCCESS", addResult.getResultCode());
        assertEquals("uid", addResult.getUid());
        assertEquals("uhUuid", addResult.getUhUuid());
        assertEquals("name", addResult.getName());
    }

    @Test
    public void memberAlreadyExistedResultTest() {
        String json = propertyValue("ws.add.member.result.already.existed");
        WsAddMemberResult wsAddMemberResult = JsonUtil.asObject(json, WsAddMemberResult .class);
        AddResult addResult = new AddResult(wsAddMemberResult);
        assertNotNull(addResult);
        assertEquals("SUCCESS_ALREADY_EXISTED", addResult.getResultCode());
        assertEquals("uid", addResult.getUid());
        assertEquals("uhUuid", addResult.getUhUuid());
        assertEquals("name", addResult.getName());
    }
}
