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
import edu.hawaii.its.api.wrapper.AddMembersResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

public class GroupingAddResultsTest {

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
        String json = propertyValue("ws.add.member.results.success");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMembersResults addMembersResults = new AddMembersResults(wsAddMemberResults);
        GroupingAddResults groupingAddResults = new GroupingAddResults(addMembersResults);
        assertNotNull(groupingAddResults);
        List<GroupingAddResult> results = groupingAddResults.getResults();
        assertNotNull(results);
        assertEquals("SUCCESS", groupingAddResults.getResultCode());
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
