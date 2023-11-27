package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

public class GroupingMoveMemberResultTest {
    private static final String SUCCESS = "SUCCESS";
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
        assertNotNull(new GroupingMoveMemberResult());

        String json = propertyValue("ws.add.member.results.success");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        assertNotNull(wsAddMemberResults);
        AddMemberResult addMemberResult = new AddMemberResult(wsAddMemberResults.getResults()[0], "group-path");
        assertNotNull(addMemberResult);

        json = propertyValue("ws.delete.member.results.success");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        assertNotNull(wsDeleteMemberResults);
        RemoveMemberResult removeMemberResult =
                new RemoveMemberResult(wsDeleteMemberResults.getResults()[0], "group-path");
        assertNotNull(removeMemberResult);

        GroupingMoveMemberResult groupingMoveMemberResult =
                new GroupingMoveMemberResult(addMemberResult, removeMemberResult);
        assertNotNull(groupingMoveMemberResult);
    }

    @Test
    public void successfulAccessors() {
        String json = propertyValue("ws.add.member.results.success");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMemberResult addMemberResult = new AddMemberResult(wsAddMemberResults.getResults()[2], "group-path");

        json = propertyValue("ws.delete.member.results.success");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMemberResult removeMemberResult =
                new RemoveMemberResult(wsDeleteMemberResults.getResults()[0], "group-path");

        GroupingMoveMemberResult groupingMoveMemberResult =
                new GroupingMoveMemberResult(addMemberResult, removeMemberResult);
        assertNotNull(groupingMoveMemberResult);
        assertNotNull(groupingMoveMemberResult.getAddResult());
        assertNotNull(groupingMoveMemberResult.getRemoveResult());
        assertEquals(SUCCESS, groupingMoveMemberResult.getResultCode());
        assertEquals("group-path", groupingMoveMemberResult.getGroupPath());

        addMemberResult = new AddMemberResult(wsAddMemberResults.getResults()[0], "group-path");
        groupingMoveMemberResult = new GroupingMoveMemberResult(addMemberResult, removeMemberResult);
        assertEquals("FAILURE", groupingMoveMemberResult.getResultCode());

    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
