package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HasMemberResultsTest {
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
        HasMemberResults hasMemberResults = new HasMemberResults(wsHasMemberResults);
        assertNotNull(hasMemberResults);

    }

    @Test public void test() {
        String json = propertyValue("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMemberResults hasMemberResults = new HasMemberResults(wsHasMemberResults);

        assertNotNull(hasMemberResults);
        assertEquals("IS_MEMBER", hasMemberResults.getResultCode());
        assertEquals("99997010", hasMemberResults.getUhUuid());
        assertEquals("testiwta", hasMemberResults.getUid());

        json = propertyValue("ws.has.member.results.is.members.uid");
        wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        hasMemberResults = new HasMemberResults(wsHasMemberResults);
        assertNotNull(hasMemberResults);
        assertEquals("IS_MEMBER", hasMemberResults.getResultCode());
        assertEquals("99997010", hasMemberResults.getUhUuid());
        assertEquals("testiwta", hasMemberResults.getUid());
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
