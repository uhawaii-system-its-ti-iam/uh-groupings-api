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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HasMemberResponseTest {
    private static Properties properties;

    private static final String IS_MEMBER = "IS_MEMBER";
    private static final String IS_NOT_MEMBER = "IS_NOT_MEMBER";

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }

    @Test
    public void construction() {
        String json = propertyValue("ws.has.member.results.is.member.uid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMemberResponse hasMemberResponse = new HasMemberResponse(wsHasMemberResults);
        assertNotNull(hasMemberResponse);
    }

    @Test
    public void accessors() {
        // Queried with a UH username that is a member.
        String json = propertyValue("ws.has.member.results.is.member.uid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMemberResponse hasMemberResponse = new HasMemberResponse(wsHasMemberResults);

        assertNotNull(hasMemberResponse.resultCode());
        assertEquals(IS_MEMBER, hasMemberResponse.resultCode());
        assertTrue(hasMemberResponse.isMember());

        // Queried with a UH number that is a member.
        json = propertyValue("ws.has.member.results.is.member.uhuuid");
        wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        hasMemberResponse = new HasMemberResponse(wsHasMemberResults);

        assertNotNull(hasMemberResponse.resultCode());
        assertEquals(IS_MEMBER, hasMemberResponse.resultCode());
        assertTrue(hasMemberResponse.isMember());

        // Queried with a UH username that is not a member.
        json = propertyValue("ws.has.member.results.not.member.uid");
        wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        hasMemberResponse = new HasMemberResponse(wsHasMemberResults);

        assertNotNull(hasMemberResponse.resultCode());
        assertEquals(IS_NOT_MEMBER, hasMemberResponse.resultCode());
        assertFalse(hasMemberResponse.isMember());

        // Queried with a UH number that is not a member.
        json = propertyValue("ws.has.member.results.not.member.uhuuid");
        wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        hasMemberResponse = new HasMemberResponse(wsHasMemberResults);

        assertNotNull(hasMemberResponse.resultCode());
        assertEquals(IS_NOT_MEMBER, hasMemberResponse.resultCode());
        assertFalse(hasMemberResponse.isMember());
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
