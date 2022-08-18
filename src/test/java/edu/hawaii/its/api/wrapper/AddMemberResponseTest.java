package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AddMemberResponseTest {

    private static PropertyLocator propertyLocator;
    final static private String SUCCESS = "SUCCESS";
    final static private String SUCCESS_ALREADY_EXISTED = "SUCCESS_ALREADY_EXISTED";

    @BeforeAll
    public static void beforeAll() throws Exception {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void construction() {
        String json = propertyLocator.find("ws.add.member.results.success.uid");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMemberResponse addMemberResponse = new AddMemberResponse(wsAddMemberResults);
        assertNotNull(addMemberResponse);
    }

    @Test
    public void accessors() {
        // When add is queried with a uid.
        String json = propertyLocator.find("ws.add.member.results.success.uid");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMemberResponse addMemberResponse = new AddMemberResponse(wsAddMemberResults);

        assertNotNull(addMemberResponse.resultCode());
        assertEquals(SUCCESS, addMemberResponse.resultCode());

        assertTrue(addMemberResponse.isSuccess());

        assertNotNull(addMemberResponse.groupPath());
        assertEquals("group-path", addMemberResponse.groupPath());

        assertNotNull(addMemberResponse.uid());
        assertEquals("uid", addMemberResponse.uid());

        assertNotNull(addMemberResponse.uhUuid());
        assertEquals("uhuuid", addMemberResponse.uhUuid());

        assertNotNull(addMemberResponse.name());
        assertEquals("name", addMemberResponse.name());

        // When add is queried with a uhuuid.
        json = propertyLocator.find("ws.add.member.results.success.uhuuid");
        wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        addMemberResponse = new AddMemberResponse(wsAddMemberResults);

        assertNotNull(addMemberResponse.resultCode());
        assertEquals(SUCCESS, addMemberResponse.resultCode());
        assertTrue(addMemberResponse.isSuccess());

        assertNotNull(addMemberResponse.groupPath());
        assertEquals("group-path", addMemberResponse.groupPath());

        assertNotNull(addMemberResponse.uid());
        assertEquals("uid", addMemberResponse.uid());

        assertNotNull(addMemberResponse.uhUuid());
        assertEquals("uhuuid", addMemberResponse.uhUuid());

        assertNotNull(addMemberResponse.name());
        assertEquals("name", addMemberResponse.name());

        // When grouper result contains null fields.
        json = propertyLocator.find("ws.add.member.results.null.values");
        wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        addMemberResponse = new AddMemberResponse(wsAddMemberResults);

        assertNotNull(addMemberResponse.resultCode());
        assertEquals(SUCCESS_ALREADY_EXISTED, addMemberResponse.resultCode());
        assertFalse(addMemberResponse.isSuccess());

        assertNotNull(addMemberResponse.groupPath());
        assertEquals("", addMemberResponse.groupPath());

        assertNotNull(addMemberResponse.uid());
        assertEquals("", addMemberResponse.uid());

        assertNotNull(addMemberResponse.uhUuid());
        assertEquals("", addMemberResponse.uhUuid());

        assertNotNull(addMemberResponse.name());
        assertEquals("", addMemberResponse.name());
    }
}
