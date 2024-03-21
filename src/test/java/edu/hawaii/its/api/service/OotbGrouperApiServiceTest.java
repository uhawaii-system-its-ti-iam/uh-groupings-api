package edu.hawaii.its.api.service;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GetGroupsResults;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.GroupSaveResults;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;
import edu.hawaii.its.api.wrapper.SubjectsResults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
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
    public void testGetGroupsResults() {
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
    public void testGetMembersResult() {
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
    public void testFindGroupsResults() {
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
    public void testGroupSaveResults() {
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
    public void testAssignAttributesResults() {
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
    public void testGetSubjectsSingle() {
        // Set up
        String uhIdentifier = "uhId1";
        SubjectsResults mockSubjectsResults = mock(SubjectsResults.class);

        when(ootbGroupingPropertiesService.getSubjectsResults()).thenReturn(mockSubjectsResults);

        // Execution
        SubjectsResults results = grouperService.getSubjects(uhIdentifier);

        // Verifications
        assertNotNull(results);
        verify(mockSubjectsResults).getSubjectsAfterAssignSubject(uhIdentifier);

    }
    @Test
    public void testGetSubjectsMultiple() {
        // Set up
        List<String> uhIdentifiers = Arrays.asList("uhId1", "uhId2");
        SubjectsResults mockSubjectsResults = mock(SubjectsResults.class);
        when(ootbGroupingPropertiesService.getSubjectsResults()).thenReturn(mockSubjectsResults);

        // Execution
        SubjectsResults results = grouperService.getSubjects(uhIdentifiers);

        // Verifications
        assertNotNull(results);
        verify(mockSubjectsResults).getSubjectsAfterAssignSubjects(uhIdentifiers);

    }

    @Test
    public void testRemoveMember() {
        // Set up
        String currentUser = "user1";
        String groupPath = "group-path-0";
        String uhIdentifier = "uhId1";
        RemoveMemberResult mockRemoveMemberResult = new RemoveMemberResult(); // Assume this is a valid class
        GetMembersResults mockMembers = mock(GetMembersResults.class);

        RemoveMembersResults mockRemoveMembersResults = mock(RemoveMembersResults.class);
        when(mockRemoveMembersResults.getResults()).thenReturn(Collections.singletonList(mockRemoveMemberResult));
        when(ootbGroupingPropertiesService.getRemoveMembersResults()).thenReturn(mockRemoveMembersResults);
        when(ootbGroupingPropertiesService.getMembersResults()).thenReturn(mockMembers);

        // Execution
        RemoveMemberResult result = grouperService.removeMember(currentUser, groupPath, uhIdentifier);

        // Verifications
        assertNotNull(result);
        verify(mockMembers).removeMember(groupPath, uhIdentifier);

    }

    @Test
    public void testRemoveMembers() {
        // Set up
        String currentUser = "user1";
        String groupPath = "groupPath2";
        List<String> uhIdentifiers = Arrays.asList("uhId1", "uhId2");
        RemoveMembersResults mockRemoveMembersResults = new RemoveMembersResults(); // Assume this is a valid class
        GetMembersResults mockMembers = mock(GetMembersResults.class);

        when(ootbGroupingPropertiesService.getRemoveMembersResults()).thenReturn(mockRemoveMembersResults);
        when(ootbGroupingPropertiesService.getMembersResults()).thenReturn(mockMembers);

        // Execution
        RemoveMembersResults results = grouperService.removeMembers(currentUser, groupPath, uhIdentifiers);

        // Verifications
        assertNotNull(results);
        verify(mockMembers).removeMembers(groupPath, uhIdentifiers);
    }

}
