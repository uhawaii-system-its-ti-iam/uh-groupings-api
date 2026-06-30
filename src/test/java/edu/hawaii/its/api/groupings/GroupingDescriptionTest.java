package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.Group;

import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupingDescriptionTest {

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void constructor() {
        FindGroupsResults findGroupsResults =
                groupingsTestConfiguration.findGroupsResultsDescriptionTestData();
        assertNotNull(findGroupsResults);

        GroupingDescription groupingDescription = new GroupingDescription(findGroupsResults.getGroup());
        assertNotNull(groupingDescription);

        groupingDescription = new GroupingDescription();
        assertNotNull(groupingDescription);
    }

    @Test
    public void success() {
        FindGroupsResults findGroupsResults =
                groupingsTestConfiguration.findGroupsResultsDescriptionTestData();
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
