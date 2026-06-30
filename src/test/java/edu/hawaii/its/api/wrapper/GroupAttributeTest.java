package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupAttributeTest {
    private static final String TRIO = "uh-settings:attributes:for-groups:uh-grouping:is-trio";

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void constructor() {
        assertNotNull(groupingsTestConfiguration.attributeAssignSuccessTestData());
        assertNotNull(new GroupAttribute(null));
        assertNotNull(new GroupAttribute());
        assertNotNull(new GroupAttribute(new WsAttributeAssign()));
    }

    @Test
    public void accessors() {
        GroupAttribute groupAttribute =
                groupingsTestConfiguration.attributeAssignSuccessTestData();

        assertNotNull(groupAttribute);
        assertEquals("SUCCESS", groupAttribute.getResultCode());
        assertEquals("group-path", groupAttribute.getGroupPath());
        assertEquals(TRIO, groupAttribute.getAttributeName());

        groupAttribute =
                groupingsTestConfiguration.attributeAssignFailureTestData();
        assertNotNull(groupAttribute);
        assertEquals("FAILURE", groupAttribute.getResultCode());
        assertEquals("", groupAttribute.getGroupPath());
        assertEquals("", groupAttribute.getAttributeName());
    }
}
