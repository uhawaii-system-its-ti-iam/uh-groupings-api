package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RemoveResultTest extends FetchesProperties {

    @Test
    public void construction() {
        String json = propertyValue("ws.delete.member.result.success");
        WsDeleteMemberResult wsDeleteMemberResult = JsonUtil.asObject(json, WsDeleteMemberResult.class);
        RemoveResult removeResult = new RemoveResult(wsDeleteMemberResult);
        assertNotNull(removeResult);
        removeResult = new RemoveResult(null);
        assertNotNull(removeResult);
    }

    @Test
    public void removeSuccessResultTest() {
        String json = propertyValue("ws.delete.member.result.success");
        WsDeleteMemberResult wsDeleteMemberResult = JsonUtil.asObject(json, WsDeleteMemberResult.class);
        RemoveResult removeResult = new RemoveResult(wsDeleteMemberResult);
        assertNotNull(removeResult);
        assertEquals("SUCCESS", removeResult.getResultCode());
        assertEquals("uid", removeResult.getUid());
        assertEquals("uhUuid", removeResult.getUhUuid());
        assertEquals("name", removeResult.getName());
    }

    @Test
    public void noMemberToRemoveResultTest() {
        String json = propertyValue("ws.delete.member.result.failure");
        WsDeleteMemberResult wsDeleteMemberResult = JsonUtil.asObject(json, WsDeleteMemberResult.class);
        RemoveResult removeResult = new RemoveResult(wsDeleteMemberResult);
        assertNotNull(removeResult);
        assertEquals("SUCCESS_WASNT_IMMEDIATE", removeResult.getResultCode());
        assertEquals("uid", removeResult.getUid());
        assertEquals("uhUuid", removeResult.getUhUuid());
        assertEquals("name", removeResult.getName());
    }

    @Test
    public void invalidIdentifierTest() {
        String json = propertyValue("ws.delete.member.result.invalid.identifier");
        WsDeleteMemberResult wsDeleteMemberResult = JsonUtil.asObject(json, WsDeleteMemberResult.class);
        RemoveResult removeResult = new RemoveResult(wsDeleteMemberResult);
        assertNotNull(removeResult);
        assertEquals("SUCCESS_WASNT_IMMEDIATE", removeResult.getResultCode());
        assertEquals("", removeResult.getUid());
        assertEquals("uhUuid", removeResult.getUhUuid());
        assertEquals("", removeResult.getName());
    }
}
