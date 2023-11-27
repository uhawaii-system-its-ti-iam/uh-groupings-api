package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;

public class GroupAttributeResultsTest {
    private static final String TRIO = "uh-settings:attributes:for-groups:uh-grouping:is-trio";
    private static final String SUCCESS = "SUCCESS";
    private static final String FAILURE = "FAILURE";
    private PropertyLocator propertyLocator;

    @BeforeEach
    public void beforeEach() throws Exception {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void constructor() {
        assertNotNull(new GroupAttributeResults(null));
    }

    @Test
    public void test() {
        String json = propertyLocator.find("ws.get.attribute.assignment.results.success");
        WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults =
                JsonUtil.asObject(json, WsGetAttributeAssignmentsResults.class);
        assertNotNull(wsGetAttributeAssignmentsResults);
        GroupAttributeResults groupAttributeResults = new GroupAttributeResults(wsGetAttributeAssignmentsResults);
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
