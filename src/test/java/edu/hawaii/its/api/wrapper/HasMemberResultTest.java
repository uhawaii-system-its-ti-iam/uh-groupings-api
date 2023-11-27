package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;

import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResult;

public class HasMemberResultTest {

    private PropertyLocator propertyLocator;

    @BeforeEach
    public void beforeEach() throws Exception {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void construction() {
        assertNotNull(new HasMemberResult());
        assertNotNull(new HasMemberResult(null));
    }

    @Test
    public void nullSubject() {
        String json = propertyLocator.find("ws.has.member.result.null.subject.result.code");
        WsHasMemberResult wsHasMemberResult = JsonUtil.asObject(json, WsHasMemberResult.class);
        assertNotNull(wsHasMemberResult);
        HasMemberResult hasMemberResult = new HasMemberResult(wsHasMemberResult);
        assertEquals("", hasMemberResult.getName());
        assertEquals("", hasMemberResult.getUid());
        assertEquals("", hasMemberResult.getUhUuid());
        assertEquals("", hasMemberResult.getResultCode());
        assertNotNull(hasMemberResult.getSubject());
    }
}
