package edu.hawaii.its.api.groupings;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GroupingsRemoveResultsTest {

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
        String json = propertyValue("ws.delete.member.results.success");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        GroupingsRemoveResults groupingsRemoveResults = new GroupingsRemoveResults(removeMembersResults);
        assertNotNull(groupingsRemoveResults);
        List<GroupingsRemoveResult> results = groupingsRemoveResults.getResults();
        assertNotNull(results);
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
