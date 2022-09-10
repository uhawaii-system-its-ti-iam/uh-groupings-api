package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RemoveMemberResultTest {

    private static Properties properties;
    final static private String SUCCESS = "SUCCESS";
    final static private String SUCCESS_WASNT_IMMEDIATE = "SUCCESS_WASNT_IMMEDIATE";

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }

    @Test
    public void construction() {
        String json = propertyValue("ws.delete.member.results.success.uid");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMemberResult removeMemberResult = new RemoveMemberResult(wsDeleteMemberResults);
        assertNotNull(removeMemberResult);
    }

    @Test
    public void accessors() {
        // When remove is queried with a uid.
        String json = propertyValue("ws.delete.member.results.success.uid");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMemberResult removeMemberResult = new RemoveMemberResult(wsDeleteMemberResults);

        assertNotNull(removeMemberResult.resultCode());
        assertEquals(SUCCESS, removeMemberResult.resultCode());
        assertTrue(removeMemberResult.isSuccess());

        assertNotNull(removeMemberResult.groupPath());
        assertEquals("group-path", removeMemberResult.groupPath());

        assertNotNull(removeMemberResult.uid());
        assertEquals("uid", removeMemberResult.uid());

        assertNotNull(removeMemberResult.uhUuid());
        assertEquals("uhuuid", removeMemberResult.uhUuid());

        assertNotNull(removeMemberResult.name());
        assertEquals("name", removeMemberResult.name());

        // When remove is queried with uhUuid.
        json = propertyValue("ws.delete.member.results.success.uhuuid");
        wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        removeMemberResult = new RemoveMemberResult(wsDeleteMemberResults);

        assertNotNull(removeMemberResult.resultCode());
        assertEquals(SUCCESS, removeMemberResult.resultCode());
        assertTrue(removeMemberResult.isSuccess());

        assertNotNull(removeMemberResult.groupPath());
        assertEquals("group-path", removeMemberResult.groupPath());

        assertNotNull(removeMemberResult.uid());
        assertEquals("uid", removeMemberResult.uid());

        assertNotNull(removeMemberResult.uhUuid());
        assertEquals("uhuuid", removeMemberResult.uhUuid());

        assertNotNull(removeMemberResult.name());
        assertEquals("name", removeMemberResult.name());

        // When grouper result contains null fields.
        json = propertyValue("ws.delete.member.results.null.values");
        wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        removeMemberResult = new RemoveMemberResult(wsDeleteMemberResults);

        assertNotNull(removeMemberResult.resultCode());
        assertEquals(SUCCESS_WASNT_IMMEDIATE, removeMemberResult.resultCode());
        assertFalse(removeMemberResult.isSuccess());

        assertNotNull(removeMemberResult.groupPath());
        assertEquals("", removeMemberResult.groupPath());

        assertNotNull(removeMemberResult.uid());
        assertEquals("", removeMemberResult.uid());

        assertNotNull(removeMemberResult.uhUuid());
        assertEquals("", removeMemberResult.uhUuid());

        assertNotNull(removeMemberResult.name());
        assertEquals("", removeMemberResult.name());

    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
