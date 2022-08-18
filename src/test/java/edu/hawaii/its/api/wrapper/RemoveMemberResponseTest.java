package edu.hawaii.its.api.wrapper;

import static edu.hawaii.its.api.service.ResponseCode.SUCCESS;
import static edu.hawaii.its.api.service.ResponseCode.SUCCESS_WASNT_IMMEDIATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

public class RemoveMemberResponseTest {

    private static final Path RESOURCES_HOME_PATH = Paths.get("src", "test", "resources");
    private static final String RESOURCES_HOME = RESOURCES_HOME_PATH.toString();

    private PropertyLocator propertyLocator;

    @BeforeEach
    public void beforeEach() throws Exception {
        propertyLocator = new PropertyLocator(RESOURCES_HOME, "grouper.test.properties");
    }

    @Test
    public void construction() {
        String json = propertyLocator.find("ws.delete.member.results.success.uid");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMemberResponse removeMemberResponse = new RemoveMemberResponse(wsDeleteMemberResults);
        assertNotNull(removeMemberResponse);
    }

    @Test
    public void accessors() {
        // When remove is queried with a uid.
        String json = propertyLocator.find("ws.delete.member.results.success.uid");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMemberResponse removeMemberResponse = new RemoveMemberResponse(wsDeleteMemberResults);

        assertNotNull(removeMemberResponse.resultCode());
        assertEquals(SUCCESS, removeMemberResponse.resultCode());
        assertTrue(removeMemberResponse.isSuccess());

        assertNotNull(removeMemberResponse.groupPath());
        assertEquals("group-path", removeMemberResponse.groupPath());

        assertNotNull(removeMemberResponse.uid());
        assertEquals("uid", removeMemberResponse.uid());

        assertNotNull(removeMemberResponse.uhUuid());
        assertEquals("uhuuid", removeMemberResponse.uhUuid());

        assertNotNull(removeMemberResponse.name());
        assertEquals("name", removeMemberResponse.name());

        // When remove is queried with uhUuid.
        json = propertyLocator.find("ws.delete.member.results.success.uhuuid");
        wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        removeMemberResponse = new RemoveMemberResponse(wsDeleteMemberResults);

        assertNotNull(removeMemberResponse.resultCode());
        assertEquals(SUCCESS, removeMemberResponse.resultCode());
        assertTrue(removeMemberResponse.isSuccess());

        assertNotNull(removeMemberResponse.groupPath());
        assertEquals("group-path", removeMemberResponse.groupPath());

        assertNotNull(removeMemberResponse.uid());
        assertEquals("uid", removeMemberResponse.uid());

        assertNotNull(removeMemberResponse.uhUuid());
        assertEquals("uhuuid", removeMemberResponse.uhUuid());

        assertNotNull(removeMemberResponse.name());
        assertEquals("name", removeMemberResponse.name());

        // When grouper result contains null fields.
        json = propertyLocator.find("ws.delete.member.results.null.values");
        wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        removeMemberResponse = new RemoveMemberResponse(wsDeleteMemberResults);

        assertNotNull(removeMemberResponse.resultCode());
        assertEquals(SUCCESS_WASNT_IMMEDIATE, removeMemberResponse.resultCode());
        assertFalse(removeMemberResponse.isSuccess());

        assertNotNull(removeMemberResponse.groupPath());
        assertEquals("", removeMemberResponse.groupPath());

        assertNotNull(removeMemberResponse.uid());
        assertEquals("", removeMemberResponse.uid());

        assertNotNull(removeMemberResponse.uhUuid());
        assertEquals("", removeMemberResponse.uhUuid());

        assertNotNull(removeMemberResponse.name());
        assertEquals("", removeMemberResponse.name());
    }
}
