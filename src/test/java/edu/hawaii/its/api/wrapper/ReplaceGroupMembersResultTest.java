package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ReplaceGroupMembersResultTest {

    private static Properties properties;

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }

    @Test
    public void construction() {
        String json = propertyValue("ws.add.member.results.reset.group");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        ReplaceGroupMembersResult replaceGroupMembersResult = new ReplaceGroupMembersResult(wsAddMemberResults);
        assertNotNull(replaceGroupMembersResult);
        replaceGroupMembersResult = new ReplaceGroupMembersResult();
        assertNotNull(replaceGroupMembersResult);
    }

    @Test void success() {
        String json = propertyValue("ws.add.member.results.reset.group");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        ReplaceGroupMembersResult replaceGroupMembersResult = new ReplaceGroupMembersResult(wsAddMemberResults);
        assertEquals("SUCCESS", replaceGroupMembersResult.getResultCode());
        assertEquals("group-path", replaceGroupMembersResult.getGroupPath());
        assertTrue(replaceGroupMembersResult.getResultMessage().contains("No subjects were passed in"));
    }

}
