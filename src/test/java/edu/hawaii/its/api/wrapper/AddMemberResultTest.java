package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

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
        assertNotNull(new AddMemberResult(null, null));
        assertNotNull(new AddMemberResult());
        String json = propertyValue("ws.add.member.results.success");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMemberResult addMemberResult = new AddMemberResult(wsAddMemberResults.getResults()[0], "group-path");
        assertEquals("group-path", addMemberResult.getGroupPath());
        assertEquals("SUCCESS_ALREADY_EXISTED", addMemberResult.getResultCode());
        assertEquals("group-path", addMemberResult.getGroupPath());
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }

}
