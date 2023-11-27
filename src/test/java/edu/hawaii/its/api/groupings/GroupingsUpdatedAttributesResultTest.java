package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;

public class GroupingsUpdatedAttributesResultTest {

    private PropertyLocator propertyLocator;

    @BeforeEach
    public void beforeEach() {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void test() {
        assertNotNull(new GroupingUpdatedAttributesResult());
        assertNotNull(new GroupingUpdatedAttributesResult(null));
        String json = propertyLocator.find("ws.assign.attributes.results.turn.off.opt.in.success");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        AssignAttributesResults assignAttributesResults = new AssignAttributesResults(wsAssignAttributesResults);
        GroupingUpdatedAttributesResult groupingsUpdatedAttributesResult = new GroupingUpdatedAttributesResult(assignAttributesResults);
        assertNotNull(groupingsUpdatedAttributesResult);
        assertEquals("SUCCESS", groupingsUpdatedAttributesResult.getResultCode());
        assertEquals("group-path", groupingsUpdatedAttributesResult.getGroupPath());
    }

}
