package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AddMemberResultTest {

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

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
