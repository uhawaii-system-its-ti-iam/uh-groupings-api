package edu.hawaii.its.api.groupings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GroupingsUpdatedAttributeResultTest {

    private PropertyLocator propertyLocator;

    @BeforeEach
    public void beforeEach() {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void test() {
        assertNotNull(new GroupingsUpdatedAttributeResult());
        String json = propertyLocator.find("ws.assign.attributes.results.turn.off.opt.in.success");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        AssignAttributesResults assignAttributesResults = new AssignAttributesResults(wsAssignAttributesResults);
        GroupingsUpdatedAttributeResult groupingsUpdatedAttributeResult =
                new GroupingsUpdatedAttributeResult(assignAttributesResults);
        assertNotNull(groupingsUpdatedAttributeResult);
        assertEquals("SUCCESS", groupingsUpdatedAttributeResult.getResultCode());
        assertEquals("group-path", groupingsUpdatedAttributeResult.getGroupPath());

    }
}
