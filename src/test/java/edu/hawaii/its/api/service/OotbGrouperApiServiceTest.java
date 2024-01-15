package edu.hawaii.its.api.service;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GetGroupsResults;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.GroupSaveResults;
import edu.hawaii.its.api.wrapper.SubjectsResults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = { SpringBootWebApplication.class }, properties = { "grouping.api.server.type=OOTB" })
public class OotbGrouperApiServiceTest {

    @Autowired
    GrouperService grouperService;

    @MockBean
    private OotbGroupingPropertiesService ootbGroupingPropertiesService;

    @Test
    public void isGrouperApiOOTBService(){
        assertThat(grouperService, notNullValue());
        assertThat( grouperService instanceof OotbGrouperApiService, equalTo(true));
    }

    @Test
    void testGetGroupsResults() {
        // Arrange
        String uhIdentifier = "25555555";
        GetGroupsResults expectedResults = new GetGroupsResults();
        when(ootbGroupingPropertiesService.getGroupsResults()).thenReturn(expectedResults);

        // Act
        GetGroupsResults actualResults = grouperService.getGroupsResults(uhIdentifier);

        // Assert
        assertEquals(expectedResults, actualResults);
    }

    @Test
    void testGetMembersResult() {
        // Setup
        String currentUser = "testiwta";
        List<String> groupPath = new ArrayList<>(Arrays.asList("group-0-1", "group-0-2", "group-0-3"));
        Integer pageNumber = 1;
        Integer pageSize = 700;
        String sortString = "name";
        Boolean isAscending = true;
        GetMembersResults expected = new GetMembersResults();
        when(ootbGroupingPropertiesService.getMembersResults()).thenReturn(expected);

        // Execution
        GetMembersResults actual = grouperService.getMembersResults(currentUser, groupPath, pageNumber, pageSize, sortString, isAscending);

        // Verification
        assertEquals(expected, actual);
        verify(ootbGroupingPropertiesService).getMembersResults();
    }

    @Test
    void testFindGroupsResults() {
        // Arrange
        String groupPath = "group-0-1";
        FindGroupsResults expectedResults = new FindGroupsResults();
        when(ootbGroupingPropertiesService.getFindGroupsResults()).thenReturn(expectedResults);

        // Act
        FindGroupsResults actualResults = grouperService.findGroupsResults(groupPath);

        // Assert
        assertEquals(expectedResults, actualResults);
    }
    @Test
    void testGroupSaveResults() {
        // Arrange
        String groupingPath = "group-0-1";
        String description = "Groupings group";
        GroupSaveResults expected = new GroupSaveResults();
        when(ootbGroupingPropertiesService.getGroupSaveResults()).thenReturn(expected);

        // Act
        GroupSaveResults result = grouperService.groupSaveResults(groupingPath, description);

        // Assert
        assertEquals(expected, result);
        verify(ootbGroupingPropertiesService).getGroupSaveResults();
    }

    @Test
    void testAssignAttributesResults() {
        // Setup
        String currentUser = "testiwta";
        String assignType = "group";
        String assignOperation = "opt-in";
        String groupPath = "group-0-1";
        String attributeName = "attribute1";
        AssignAttributesResults expected = new AssignAttributesResults();
        when(ootbGroupingPropertiesService.getAssignAttributesResults()).thenReturn(expected);

        // Execution
        AssignAttributesResults actual = grouperService.assignAttributesResults(currentUser, assignType, assignOperation, groupPath, attributeName);

        // Verification
        assertEquals(expected, actual);
        verify(ootbGroupingPropertiesService).getAssignAttributesResults();
    }

    // Test for validating UH identifier
    @Test
    void testGetSubjects() {
        // Setup
        String uhIdentifier = "25555555";
        SubjectsResults expectedResults = new SubjectsResults();
        when(ootbGroupingPropertiesService.getSubjectsResults()).thenReturn(expectedResults);

        // Execution
        SubjectsResults actualResults = grouperService.getSubjects(uhIdentifier);

        // Verification
        assertEquals(expectedResults, actualResults);
        verify(ootbGroupingPropertiesService).getSubjectsResults();
    }

}
