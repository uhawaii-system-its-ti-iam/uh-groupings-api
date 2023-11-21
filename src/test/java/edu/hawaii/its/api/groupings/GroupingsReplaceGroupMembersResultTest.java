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
import edu.hawaii.its.api.wrapper.AddMembersResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

public class GroupingsReplaceGroupMembersResultTest {
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
    public void constructor() {
        GroupingReplaceGroupMembersResult groupingsReplaceGroupMembersResult =
                new GroupingReplaceGroupMembersResult();
        assertNotNull(groupingsReplaceGroupMembersResult);

        String json = propertyValue("ws.add.member.results.reset.group");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMembersResults addMembersResults = new AddMembersResults(wsAddMemberResults);
        assertNotNull(addMembersResults);
        groupingsReplaceGroupMembersResult = new GroupingReplaceGroupMembersResult(addMembersResults);
        assertNotNull(groupingsReplaceGroupMembersResult);
    }

    @Test
    public void accessors() {
        String json = propertyValue("ws.add.member.results.reset.group");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMembersResults addMembersResults = new AddMembersResults(wsAddMemberResults);
        GroupingReplaceGroupMembersResult groupingsReplaceGroupMembersResult =
                new GroupingReplaceGroupMembersResult(addMembersResults);
        assertNotNull(groupingsReplaceGroupMembersResult.getGroupPath());
        assertEquals("group-path", groupingsReplaceGroupMembersResult.getGroupPath());
        assertNotNull(groupingsReplaceGroupMembersResult.getResultCode());
        assertEquals("SUCCESS", groupingsReplaceGroupMembersResult.getResultCode());
    }
}
