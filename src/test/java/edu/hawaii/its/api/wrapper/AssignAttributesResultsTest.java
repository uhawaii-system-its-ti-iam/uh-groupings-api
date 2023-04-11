package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AssignAttributesResultsTest {
    private PropertyLocator propertyLocator;

    @BeforeEach
    public void beforeAll() throws Exception {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void test() {
        String json = propertyLocator.find("ws.assign.attributes.results.turn.off.opt.in.success");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        AssignAttributesResults assignAttributesResults = new AssignAttributesResults(wsAssignAttributesResults);
        assertNotNull(assignAttributesResults);
        assertEquals("SUCCESS", assignAttributesResults.getResultCode());
        assertNotNull(assignAttributesResults.getGroup());
        assertNotNull(assignAttributesResults.getAttributesResults());
    }
}
