package edu.hawaii.its.api.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.groupings.GroupingAddResults;
import edu.hawaii.its.api.groupings.GroupingTimestampResults;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AddMembersResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestUpdateTimestampService {

    private static Properties properties;
    @Autowired
    private UpdateTimestampService timestampService;

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }

    @Test
    public void update() {
        String json = propertyValue("ws.add.member.results.failure");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMembersResults addMembersResults = new AddMembersResults(wsAddMemberResults);
        GroupingAddResults groupingAddResults = new GroupingAddResults(addMembersResults);
        GroupingTimestampResults groupingsTimestampResults = timestampService.update(groupingAddResults);
        assertNotNull(groupingAddResults);
        for (Boolean aBoolean : groupingsTimestampResults.isTimeUpdatedList()) {
            assertFalse(aBoolean);
        }
    }

    @Test
    public void updateNonOwnerList() {
        String json = propertyValue("ws.add.member.results.success.include.timestamp");
        GroupingAddResults groupingResult = JsonUtil.asObject(json, GroupingAddResults.class);
        GroupingTimestampResults groupingsTimestampResults = timestampService.update(groupingResult);

        assertEquals("SUCCESS", groupingsTimestampResults.getResultCode());
        assertEquals(1, groupingsTimestampResults.getGroupPaths().size());
    }

    @Test
    public void updateOwnersList() {
        String json = propertyValue("ws.add.member.results.success.owners.timestamp");
        GroupingAddResults groupingResult = JsonUtil.asObject(json, GroupingAddResults.class);
        GroupingTimestampResults groupingsTimestampResults = timestampService.update(groupingResult);

        assertEquals(groupingsTimestampResults.getCurrentUpdatedTimeList().get(0),
                groupingsTimestampResults.getCurrentUpdatedTimeList().get(1));
        assertEquals("SUCCESS", groupingsTimestampResults.getResultCode());
        assertEquals(2, groupingsTimestampResults.getGroupPaths().size());
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
