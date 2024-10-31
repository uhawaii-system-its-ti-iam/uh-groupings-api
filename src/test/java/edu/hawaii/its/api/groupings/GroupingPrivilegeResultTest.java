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
import edu.hawaii.its.api.wrapper.AssignGrouperPrivilegesResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;

public class GroupingPrivilegeResultTest {

    private static Properties properties;
    private GroupingPrivilegeResult groupingPrivilegeResult;

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
    public void testConstructor() {
        String json = propertyValue("ws.assign.grouper.privileges.results.success");
        WsAssignGrouperPrivilegesLiteResult wsAssignGrouperPrivilegesLiteResult =
                JsonUtil.asObject(json, WsAssignGrouperPrivilegesLiteResult.class);
        assertNotNull(wsAssignGrouperPrivilegesLiteResult);
        AssignGrouperPrivilegesResult assignGrouperPrivilegesResult =
                new AssignGrouperPrivilegesResult(wsAssignGrouperPrivilegesLiteResult);
        assertNotNull(assignGrouperPrivilegesResult);
        groupingPrivilegeResult = new GroupingPrivilegeResult(assignGrouperPrivilegesResult);
        assertNotNull(groupingPrivilegeResult, "GroupingPrivilegeResult is not null");
    }

    @Test
    public void accessors() {
        String json = propertyValue("ws.assign.grouper.privileges.results.success");
        WsAssignGrouperPrivilegesLiteResult wsAssignGrouperPrivilegesLiteResult =
                JsonUtil.asObject(json, WsAssignGrouperPrivilegesLiteResult.class);
        assertNotNull(wsAssignGrouperPrivilegesLiteResult);
        AssignGrouperPrivilegesResult assignGrouperPrivilegesResult =
                new AssignGrouperPrivilegesResult(wsAssignGrouperPrivilegesLiteResult);
        groupingPrivilegeResult = new GroupingPrivilegeResult(assignGrouperPrivilegesResult);
        assertNotNull(groupingPrivilegeResult.getSubject());
        assertEquals("name", groupingPrivilegeResult.getSubject().getName());
        assertEquals("group-path", groupingPrivilegeResult.getGroupPath());
        assertEquals("privilege-type", groupingPrivilegeResult.getPrivilegeType());
        assertEquals("privilege-name", groupingPrivilegeResult.getPrivilegeName());
        assertEquals("SUCCESS", groupingPrivilegeResult.getResultCode());
    }
}
