package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

public class GroupingMoveMembersResultTest {

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
        String json = propertyValue("ws.add.member.results.success");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        assertNotNull(wsAddMemberResults);
        AddMembersResults addMembersResults = new AddMembersResults(wsAddMemberResults);
        assertNotNull(addMembersResults);

        json = propertyValue("ws.delete.member.results.success");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        assertNotNull(wsDeleteMemberResults);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        assertNotNull(removeMembersResults);

        GroupingMoveMembersResult groupingMoveMembersResult =
                new GroupingMoveMembersResult(addMembersResults, removeMembersResults);
        assertNotNull(groupingMoveMembersResult);
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
