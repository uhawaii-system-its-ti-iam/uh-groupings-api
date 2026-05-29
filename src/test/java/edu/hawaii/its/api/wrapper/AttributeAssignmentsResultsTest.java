package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.OptType;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class AttributeAssignmentsResultsTest {

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void construction() {
        AttributeAssignmentsResults results = groupingsTestConfiguration.attributeAssignmentOptInResultTestData();
        assertNotNull(results);
        assertEquals(results.getResultCode(), "SUCCESS");
        assertNotNull(results.getAttributeDefName());
    }

    @Test
    public void nullConstruction() {
        AttributeAssignmentsResults results = new AttributeAssignmentsResults(null);
        assertNotNull(results);
        assertNull(results.getAttributeDefName());
        assertFalse(results.isOptInOn());
        assertFalse(results.isOptOutOn());
        assertEquals(results.getResultCode(), null);
    }

    @Test
    public void getOwnerGroupNamesTest() {
        AttributeAssignmentsResults results = groupingsTestConfiguration.attributeAssignmentOptInResultTestData();
        assertNotNull(results);

        List<String> ownerGroupNames = results.getOwnerGroupNames();
        assertNotNull(ownerGroupNames);

        assertEquals(1, ownerGroupNames.size());
        assertEquals("tmp:grouping-path:grouping-path-many", ownerGroupNames.get(0));

        Set<String> ownerGroupNamesSet = new HashSet<>();
        for (String name : ownerGroupNames) {
            assertTrue(ownerGroupNamesSet.add(name));
        }

        results = new AttributeAssignmentsResults(null);
        assertTrue(results.getOwnerGroupNames().isEmpty());

    }

    @Test
    public void isAttributeDefNameTest() {
        AttributeAssignmentsResults results = groupingsTestConfiguration.attributeAssignmentOptInResultTestData();
        assertNotNull(results);
        assertTrue(results.isAttributeDefName(OptType.IN.value()));
        assertFalse(results.isAttributeDefName(OptType.OUT.value()));

        results = new AttributeAssignmentsResults(null);
        assertFalse(results.isAttributeDefName(OptType.IN.value()));

        results = groupingsTestConfiguration.attributeAssignmentOptOutResultTestData();
        assertNotNull(results);
        assertTrue(results.isAttributeDefName(OptType.OUT.value()));
        assertFalse(results.isAttributeDefName(OptType.IN.value()));

        results = groupingsTestConfiguration.attributeAssignmentEmptyResultTestData();
        assertNotNull(results);
        assertFalse(results.isAttributeDefName(OptType.OUT.value()));
        assertFalse(results.isAttributeDefName(OptType.IN.value()));
    }

    @Test
    public void getGroupNamesTest() {
        AttributeAssignmentsResults results = groupingsTestConfiguration.attributeAssignmentOptInResultTestData();
        assertNotNull(results);

        List<String> groupNames = results.getGroupNames();
        assertNotNull(groupNames);
        assertFalse(groupNames.isEmpty());
        assertEquals(1, groupNames.size());
        assertEquals("tmp:grouping-path:grouping-path-many", groupNames.get(0));

        results = new AttributeAssignmentsResults(null);
        groupNames = results.getGroupNames();
        assertNotNull(groupNames);
        assertTrue(groupNames.isEmpty());
    }

    @Test
    public void getGroupNamesAndDescriptionsTest() {
        AttributeAssignmentsResults results = groupingsTestConfiguration.attributeAssignmentOptInResultTestData();
        assertNotNull(results);

        List<GroupingPath> groupNamesAndDescriptions = results.getGroupNamesAndDescriptions();
        assertNotNull(groupNamesAndDescriptions);
        assertFalse(groupNamesAndDescriptions.isEmpty());
        assertEquals(1, groupNamesAndDescriptions.size());
        GroupingPath groupingPathObj =
                new GroupingPath("tmp:grouping-path:grouping-path-many", "Test Many Groups In Basis");
        assertEquals(groupingPathObj.getName(), groupNamesAndDescriptions.get(0).getName());
        assertEquals(groupingPathObj.getDescription(), groupNamesAndDescriptions.get(0).getDescription());

        results = new AttributeAssignmentsResults(null);
        groupNamesAndDescriptions = results.getGroupNamesAndDescriptions();
        assertNotNull(groupNamesAndDescriptions);
        assertTrue(groupNamesAndDescriptions.isEmpty());
    }
}
