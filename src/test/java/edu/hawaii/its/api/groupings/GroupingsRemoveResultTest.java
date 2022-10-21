package edu.hawaii.its.api.groupings;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class GroupingsRemoveResultTest {
    private static Properties properties;

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }

    @Test
    public void test() {
        String json = propertyValue("ws.delete.member.results.success.single.result");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMemberResult removeMemberResult = new RemoveMemberResult(wsDeleteMemberResults);
        GroupingsRemoveResult groupingsRemoveResult = new GroupingsRemoveResult(removeMemberResult);
        assertNotNull(groupingsRemoveResult);

        assertEquals("SUCCESS", groupingsRemoveResult.resultCode);
        assertEquals("uid", groupingsRemoveResult.uid);
        assertEquals("uhUuid", groupingsRemoveResult.getUhUuid());
        assertEquals("name", groupingsRemoveResult.getName());
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
