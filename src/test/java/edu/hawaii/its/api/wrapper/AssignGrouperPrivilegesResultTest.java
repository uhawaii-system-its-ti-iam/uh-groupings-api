package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;

public class AssignGrouperPrivilegesResultTest {
    private PropertyLocator propertyLocator;

    @BeforeEach
    public void beforeAll() throws Exception {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void test() {
        String json = propertyLocator.find("ws.assign.grouper.privileges.results.success");
        WsAssignGrouperPrivilegesLiteResult wsAssignGrouperPrivilegesLiteResult =
                JsonUtil.asObject(json, WsAssignGrouperPrivilegesLiteResult.class);
        assertNotNull(wsAssignGrouperPrivilegesLiteResult);
        AssignGrouperPrivilegesResult assignGrouperPrivilegesResult =
                new AssignGrouperPrivilegesResult(wsAssignGrouperPrivilegesLiteResult);
        assertNotNull(assignGrouperPrivilegesResult);
        assertEquals("SUCCESS", assignGrouperPrivilegesResult.getResultCode());
        assertNotNull(assignGrouperPrivilegesResult.getGroup());
        assertNotNull(assignGrouperPrivilegesResult.getSubject());
        assertEquals("privilege-name", assignGrouperPrivilegesResult.getPrivilegeName());
        assertEquals("privilege-type", assignGrouperPrivilegesResult.getPrivilegeType());
        assertFalse(assignGrouperPrivilegesResult.isAllowed());
    }
}
