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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AddMemberResultTest {

    private static Properties properties;
    final static private String SUCCESS = "SUCCESS";
    final static private String SUCCESS_ALREADY_EXISTED = "SUCCESS_ALREADY_EXISTED";

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }

    @Test
    public void construction() {
        String json = propertyValue("ws.add.member.results.success.uid");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMemberResult addMemberResult = new AddMemberResult(wsAddMemberResults);
        assertNotNull(addMemberResult);
    }

    @Test
    public void accessors() {
        // When add is queried with a uid.
        String json = propertyValue("ws.add.member.results.success.uid");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMemberResult addMemberResult = new AddMemberResult(wsAddMemberResults);

        assertNotNull(addMemberResult.resultCode());
        assertEquals(SUCCESS, addMemberResult.resultCode());

        assertTrue(addMemberResult.isSuccess());

        assertNotNull(addMemberResult.groupPath());
        assertEquals("group-path", addMemberResult.groupPath());

        assertNotNull(addMemberResult.uid());
        assertEquals("uid", addMemberResult.uid());

        assertNotNull(addMemberResult.uhUuid());
        assertEquals("uhuuid", addMemberResult.uhUuid());

        assertNotNull(addMemberResult.name());
        assertEquals("name", addMemberResult.name());

        // When add is queried with a uhuuid.
        json = propertyValue("ws.add.member.results.success.uhuuid");
        wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        addMemberResult = new AddMemberResult(wsAddMemberResults);

        assertNotNull(addMemberResult.resultCode());
        assertEquals(SUCCESS, addMemberResult.resultCode());
        assertTrue(addMemberResult.isSuccess());

        assertNotNull(addMemberResult.groupPath());
        assertEquals("group-path", addMemberResult.groupPath());

        assertNotNull(addMemberResult.uid());
        assertEquals("uid", addMemberResult.uid());

        assertNotNull(addMemberResult.uhUuid());
        assertEquals("uhuuid", addMemberResult.uhUuid());

        assertNotNull(addMemberResult.name());
        assertEquals("name", addMemberResult.name());

        // When grouper result contains null fields.
        json = propertyValue("ws.add.member.results.null.values");
        wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        addMemberResult = new AddMemberResult(wsAddMemberResults);

        assertNotNull(addMemberResult.resultCode());
        assertEquals(SUCCESS_ALREADY_EXISTED, addMemberResult.resultCode());
        assertFalse(addMemberResult.isSuccess());

        assertNotNull(addMemberResult.groupPath());
        assertEquals("", addMemberResult.groupPath());

        assertNotNull(addMemberResult.uid());
        assertEquals("", addMemberResult.uid());

        assertNotNull(addMemberResult.uhUuid());
        assertEquals("", addMemberResult.uhUuid());

        assertNotNull(addMemberResult.name());
        assertEquals("", addMemberResult.name());
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
