package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

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

import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

@SpringBootTest(classes = { SpringBootWebApplication.class })
class OotbGroupingPropertiesServiceTest {

    @Autowired
    private OotbGroupingPropertiesService ootbGroupingPropertiesService;

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

        // Execution
        ootbGroupingPropertiesService.removeMember(groupPath, uhIdentifierToRemove);

        // Verification
        boolean isMemberRemoved = Arrays.stream(ootbGroupingPropertiesService.wsGetMembersResultsList())
                .flatMap(result -> Arrays.stream(result.getWsSubjects()))
                .noneMatch(subject -> uhIdentifierToRemove.equals(subject.getId()));

        assertTrue(isMemberRemoved);
    }

    @Test
    void testRemoveMembers() {
        // Setup
        String groupPath = "group-path-0";
        List<String> uhIdentifiersToRemove = List.of("user123", "user456");
        WsGroup wsGroup = new WsGroup();
        wsGroup.setName(groupPath);

        WsSubject subjectToRemove1 = new WsSubject();
        subjectToRemove1.setId(uhIdentifiersToRemove.get(0));
        WsSubject subjectToRemove2 = new WsSubject();
        subjectToRemove2.setId(uhIdentifiersToRemove.get(1));

        WsGetMembersResult initialGroupMembers = new WsGetMembersResult();
        initialGroupMembers.setWsGroup(wsGroup);
        initialGroupMembers.setWsSubjects(new WsSubject[] { subjectToRemove1, subjectToRemove2 });

        WsGetMembersResult[] initialMembers = { initialGroupMembers };
        ootbGroupingPropertiesService.getMembersResults().getWsGetMembersResults().setResults(initialMembers);

        // Execution
        ootbGroupingPropertiesService.removeMembers(groupPath, uhIdentifiersToRemove);

        // Verification
        boolean areMembersRemoved = Arrays.stream(ootbGroupingPropertiesService.wsGetMembersResultsList())
                .flatMap(result -> Arrays.stream(result.getWsSubjects()))
                .noneMatch(subject -> uhIdentifiersToRemove.contains(subject.getId()));

        assertTrue(areMembersRemoved);
    }

    @Test
    public void testAddMember() {
        // Mock setup
        String groupPath = "testGroup";
        String uhIdentifier = "user123";

        WsSubject mockSubject = new WsSubject();
        mockSubject.setId(uhIdentifier);
        mockSubject.setName("Test User");
        mockSubject.setAttributeValues(new String[] { "attr1", "attr2" });

        WsGetMembersResult mockMembersResult = new WsGetMembersResult();
        WsGroup mockGroup = new WsGroup();
        mockGroup.setName("uh-settings:groupingOotbUsers");
        mockMembersResult.setWsGroup(mockGroup);
        mockMembersResult.setWsSubjects(new WsSubject[] { mockSubject });

        WsGetMembersResult[] mockedResult = { mockMembersResult };
        // Assuming the service method calls getMembersResults().getWsGetMembersResults() internally
        ootbGroupingPropertiesService.getMembersResults().getWsGetMembersResults().setResults(mockedResult);

        // Execution
        ootbGroupingPropertiesService.addMember(groupPath, uhIdentifier);

        // Verification
        boolean isMemberAdded = Arrays.stream(ootbGroupingPropertiesService.wsGetMembersResultsList())
                .flatMap(result -> Arrays.stream(result.getWsSubjects()))
                .allMatch(subject -> uhIdentifier.equals(subject.getId()));

        assertTrue(isMemberAdded);
    }

    @Test
    void testAddMembers() {
        // Set Up
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

        // Assuming these subjects are part of the "uh-settings:groupingOotbUsers" group
        WsGetMembersResult membersResultForOotbUsers = mock(WsGetMembersResult.class);
        when(membersResultForOotbUsers.getWsSubjects()).thenReturn(new WsSubject[] { subject1, subject2 });
        when(membersResultForOotbUsers.getWsGroup()).thenReturn(new WsGroup());

        WsGetMembersResult[] mockedResult = { membersResultForOotbUsers };
        ootbGroupingPropertiesService.getMembersResults().getWsGetMembersResults().setResults(mockedResult);

        // Execution
        ootbGroupingPropertiesService.addMembers(groupPath, uhIdentifiers);

        // Verification
        boolean areMembersAdded = Arrays.stream(ootbGroupingPropertiesService.wsGetMembersResultsList())
                .flatMap(result -> Arrays.stream(result.getWsSubjects()))
                .allMatch(subject -> uhIdentifiers.contains(subject.getId()));

        assertTrue(areMembersAdded);
    }

}
