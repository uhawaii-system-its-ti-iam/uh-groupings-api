package edu.hawaii.its.api.groupings;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddMembersResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GroupingsAddResultTest {

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
        String json = propertyValue("ws.add.member.results.success.single.result");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMemberResult addMemberResult = new AddMemberResult(wsAddMemberResults);
        GroupingsAddResult groupingsAddResult = new GroupingsAddResult(addMemberResult);
        assertNotNull(groupingsAddResult);

        assertEquals("SUCCESS", groupingsAddResult.resultCode);
        assertEquals("uid", groupingsAddResult.uid);
        assertEquals("uhUuid", groupingsAddResult.getUhUuid());
        assertEquals("name", groupingsAddResult.getName());

        json = propertyValue("ws.add.member.results.success");
        wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMembersResults addMembersResults = new AddMembersResults(wsAddMemberResults);
        groupingsAddResult = new GroupingsAddResult(addMembersResults.getResults().get(0));
        assertNotNull(groupingsAddResult);

        assertEquals("SUCCESS", groupingsAddResult.resultCode);
        assertEquals("uid-0", groupingsAddResult.uid);
        assertEquals("uhUuid-0", groupingsAddResult.getUhUuid());
        assertEquals("name-0", groupingsAddResult.getName());
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
