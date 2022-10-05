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
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RemoveMemberResultTest {

    private static Properties properties;

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }

    @Test
    public void construction() {
        String json = propertyValue("ws.delete.member.results.success.single.result");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMemberResult removeMemberResult = new RemoveMemberResult(wsDeleteMemberResults);
        assertNotNull(removeMemberResult);

        removeMemberResult = new RemoveMemberResult(null);
        assertNotNull(removeMemberResult);
    }

    @Test
    public void memberRemoveSuccessTest() {
        String json = propertyValue("ws.delete.member.results.success.single.result");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);

        RemoveMemberResult removeMemberResult = new RemoveMemberResult(wsDeleteMemberResults);
        assertNotNull(removeMemberResult);
        assertNotNull(removeMemberResult.getResultCode());
        assertNotNull(removeMemberResult.getGroupPath());
        assertNotNull(removeMemberResult.getUid());
        assertNotNull(removeMemberResult.getUhUuid());
        assertNotNull(removeMemberResult.getName());
        assertEquals("SUCCESS", removeMemberResult.getResultCode());
        assertEquals("group-path", removeMemberResult.getGroupPath());
        assertEquals("uid", removeMemberResult.getUid());
        assertEquals("uhUuid", removeMemberResult.getUhUuid());
        assertEquals("name", removeMemberResult.getName());
    }

    @Test
    public void memberNotInGroupTest() {
        // The uh identifier passed is valid but is not in the group.
        String json = propertyValue("ws.delete.member.results.not.in.group.single.result");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);

        RemoveMemberResult removeMemberResult = new RemoveMemberResult(wsDeleteMemberResults);
        assertNotNull(removeMemberResult);
        assertNotNull(removeMemberResult.getResultCode());
        assertNotNull(removeMemberResult.getGroupPath());
        assertNotNull(removeMemberResult.getUid());
        assertNotNull(removeMemberResult.getUhUuid());
        assertNotNull(removeMemberResult.getName());
        assertEquals("SUCCESS_WASNT_IMMEDIATE", removeMemberResult.getResultCode());
        assertEquals("group-path", removeMemberResult.getGroupPath());
        assertEquals("uid", removeMemberResult.getUid());
        assertEquals("uhUuid", removeMemberResult.getUhUuid());
        assertEquals("name", removeMemberResult.getName());
    }

    @Test
    public void invalidMemberTest() {
        // The uh identifier passed is invalid.
        String json = propertyValue("ws.delete.member.results.invalid.single.result");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);

        RemoveMemberResult removeMemberResult = new RemoveMemberResult(wsDeleteMemberResults);
        assertNotNull(removeMemberResult);
        assertNotNull(removeMemberResult.getResultCode());
        assertNotNull(removeMemberResult.getGroupPath());
        assertNotNull(removeMemberResult.getUid());
        assertNotNull(removeMemberResult.getUhUuid());
        assertNotNull(removeMemberResult.getName());
        assertEquals("SUCCESS_WASNT_IMMEDIATE", removeMemberResult.getResultCode());
        assertEquals("group-path", removeMemberResult.getGroupPath());
        assertEquals("", removeMemberResult.getUid());
        assertEquals("bogus-name", removeMemberResult.getUhUuid());
        assertEquals("", removeMemberResult.getName());
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
