package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

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
    public void constructor() {
        String json = propertyValue("ws.delete.member.results.success");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        GroupingsRemoveResults groupingsRemoveResults = new GroupingsRemoveResults(removeMembersResults);
        assertNotNull(groupingsRemoveResults);
    }

    @Test
    public void add() {
        String json = propertyValue("ws.delete.member.results.success");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        GroupingsRemoveResults groupingsRemoveResults = new GroupingsRemoveResults(removeMembersResults);
        List<GroupingsRemoveResult> results = groupingsRemoveResults.getResults();
        assertNotNull(results);

        json = propertyValue("ws.delete.member.results.success.single.result");
        wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMemberResult removeMemberResult = new RemoveMemberResult(wsDeleteMemberResults);
        GroupingsRemoveResult groupingsRemoveResult = new GroupingsRemoveResult(removeMemberResult);
        groupingsRemoveResults.add(groupingsRemoveResult);
        assertEquals(4, groupingsRemoveResults.getResults().size());
        GroupingsRemoveResult addedResult = groupingsRemoveResults.getResults().get(3);
        assertEquals(groupingsRemoveResult, addedResult);
        assertEquals("SUCCESS", addedResult.getResultCode());
        assertEquals("uid", addedResult.getUid());
        assertEquals("uhUuid", addedResult.getUhUuid());
        assertEquals("group-path", addedResult.getGroupPath());

        json = propertyValue("ws.delete.member.results.success");
        wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        GroupingsRemoveResults resultsToAdd = new GroupingsRemoveResults(removeMembersResults);
        groupingsRemoveResults.add(resultsToAdd);
        assertNotNull(groupingsRemoveResults);
        assertEquals(7, groupingsRemoveResults.getResults().size());
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
