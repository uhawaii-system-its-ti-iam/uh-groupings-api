package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GetGroupsResults;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;
import edu.hawaii.its.api.wrapper.GroupSaveResults;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;
import edu.hawaii.its.api.wrapper.SubjectsResults;

@SpringBootTest(classes = { SpringBootWebApplication.class })
class OotbGroupingPropertiesServiceTest {

    @Autowired
    private OotbGroupingPropertiesService ootbGroupingPropertiesService;

    @Test
    public void testHasMembersResultsBean() {
        HasMembersResults result = ootbGroupingPropertiesService.getHasMembersResults();
        assertNotNull(result);
    }

    @Test
    public void testFindGroupsResults() {
        FindGroupsResults result = ootbGroupingPropertiesService.getFindGroupsResults();
        assertNotNull(result);
    }

    @Test
    public void testGetSubjectsResults() {
        SubjectsResults result = ootbGroupingPropertiesService.getSubjectsResults();
        assertNotNull(result);
    }

    @Test
    public void testGetGroupSaveResults() {
        GroupSaveResults result = ootbGroupingPropertiesService.getGroupSaveResults();
        assertNotNull(result);
    }

    @Test
    public void testGetAssignAttributesResults() {
        AssignAttributesResults result = ootbGroupingPropertiesService.getAssignAttributesResults();
        assertNotNull(result);
    }

    @Test
    public void testGetMembersResults() {
        GetMembersResults result = ootbGroupingPropertiesService.getMembersResults();
        assertNotNull(result);
    }

    @Test
    public void testGetAddMembersResults() {
        AddMembersResults result = ootbGroupingPropertiesService.getAddMembersResults();
        assertNotNull(result);
    }

    @Test
    public void testGetRemoveMembersResults() {
        RemoveMembersResults result = ootbGroupingPropertiesService.getRemoveMembersResults();
        assertNotNull(result);
    }

    @Test
    public void testGetGroupAttributeResults() {
        GroupAttributeResults result = ootbGroupingPropertiesService.getGroupAttributeResults();
        assertNotNull(result);
    }

    @Test
    public void testGetGroupsResults() {
        GetGroupsResults result = ootbGroupingPropertiesService.getGroupsResults();
        assertNotNull(result);
    }
}
