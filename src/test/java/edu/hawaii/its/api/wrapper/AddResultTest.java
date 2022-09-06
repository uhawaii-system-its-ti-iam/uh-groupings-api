package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResult;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AddResultTest {

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

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
