package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupAttributeResultsTest {
    private static final String TRIO = "uh-settings:attributes:for-groups:uh-grouping:is-trio";
    private static final String SUCCESS = "SUCCESS";
    private static final String FAILURE = "FAILURE";

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void constructor() {
        assertNotNull(new GroupAttributeResults(null));
    }

    @Test
    public void test() {
        GroupAttributeResults groupAttributeResults =
                groupingsTestConfiguration.getAttributeAssignmentResultsSuccessTestData();
        assertNotNull(groupAttributeResults);
        assertEquals(SUCCESS, groupAttributeResults.getResultCode());
        List<GroupAttribute> groupAttributes = groupAttributeResults.getGroupAttributes();
        assertFalse(groupAttributes.isEmpty());
        for (GroupAttribute groupAttribute : groupAttributes) {
            assertEquals(TRIO, groupAttribute.getAttributeName());
            assertEquals(SUCCESS, groupAttribute.getResultCode());
            assertEquals("group-path", groupAttribute.getGroupPath());
        }
    }
}
