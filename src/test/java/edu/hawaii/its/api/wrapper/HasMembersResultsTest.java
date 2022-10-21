package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HasMembersResultsTest {

    private static Properties properties;

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));

    }

    @Test
    public void construction() {
        String json = propertyValue("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
    }


    @Test
    public void test() {
        String json = propertyValue("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        JsonUtil.printJson(hasMembersResults);

        List<String> testUsernames = getTestUsernames();
        List<String> testNumbers = getTestNumbers();
        int i = 0;
        for (HasMemberResult result : hasMembersResults.getResults()) {
            assertEquals("IS_MEMBER", result.getResultCode());
            assertEquals(testUsernames.get(i), result.getUid());
            assertEquals(testNumbers.get(i),result.getUhUuid());
            i++;
        }

        json = propertyValue("ws.has.member.results.is.members.uid");
        wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        hasMembersResults = new HasMembersResults(wsHasMemberResults);

        i = 0;
        for (HasMemberResult result : hasMembersResults.getResults()) {
            assertEquals("IS_MEMBER", result.getResultCode());
            assertEquals(testUsernames.get(i), result.getUid());
            assertEquals(testNumbers.get(i),result.getUhUuid());
            i++;
        }
    }


    public List<String> getTestUsernames() {
        String[] array = { "testiwta", "testiwtb", "testiwtc", "testiwtd", "testiwte" };
        return new ArrayList<>(Arrays.asList(array));
    }

    public List<String> getTestNumbers() {
        String[] array = { "99997010", "99997027", "99997033", "99997043", "99997056" };
        return new ArrayList<>(Arrays.asList(array));
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
