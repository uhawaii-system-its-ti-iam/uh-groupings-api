package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupingRemoveResultTest {

    @Value("${groupings.api.test.uids}")
    private List<String> TEST_UIDS;

    @Value("${groupings.api.test.uh-uuids}")
    private List<String> TEST_UH_UUIDS;

    @Value("${groupings.api.test.uh-names}")
    private List<String> TEST_NAMES;

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
        assertNotNull(wsDeleteMemberResults);
        assertTrue(wsDeleteMemberResults.getResults().length > 0);
        WsDeleteMemberResult wsDeleteMemberResult = wsDeleteMemberResults.getResults()[0];
        assertNotNull(wsDeleteMemberResult);
        GroupingRemoveResult groupingRemoveResult =
                new GroupingRemoveResult(new RemoveMemberResult(wsDeleteMemberResult, "group-path"));

        assertNotNull(groupingRemoveResult);
        assertEquals("SUCCESS_WASNT_IMMEDIATE", groupingRemoveResult.getResultCode());
        assertEquals(TEST_UIDS.get(0), groupingRemoveResult.getUid());
        assertEquals(TEST_UH_UUIDS.get(0), groupingRemoveResult.getUhUuid());
        assertEquals(TEST_NAMES.get(0), groupingRemoveResult.getName());
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
