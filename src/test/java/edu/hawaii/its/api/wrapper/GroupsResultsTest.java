package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;

public class GroupsResultsTest {

    private PropertyLocator propertyLocator;

    @BeforeEach
    public void beforeEach() throws Exception {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void construction() {
        String json = propertyLocator.find("groups.results");
        WsGetGroupsResults wsGetGroupsResults = JsonUtil.asObject(json, WsGetGroupsResults.class);
        GroupsResults groupsResults = new GroupsResults(wsGetGroupsResults);
        assertNotNull(wsGetGroupsResults);
        assertNotNull(groupsResults);

        groupsResults = new GroupsResults(null);
        assertNotNull(groupsResults);
    }

    @Test
    public void groupPathsTest() {
        String json = propertyLocator.find("groups.results");
        WsGetGroupsResults wsGetGroupsResults = JsonUtil.asObject(json, WsGetGroupsResults.class);
        GroupsResults groupsResults = new GroupsResults(wsGetGroupsResults);
        List<String> groupPaths = groupsResults.groupPaths();
        assertNotNull(wsGetGroupsResults);
        assertNotNull(groupsResults);
        assertNotNull(groupPaths);
        assertFalse(groupPaths.isEmpty());

        groupsResults = new GroupsResults(null);
        groupPaths = groupsResults.groupPaths();
        assertNotNull(groupPaths);
        assertTrue(groupPaths.isEmpty());

        json = propertyLocator.find("groups.results.empty.results");
        wsGetGroupsResults = JsonUtil.asObject(json, WsGetGroupsResults.class);
        groupsResults = new GroupsResults(wsGetGroupsResults);
        groupPaths = groupsResults.groupPaths();
        assertNotNull(wsGetGroupsResults);
        assertNotNull(groupsResults);
        assertNotNull(groupPaths);
        assertTrue(groupPaths.isEmpty());

        json = propertyLocator.find("groups.results.empty.groups");
        wsGetGroupsResults = JsonUtil.asObject(json, WsGetGroupsResults.class);
        groupsResults = new GroupsResults(wsGetGroupsResults);
        groupPaths = groupsResults.groupPaths();
        assertNotNull(wsGetGroupsResults);
        assertNotNull(groupsResults);
        assertNotNull(groupPaths);
        assertTrue(groupPaths.isEmpty());
    }
}
