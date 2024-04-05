package edu.hawaii.its.api.service;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.groupings.MemberResult;
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
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    void testGetSubjectsSingle() {
        // Setup
        String uhIdentifier = "12345";
        SubjectsResults expectedResults = new SubjectsResults(); // Assume this is properly initialized or mocked
        when(ootbGroupingPropertiesService.getSubjectsResults()).thenReturn(expectedResults);

        // Execution
        SubjectsResults result = grouperService.getSubjects(uhIdentifier);

        // Verification
        verify(ootbGroupingPropertiesService).updateSubjectsByUhIdentifier(uhIdentifier);
        verify(ootbGroupingPropertiesService).getSubjectsResults();
        assertEquals(expectedResults, result, "The returned SubjectsResults should match the expected results.");
    }

    @Test
    void testGetSubjectsMultiple() {
        // Setup
        List<String> uhIdentifiers = List.of("12345", "67890");
        SubjectsResults expectedResults = new SubjectsResults();
        when(ootbGroupingPropertiesService.getSubjectsResults()).thenReturn(expectedResults);

        // Execution
        SubjectsResults result = grouperService.getSubjects(uhIdentifiers);

        // Verification
        verify(ootbGroupingPropertiesService).updateSubjectsByUhIdentifiers(uhIdentifiers);
        verify(ootbGroupingPropertiesService).getSubjectsResults();
        assertEquals(expectedResults, result, "The returned SubjectsResults should match the expected results.");
    }


    @Test
    public void testAddMember() {
        // Setup
        String currentUser = "testUser";
        String groupPath = "testGroup";
        String uhIdentifier = "12345";

        AddMembersResults mockResults = mock(AddMembersResults.class);
        List<AddMemberResult> memberResultList = new ArrayList<>();
        AddMemberResult mockMemberResult = new AddMemberResult();
        memberResultList.add(mockMemberResult);

        when(ootbGroupingPropertiesService.getAddMembersResults()).thenReturn(mockResults);
        when(mockResults.getResults()).thenReturn(memberResultList);

        // Execution
        grouperService.addMember(currentUser, groupPath, uhIdentifier);

        // Verification
        verify(ootbGroupingPropertiesService).addMember(eq(groupPath), eq(uhIdentifier));
        verify(mockResults).getResults();
    }

    @Test
    public void testAddMembers() {
        // Setup
        String currentUser = "testUser";
        String groupPath = "testGroup";
        List<String> uhIdentifiers = List.of("12345", "67890");

        // Execution
        grouperService.addMembers(currentUser, groupPath, uhIdentifiers);

        // Verification
        verify(ootbGroupingPropertiesService).addMembers(eq(groupPath), eq(uhIdentifiers));
    }

    @Test
    void testRemoveMember() {
        // Setup
        String currentUser = "testUser";
        String groupPath = "testGroup";
        String uhIdentifier = "12345";

        // Mocking RemoveMembersResults and its method to return a non-empty list
        RemoveMembersResults mockResults = mock(RemoveMembersResults.class);
        List<RemoveMemberResult> memberResultList = new ArrayList<>();
        RemoveMemberResult mockMemberResult = new RemoveMemberResult();
        memberResultList.add(mockMemberResult);

        when(ootbGroupingPropertiesService.getRemoveMembersResults()).thenReturn(mockResults);
        when(mockResults.getResults()).thenReturn(memberResultList);

        // Execution
        grouperService.removeMember(currentUser, groupPath, uhIdentifier);

        // Verification
        verify(ootbGroupingPropertiesService).removeMember(eq(groupPath), eq(uhIdentifier));
        verify(mockResults).getResults();
    }

    @Test
    public void testRemoveMembers() {
        // Setup
        String currentUser = "testUser";
        String groupPath = "testGroup";
        List<String> uhIdentifiers = List.of("12345", "67890");

        // Execution
        grouperService.removeMembers(currentUser, groupPath, uhIdentifiers);

        // Verification
        verify(ootbGroupingPropertiesService).removeMembers(eq(groupPath), eq(uhIdentifiers));
    }



}
