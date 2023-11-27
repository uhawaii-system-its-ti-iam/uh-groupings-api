package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.Group;

import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;

public class GroupingDescriptionTest {

    private PropertyLocator propertyLocator;

    @BeforeEach
    public void beforeEach() {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void constructor() {
        String json = propertyLocator.find("find.groups.results.description");
        WsFindGroupsResults wsFindGroupsResults = JsonUtil.asObject(json, WsFindGroupsResults.class);
        assertNotNull(wsFindGroupsResults);
        FindGroupsResults findGroupsResults = new FindGroupsResults(wsFindGroupsResults);
        assertNotNull(findGroupsResults);
        GroupingDescription groupingDescription = new GroupingDescription(findGroupsResults.getGroup());
        assertNotNull(groupingDescription);
        groupingDescription = new GroupingDescription();
        assertNotNull(groupingDescription);
    }

    @Test
    public void success() {
        String json = propertyLocator.find("find.groups.results.description");
        WsFindGroupsResults wsFindGroupsResults = JsonUtil.asObject(json, WsFindGroupsResults.class);
        FindGroupsResults findGroupsResults = new FindGroupsResults(wsFindGroupsResults);
        GroupingDescription groupingDescription = new GroupingDescription(findGroupsResults.getGroup());
        assertNotNull(groupingDescription);
        assertEquals("SUCCESS", groupingDescription.getResultCode());
        assertEquals("Test Many Groups In Basis", groupingDescription.getDescription());
        assertEquals("tmp:testiwtb:testiwtb-many", groupingDescription.getGroupPath());
    }

    @Test
    public void failure() {
        GroupingDescription groupingDescription = new GroupingDescription(new Group(new WsGroup()));
        assertNotNull(groupingDescription);
        assertEquals("FAILURE", groupingDescription.getResultCode());
        assertEquals("", groupingDescription.getDescription());
        assertEquals("", groupingDescription.getGroupPath());
    }
}
