package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class AssignGrouperPrivilegesResultTest {

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void test() {
        AssignGrouperPrivilegesResult assignGrouperPrivilegesResult =
                groupingsTestConfiguration.assignGrouperPrivilegesResultsSuccessTestData();
        assertNotNull(assignGrouperPrivilegesResult);
        assertEquals("SUCCESS", assignGrouperPrivilegesResult.getResultCode());
        assertNotNull(assignGrouperPrivilegesResult.getGroup());
        assertNotNull(assignGrouperPrivilegesResult.getSubject());
        assertEquals("privilege-name", assignGrouperPrivilegesResult.getPrivilegeName());
        assertEquals("privilege-type", assignGrouperPrivilegesResult.getPrivilegeType());
        assertFalse(assignGrouperPrivilegesResult.isAllowed());
    }
}
