package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

public class GroupingRemoveResultsTest {
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
        assertNotNull(wsDeleteMemberResults);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        assertNotNull(removeMembersResults);
        GroupingRemoveResults groupingRemoveResults = new GroupingRemoveResults(removeMembersResults);
        assertNotNull(groupingRemoveResults);
    }

    @Test
    public void test() {
        String json = propertyValue("ws.delete.member.results.success");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        GroupingRemoveResults groupingRemoveResults = new GroupingRemoveResults(removeMembersResults);
        assertEquals("SUCCESS", groupingRemoveResults.getResultCode());

        json = propertyValue("ws.delete.member.results.failure");
        wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        groupingRemoveResults = new GroupingRemoveResults(removeMembersResults);
        assertEquals("FAILURE", groupingRemoveResults.getResultCode());
    }

    @Test
    public void add() {
        String json = propertyValue("ws.delete.member.results.success");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        GroupingRemoveResults groupingRemoveResults = new GroupingRemoveResults(removeMembersResults);
        List<GroupingRemoveResult> results = groupingRemoveResults.getResults();
        assertNotNull(results);

        RemoveMemberResult removeMemberResult =
                new RemoveMemberResult(wsDeleteMemberResults.getResults()[0], "group-path");
        GroupingRemoveResult groupingRemoveResult = new GroupingRemoveResult(removeMemberResult);
        assertEquals(5, groupingRemoveResults.getResults().size());
        groupingRemoveResults.add(groupingRemoveResult);
        assertEquals(6, groupingRemoveResults.getResults().size());

        json = propertyValue("ws.delete.member.results.success");
        wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        GroupingRemoveResults resultsToAdd = new GroupingRemoveResults(removeMembersResults);
        groupingRemoveResults.add(resultsToAdd);
        assertNotNull(groupingRemoveResults);
        assertEquals(11, groupingRemoveResults.getResults().size());
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
