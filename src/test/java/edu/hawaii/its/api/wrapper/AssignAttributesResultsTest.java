package edu.hawaii.its.api.wrapper;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributeResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefName;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;

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

    @Test
    public void constructorNullTest() {
        AssignAttributesResults assignAttributesResults = new AssignAttributesResults(null);
        assertNotNull(assignAttributesResults.getAssignAttributeResults());
    }

    @Test
    public void emptyGroupTest() {
        WsAssignAttributesResults wsAssignAttributesResults = new WsAssignAttributesResults();
        wsAssignAttributesResults.setWsGroups(new WsGroup[0]);
        AssignAttributesResults results = new AssignAttributesResults(wsAssignAttributesResults);
        assertNotNull(results.getGroup());
    }

    @Test
    public void emptyAttributeResultsTest() {
        WsAssignAttributesResults wsAssignAttributesResults = new WsAssignAttributesResults();
        AssignAttributesResults assignAttributesResults = new AssignAttributesResults(wsAssignAttributesResults);
        assertNotNull(assignAttributesResults.getAssignAttributeResults());
        assertTrue(assignAttributesResults.getAssignAttributeResults().isEmpty());
    }

    @Test
    public void attributeResultsTest() {
        WsAssignAttributeResult wsAssignAttributeResult1 = new WsAssignAttributeResult();
        WsAssignAttributeResult wsAssignAttributeResult2 = new WsAssignAttributeResult();
        WsAssignAttributesResults wsAssignAttributesResults = new WsAssignAttributesResults();
        wsAssignAttributesResults.setWsAttributeAssignResults(new WsAssignAttributeResult[]{wsAssignAttributeResult1, wsAssignAttributeResult2});
        AssignAttributesResults assignAttributesResults = new AssignAttributesResults(wsAssignAttributesResults);
        assertEquals(2, assignAttributesResults.getAssignAttributeResults().size());
    }

    @Test
    public void attributeDefNamesTest() {
        WsAttributeDefName wsAttributeDefName1 = new WsAttributeDefName();
        WsAttributeDefName wsAttributeDefName2 = new WsAttributeDefName();
        WsAssignAttributesResults wsAssignAttributesResults = new WsAssignAttributesResults();
        wsAssignAttributesResults.setWsAttributeDefNames(new WsAttributeDefName[]{wsAttributeDefName1, wsAttributeDefName2});
        AssignAttributesResults assignAttributesResults = new AssignAttributesResults(wsAssignAttributesResults);
        assertEquals(2, assignAttributesResults.getAttributesResults().size());
    }

}
