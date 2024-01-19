package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;

public class GroupingPathsTest {

    private PropertyLocator propertyLocator;

    @BeforeEach
    public void beforeEach() { propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties"); }

    @Test
    public void test() {
        String json = propertyLocator.find("ws.get.attribute.assignment.results.success");
        WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = JsonUtil.asObject(json, WsGetAttributeAssignmentsResults.class);
        GroupingPaths groupingPaths = new GroupingPaths(new GroupAttributeResults(wsGetAttributeAssignmentsResults));
        assertNotNull(groupingPaths);
        assertEquals("SUCCESS", groupingPaths.getResultCode());
        List<GroupingPath> paths = groupingPaths.getGroupingPaths();
        assertNotNull(paths);
        assertEquals(1, paths.size());
    }
}