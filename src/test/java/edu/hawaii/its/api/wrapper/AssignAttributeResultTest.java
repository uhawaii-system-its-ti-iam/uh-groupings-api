package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributeResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;

public class AssignAttributeResultTest {
    private PropertyLocator propertyLocator;

    @BeforeEach
    public void beforeAll() throws Exception {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void test() {
        String json = propertyLocator.find("ws.assign.attributes.results.turn.off.opt.in.success");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        WsAssignAttributeResult wsAssignAttributeResult = wsAssignAttributesResults.getWsAttributeAssignResults()[0];
        AssignAttributeResult assignAttributeResult = new AssignAttributeResult(wsAssignAttributeResult);
        assertNotNull(assignAttributeResult);
        assertEquals("SUCCESS", assignAttributeResult.getResultCode());
        assertTrue(assignAttributeResult.isAttributeChanged());
        assertFalse(assignAttributeResult.isAttributeValuesChanged());
        assertTrue(assignAttributeResult.isAttributeRemoved());

        json = propertyLocator.find("ws.assign.attributes.results.null.assign.attribute.result");
        wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        wsAssignAttributeResult = wsAssignAttributesResults.getWsAttributeAssignResults()[0];
        assignAttributeResult = new AssignAttributeResult(wsAssignAttributeResult);
        assertNotNull(assignAttributeResult);
        assertEquals("FAILURE", assignAttributeResult.getResultCode());
        assertFalse(assignAttributeResult.isAttributeChanged());
        assertFalse(assignAttributeResult.isAttributeValuesChanged());
        assertFalse(assignAttributeResult.isAttributeRemoved());
    }
}
