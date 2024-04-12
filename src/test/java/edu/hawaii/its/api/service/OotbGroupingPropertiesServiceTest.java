package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

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

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

@SpringBootTest(classes = { SpringBootWebApplication.class })
class OotbGroupingPropertiesServiceTest {

    @Autowired
    private OotbGroupingPropertiesService ootbGroupingPropertiesService;

    @MockBean
    private GroupAttributeResults groupAttributeResultsMock;

    @MockBean
    private GetGroupsResults getGroupsResultsMock;

    @Test
    public void testHasMembersResultsBean() {
        HasMembersResults result = ootbGroupingPropertiesService.getHasMembersResultsBean();
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

    @Test
    void testRemoveMember() {
        //Set Up
        String currentUser = "testiwta";
        String groupPath = "group-path-0";
        String uhIdentifierToRemove = "user123";
        WsSubject subjectToRemove = new WsSubject();
        subjectToRemove.setId(uhIdentifierToRemove);
        WsGroup wsGroupToRemove = new WsGroup();
        wsGroupToRemove.setName(groupPath);
        WsGetMembersResult initialGroupMember = new WsGetMembersResult();
        initialGroupMember.setWsGroup(wsGroupToRemove);
        initialGroupMember.setWsSubjects(new WsSubject[] { subjectToRemove });

        WsGetMembersResult[] initialMembers = { initialGroupMember };
        ootbGroupingPropertiesService.getMembersResults().getWsGetMembersResults().setResults(initialMembers);

        WsGetGroupsResult wsGetGroupsResult = mock(WsGetGroupsResult.class);
        WsGetGroupsResults wsGetGroupsResults = new WsGetGroupsResults();
        wsGetGroupsResults.setResults(new WsGetGroupsResult[] { wsGetGroupsResult });
        when(wsGetGroupsResult.getWsSubject()).thenReturn(subjectToRemove);
        when(wsGetGroupsResult.getWsGroups()).thenReturn(new WsGroup[] { wsGroupToRemove });
        when(ootbGroupingPropertiesService.getGroupsResults().getWsGetGroupsResults()).thenReturn(
                wsGetGroupsResults);

        // Execution
        ootbGroupingPropertiesService.ootbRemoveMember(currentUser, groupPath, uhIdentifierToRemove);

        // Verification
        boolean isMemberRemoved = Arrays.stream(ootbGroupingPropertiesService.wsGetMembersResultsList())
                .flatMap(result -> Arrays.stream(result.getWsSubjects()))
                .noneMatch(subject -> uhIdentifierToRemove.equals(subject.getId()));

        assertTrue(isMemberRemoved);
    }

    @Test
    void testRemoveMembers() {
        // Setup
        String currentUser = "testiwta";
        String groupPath = "group-path-0";
        List<String> uhIdentifiersToRemove = List.of("user123", "user456");
        WsGroup wsGroup = new WsGroup();
        wsGroup.setName(groupPath);

        WsSubject subjectToRemove1 = new WsSubject();
        subjectToRemove1.setId(uhIdentifiersToRemove.get(0));
        subjectToRemove1.setIdentifierLookup("uh id");
        WsSubject subjectToRemove2 = new WsSubject();
        subjectToRemove2.setId(uhIdentifiersToRemove.get(1));
        subjectToRemove2.setIdentifierLookup("uh id");

        WsGetMembersResult initialGroupMembers = new WsGetMembersResult();
        initialGroupMembers.setWsGroup(wsGroup);
        initialGroupMembers.setWsSubjects(new WsSubject[] { subjectToRemove1, subjectToRemove2 });

        WsGetMembersResult[] initialMembers = { initialGroupMembers };
        ootbGroupingPropertiesService.getMembersResults().getWsGetMembersResults().setResults(initialMembers);

        WsGetGroupsResult wsGetGroupsResult = mock(WsGetGroupsResult.class);
        WsGetGroupsResults wsGetGroupsResults = new WsGetGroupsResults();
        wsGetGroupsResults.setResults(new WsGetGroupsResult[] { wsGetGroupsResult });
        when(wsGetGroupsResult.getWsSubject()).thenReturn(subjectToRemove1);
        when(wsGetGroupsResult.getWsGroups()).thenReturn(new WsGroup[] { wsGroup });
        when(ootbGroupingPropertiesService.getGroupsResults().getWsGetGroupsResults()).thenReturn(
                wsGetGroupsResults);

        // Execution
        ootbGroupingPropertiesService.removeMembers(currentUser, groupPath, uhIdentifiersToRemove);

        // Verification
        boolean areMembersRemoved = Arrays.stream(ootbGroupingPropertiesService.wsGetMembersResultsList())
                .flatMap(result -> Arrays.stream(result.getWsSubjects()))
                .noneMatch(subject -> uhIdentifiersToRemove.contains(subject.getId()));

        assertTrue(areMembersRemoved);
    }

    @Test
    public void testAddMember() {
        // Mock setup
        String currentUser = "testiwta";
        String groupPath = "testGroup";
        String uhIdentifier = "user123";

        WsSubject mockSubject = new WsSubject();
        mockSubject.setId(uhIdentifier);
        mockSubject.setName("Test User");
        mockSubject.setAttributeValues(new String[] { "attr1", "attr2" });
        WsGroup wsGroup = new WsGroup();
        wsGroup.setName("group");

        WsGetMembersResult mockMembersResult = new WsGetMembersResult();
        WsGroup mockGroup = new WsGroup();
        mockGroup.setName("uh-settings:groupingOotbUsers");
        mockMembersResult.setWsGroup(mockGroup);
        mockMembersResult.setWsSubjects(new WsSubject[] { mockSubject });

        WsGetMembersResult[] mockedResult = { mockMembersResult };
        // Assuming the service method calls getMembersResults().getWsGetMembersResults() internally
        ootbGroupingPropertiesService.getMembersResults().getWsGetMembersResults().setResults(mockedResult);

        WsGetGroupsResult wsGetGroupsResult = mock(WsGetGroupsResult.class);
        WsGetGroupsResults wsGetGroupsResults = new WsGetGroupsResults();
        wsGetGroupsResults.setResults(new WsGetGroupsResult[] { wsGetGroupsResult });
        when(wsGetGroupsResult.getWsSubject()).thenReturn(mockSubject);
        when(wsGetGroupsResult.getWsGroups()).thenReturn(new WsGroup[] {});
        when(ootbGroupingPropertiesService.getGroupsResults().getWsGetGroupsResults()).thenReturn(
                wsGetGroupsResults);

        // Execution
        ootbGroupingPropertiesService.addMember(currentUser, groupPath, uhIdentifier);

        // Verification
        boolean isMemberAdded = Arrays.stream(ootbGroupingPropertiesService.wsGetMembersResultsList())
                .flatMap(result -> Arrays.stream(result.getWsSubjects()))
                .allMatch(subject -> uhIdentifier.equals(subject.getId()));

        assertTrue(isMemberAdded);
    }

    @Test
    void testAddMembers() {
        // Set Up
        String currentUser = "testiwta";
        String groupPath = "testGroup";
        List<String> uhIdentifiers = Arrays.asList("user1", "user2");
        WsSubject subject1 = new WsSubject();
        subject1.setId("user1");
        subject1.setName("User One");
        subject1.setAttributeValues(new String[] { "attr1" });
        WsSubject subject2 = new WsSubject();
        subject2.setId("user2");
        subject2.setName("User Two");
        subject2.setAttributeValues(new String[] { "attr2" });
        WsGroup wsGroup = new WsGroup();
        wsGroup.setName("group");

        // Assuming these subjects are part of the "uh-settings:groupingOotbUsers" group
        WsGetMembersResult membersResultForOotbUsers = mock(WsGetMembersResult.class);
        when(membersResultForOotbUsers.getWsSubjects()).thenReturn(new WsSubject[] { subject1, subject2 });
        when(membersResultForOotbUsers.getWsGroup()).thenReturn(new WsGroup());

        WsGetMembersResult[] mockedResult = { membersResultForOotbUsers };
        ootbGroupingPropertiesService.getMembersResults().getWsGetMembersResults().setResults(mockedResult);

        WsGetGroupsResult wsGetGroupsResult = mock(WsGetGroupsResult.class);
        WsGetGroupsResults wsGetGroupsResults = new WsGetGroupsResults();
        wsGetGroupsResults.setResults(new WsGetGroupsResult[] { wsGetGroupsResult });
        when(wsGetGroupsResult.getWsSubject()).thenReturn(subject1);
        when(wsGetGroupsResult.getWsGroups()).thenReturn(new WsGroup[] { wsGroup });
        when(ootbGroupingPropertiesService.getGroupsResults().getWsGetGroupsResults()).thenReturn(
                wsGetGroupsResults);

        // Execution
        ootbGroupingPropertiesService.ootbAddMembers(currentUser, groupPath, uhIdentifiers);

        // Verification
        boolean areMembersAdded = Arrays.stream(ootbGroupingPropertiesService.wsGetMembersResultsList())
                .flatMap(result -> Arrays.stream(result.getWsSubjects()))
                .allMatch(subject -> uhIdentifiers.contains(subject.getId()));

        assertTrue(areMembersAdded);
    }

    @Test
    public void testOptIn() {
        // Setup
        String currentUser = "user123";
        String groupPath = "testGroup:include";
        String uhIdentifier = "user123";

        WsAttributeAssign assign1 = new WsAttributeAssign();
        WsAttributeAssign assign2 = new WsAttributeAssign();
        WsAttributeAssign[] wsAttributeAssigns = { assign1, assign2 };

        WsGroup wsGroup = new WsGroup();
        wsGroup.setName("existed group");

        WsGetGroupsResult wsGetGroupsResult1 = new WsGetGroupsResult();
        wsGetGroupsResult1.setWsGroups(new WsGroup[] { wsGroup });
        WsGetGroupsResult[] wsGetGroupsResults = { wsGetGroupsResult1 };
        WsGetGroupsResults wsGetGroupsResults1 = new WsGetGroupsResults();
        wsGetGroupsResults1.setResults(wsGetGroupsResults);
        WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = new WsGetAttributeAssignmentsResults();
        wsGetAttributeAssignmentsResults.setWsAttributeAssigns(wsAttributeAssigns);

        when(ootbGroupingPropertiesService.getGroupAttributeResults().getWsGetAttributeAssignmentsResults()).thenReturn(
                wsGetAttributeAssignmentsResults);

        when(ootbGroupingPropertiesService.getGroupsResults().getWsGetGroupsResults()).thenReturn(
                wsGetGroupsResults1);

        // Execution
        ootbGroupingPropertiesService.ootbAddMember(currentUser, groupPath, uhIdentifier);

        // Verification
        verify(ootbGroupingPropertiesService.getGroupAttributeResults()).getWsGetAttributeAssignmentsResults(); // Example verification
        boolean hasExpectedGroupPath =
                Arrays.stream(ootbGroupingPropertiesService.getGroupsResults().getWsGetGroupsResults().getResults())
                        .flatMap(result -> Arrays.stream(
                                result.getWsGroups()))
                        .anyMatch(group -> groupPath.equals(
                                group.getName()));

        assertTrue(hasExpectedGroupPath);
    }

    @Test
    public void testOptOut() {
        // Setup
        String currentUser = "user123";
        String groupPath = "testGroup:exclude";
        String uhIdentifier = "user123";

        WsAttributeAssign assign1 = new WsAttributeAssign();
        WsAttributeAssign assign2 = new WsAttributeAssign();
        WsAttributeAssign[] wsAttributeAssigns = { assign1, assign2 };

        WsGroup wsGroup = new WsGroup();
        wsGroup.setName("existed group");

        WsGetGroupsResult wsGetGroupsResult1 = new WsGetGroupsResult();
        wsGetGroupsResult1.setWsGroups(new WsGroup[] { wsGroup });
        WsGetGroupsResult[] wsGetGroupsResults = { wsGetGroupsResult1 };
        WsGetGroupsResults wsGetGroupsResults1 = new WsGetGroupsResults();
        wsGetGroupsResults1.setResults(wsGetGroupsResults);
        WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = new WsGetAttributeAssignmentsResults();
        wsGetAttributeAssignmentsResults.setWsAttributeAssigns(wsAttributeAssigns);

        when(ootbGroupingPropertiesService.getGroupAttributeResults().getWsGetAttributeAssignmentsResults()).thenReturn(
                wsGetAttributeAssignmentsResults);

        when(ootbGroupingPropertiesService.getGroupsResults().getWsGetGroupsResults()).thenReturn(
                wsGetGroupsResults1);

        // Execution
        ootbGroupingPropertiesService.ootbAddMember(currentUser, groupPath, uhIdentifier);

        // Verification
        verify(ootbGroupingPropertiesService.getGroupAttributeResults()).getWsGetAttributeAssignmentsResults(); // Example verification
        boolean hasExpectedGroupPath =
                Arrays.stream(ootbGroupingPropertiesService.getGroupsResults().getWsGetGroupsResults().getResults())
                        .flatMap(result -> Arrays.stream(
                                result.getWsGroups()))
                        .anyMatch(group -> groupPath.equals(
                                group.getName()));

        assertFalse(hasExpectedGroupPath);
    }

}
