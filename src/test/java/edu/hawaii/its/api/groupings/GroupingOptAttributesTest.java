package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupingOptAttributesTest {

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void constructor() {
        GroupAttributeResults groupAttributeResults =
                groupingsTestConfiguration.getAttributeAssignmentResultsOptInOnOptOutOnTestData();
        GroupingOptAttributes groupingOptAttributes = new GroupingOptAttributes(groupAttributeResults);
        assertNotNull(groupingOptAttributes);
        groupingOptAttributes = new GroupingOptAttributes();
        assertNotNull(groupingOptAttributes);
    }

    @Test
    public void optInOnOptOutOn() {
        GroupAttributeResults groupAttributeResults =
                groupingsTestConfiguration.getAttributeAssignmentResultsOptInOnOptOutOnTestData();
        GroupingOptAttributes groupingOptAttributes = new GroupingOptAttributes(groupAttributeResults);
        assertEquals("SUCCESS", groupingOptAttributes.getResultCode());
        assertNotNull(groupingOptAttributes.getGroupPath());
        assertTrue(groupingOptAttributes.isOptInOn());
        assertTrue(groupingOptAttributes.isOptOutOn());

    }

    @Test
    public void optInOnOptOutOff() {
        GroupAttributeResults groupAttributeResults =
                groupingsTestConfiguration.getAttributeAssignmentResultsOptInOnOptOutOffTestData();
        GroupingOptAttributes groupingOptAttributes = new GroupingOptAttributes(groupAttributeResults);
        assertEquals("SUCCESS", groupingOptAttributes.getResultCode());
        assertNotNull(groupingOptAttributes.getGroupPath());
        assertTrue(groupingOptAttributes.isOptInOn());
        assertFalse(groupingOptAttributes.isOptOutOn());
    }

    @Test
    public void optInOffOptOutOn() {
        GroupAttributeResults groupAttributeResults =
                groupingsTestConfiguration.getAttributeAssignmentResultsOptInOffOptOutOnTestData();
        GroupingOptAttributes groupingOptAttributes = new GroupingOptAttributes(groupAttributeResults);
        assertEquals("SUCCESS", groupingOptAttributes.getResultCode());
        assertNotNull(groupingOptAttributes.getGroupPath());
        assertFalse(groupingOptAttributes.isOptInOn());
        assertTrue(groupingOptAttributes.isOptOutOn());
    }

    @Test
    public void optInOffOptOutOff() {
        GroupAttributeResults groupAttributeResults =
                groupingsTestConfiguration.getAttributeAssignmentResultsOptInOffOptOutOffTestData();
        GroupingOptAttributes groupingOptAttributes = new GroupingOptAttributes(groupAttributeResults);
        assertEquals("SUCCESS", groupingOptAttributes.getResultCode());
        assertNotNull(groupingOptAttributes.getGroupPath());
        assertFalse(groupingOptAttributes.isOptInOn());
        assertFalse(groupingOptAttributes.isOptOutOn());
    }

    @Test
    public void failure() {
        GroupingOptAttributes groupingOptAttributes =
                new GroupingOptAttributes(new GroupAttributeResults(new WsGetAttributeAssignmentsResults()));
        assertEquals("FAILURE", groupingOptAttributes.getResultCode());
        assertEquals("", groupingOptAttributes.getGroupPath());
        assertFalse(groupingOptAttributes.isOptInOn());
        assertFalse(groupingOptAttributes.isOptOutOn());
    }
}
