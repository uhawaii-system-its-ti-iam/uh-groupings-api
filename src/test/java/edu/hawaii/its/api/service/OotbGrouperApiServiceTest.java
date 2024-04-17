package edu.hawaii.its.api.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GetGroupsResults;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.GroupSaveResults;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;
import edu.hawaii.its.api.wrapper.SubjectsResults;

@SpringBootTest(classes = { SpringBootWebApplication.class }, properties = { "grouping.api.server.type=OOTB" })
public class OotbGrouperApiServiceTest {

    @Autowired
    GrouperService grouperService;

    @MockBean
    private OotbGroupingPropertiesService ootbGroupingPropertiesService;

    @Test
    public void isGrouperApiOOTBService() {
        assertThat(grouperService, notNullValue());
        assertThat(grouperService instanceof OotbGrouperApiService, equalTo(true));
    }

    @Test
    public void testGetGroupsResults() {
        // Arrange
        String uhIdentifier = "88888887";
        GetGroupsResults expectedResults = new GetGroupsResults();
        when(ootbGroupingPropertiesService.getGroups(uhIdentifier)).thenReturn(expectedResults);

        // Act
        GetGroupsResults actualResults = grouperService.getGroupsResults(uhIdentifier);

        // Assert
        assertEquals(expectedResults, actualResults);
    }

    @Test
    public void testGetMembersResult() {
        // Setup
        String currentUser = "testiwta";
        List<String> groupPaths = new ArrayList<>(Arrays.asList("group-0-1", "group-0-2", "group-0-3"));
        Integer pageNumber = 1;
        Integer pageSize = 700;
        String sortString = "name";
        Boolean isAscending = true;
        GetMembersResults expected = new GetMembersResults();
        when(ootbGroupingPropertiesService.getMembersResults()).thenReturn(expected);

        // Execution
        GetMembersResults actual =
                grouperService.getMembersResults(groupPaths);

        // Verification
        assertEquals(expected, actual);
        verify(ootbGroupingPropertiesService).getMembersResults();
    }

    @Test
    public void testFindGroupsResults() {
        // Arrange
        String groupPath = "membership1";
        FindGroupsResults expectedResults = new FindGroupsResults();
        when(ootbGroupingPropertiesService.getFindGroups(groupPath)).thenReturn(expectedResults);

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
        AssignAttributesResults actual =
                grouperService.assignAttributesResults(currentUser, assignType, assignOperation, groupPath,
                        attributeName);

        // Verification
        assertEquals(expected, actual);
        verify(ootbGroupingPropertiesService).getAssignAttributesResults();
    }

    // Test for validating UH identifier
    @Test
    void testGetSubjectsSingle() {
        // Setup
        String uhIdentifier = "12345";
        SubjectsResults expectedResults = new SubjectsResults(); // Properly initialized or mocked
        when(ootbGroupingPropertiesService.getSubject(uhIdentifier)).thenReturn(expectedResults);

        // Execution
        SubjectsResults result = grouperService.getSubjects(uhIdentifier);

        // Verification
        verify(ootbGroupingPropertiesService).getSubject(uhIdentifier);
        assertEquals(expectedResults, result, "The returned SubjectsResults should match the expected results.");
    }

    @Test
    void testGetSubjectsMultiple() {
        // Setup
        List<String> uhIdentifiers = List.of("12345", "67890");
        SubjectsResults expectedResults = new SubjectsResults(); // Mock or use a real instance
        when(ootbGroupingPropertiesService.getSubjects(uhIdentifiers)).thenReturn(expectedResults);

        // Execution
        SubjectsResults result = grouperService.getSubjects(uhIdentifiers);

        // Verification
        verify(ootbGroupingPropertiesService).getSubjects(uhIdentifiers);
        assertEquals(expectedResults, result, "The returned SubjectsResults should match the expected results.");
    }

    @Test
    public void testAddMember() {
        // Setup
        String currentUser = "testUser";
        String groupPath = "testGroup";
        String uhIdentifier = "12345";
        AddMemberResult expected = new AddMemberResult(); // Assume this is properly initialized
        when(ootbGroupingPropertiesService.addMember(currentUser, groupPath, uhIdentifier)).thenReturn(expected);

        // Execution
        AddMemberResult result = grouperService.addMember(currentUser, groupPath, uhIdentifier);

        // Verification
        verify(ootbGroupingPropertiesService).addMember(currentUser, groupPath, uhIdentifier);
        assertEquals(expected, result, "The returned AddMemberResult should match the expected result.");
    }

    @Test
    public void testAddMembers() {
        // Setup
        String currentUser = "testUser";
        String groupPath = "testGroup";
        List<String> uhIdentifiers = List.of("12345", "67890");
        AddMembersResults expected = new AddMembersResults(); // Assume this is properly initialized
        when(ootbGroupingPropertiesService.addMembers(currentUser, groupPath, uhIdentifiers)).thenReturn(expected);

        // Execution
        AddMembersResults result = grouperService.addMembers(currentUser, groupPath, uhIdentifiers);

        // Verification
        verify(ootbGroupingPropertiesService).addMembers(currentUser, groupPath, uhIdentifiers);
        assertEquals(expected, result, "The returned AddMembersResults should match the expected results.");
    }

    @Test
    void testRemoveMember() {
        // Setup
        String currentUser = "testUser";
        String groupPath = "testGroup";
        String uhIdentifier = "12345";
        RemoveMemberResult expected = new RemoveMemberResult(); // Assume properly initialized
        when(ootbGroupingPropertiesService.removeMember(currentUser, groupPath, uhIdentifier)).thenReturn(expected);

        // Execution
        RemoveMemberResult result = grouperService.removeMember(currentUser, groupPath, uhIdentifier);

        // Verification
        verify(ootbGroupingPropertiesService).removeMember(currentUser, groupPath, uhIdentifier);
        assertEquals(expected, result, "The returned RemoveMemberResult should match the expected result.");
    }

    @Test
    public void testRemoveMembers() {
        // Setup
        String currentUser = "testUser";
        String groupPath = "testGroup";
        List<String> uhIdentifiers = List.of("12345", "67890");
        RemoveMembersResults expected = new RemoveMembersResults(); // Assume properly initialized
        when(ootbGroupingPropertiesService.removeMembers(currentUser, groupPath, uhIdentifiers)).thenReturn(expected);

        // Execution
        RemoveMembersResults result = grouperService.removeMembers(currentUser, groupPath, uhIdentifiers);

        // Verification
        verify(ootbGroupingPropertiesService).removeMembers(currentUser, groupPath, uhIdentifiers);
        assertEquals(expected, result, "The returned RemoveMembersResults should match the expected results.");
    }

}
