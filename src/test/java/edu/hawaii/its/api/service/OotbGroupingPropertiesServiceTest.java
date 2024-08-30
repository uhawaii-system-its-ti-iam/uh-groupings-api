package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GetGroupsResults;
import edu.hawaii.its.api.wrapper.GetMembersResult;
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

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

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

    @Test
    public void testAddMembers() {
        AddMembersResults result = ootbGroupingPropertiesService.addMembers("CURRENT_USER", "groupPath", Arrays.asList("uh1234", "admin1234"));
        assertNotNull(result);
    }

    @Test
    public void testRemoveMembers() {
        RemoveMembersResults result = ootbGroupingPropertiesService.removeMembers("CURRENT_USER", "groupPath", Arrays.asList("uh1234", "admin1234"));
        assertNotNull(result);
    }

    @Test
    public void testGetSubject() {
        SubjectsResults result = ootbGroupingPropertiesService.getSubject("userId");
        assertNotNull(result);
    }

    @Test
    public void testGetSubjects() {
        SubjectsResults result = ootbGroupingPropertiesService.getSubjects(Arrays.asList("user1", "user2"));
        assertNotNull(result);
    }

    @Test
    public void testResetGroup() {
        AddMembersResults result = ootbGroupingPropertiesService.resetGroup("groupPath");
        assertNotNull(result);
    }

    @Test
    public void testGetOwnedGroupings() {
        GetMembersResults result =
                ootbGroupingPropertiesService.getOwnedGroupings(Arrays.asList("groupPath1", "groupPath2"));
        assertNotNull(result);
    }

    @Test
    public void testGetGroupAttributeResultsByAttribute() {
        GroupAttributeResults result =
                ootbGroupingPropertiesService.getGroupAttributeResultsByAttribute("is-trio");
        assertNotNull(result);
    }

    @Test
    public void testGetGroupAttributeResultsByAttributeAndGroupPathList() {
        GroupAttributeResults result =
                ootbGroupingPropertiesService.getGroupAttributeResultsByAttributeAndGroupPathList("is-trio",
                        Arrays.asList("groupPath1", "groupPath2"));
        assertNotNull(result);
    }

    @Test
    public void testGetGroupAttributeResultsQuery() {
        GroupAttributeResults result =
                ootbGroupingPropertiesService.getGroupAttributeResults("currentUser", "groupPath");
        assertNotNull(result);
    }

    @Test
    public void testGetGroups() {
        GetGroupsResults result = ootbGroupingPropertiesService.getGroups("userId");
        assertNotNull(result);
    }

    @Test
    public void testGetFindGroups() {
        FindGroupsResults result = ootbGroupingPropertiesService.getFindGroups("groupPath");
        assertNotNull(result);
    }

    @Test
    public void testUpdateDescription() {
        GroupSaveResults result = ootbGroupingPropertiesService.updateDescription("groupPath", "New Description");
        assertNotNull(result);
    }

    @Test
    public void testManageAttributeAssignmentAssign() {
        String currentUser = "CURRENT_USER";
        String groupPath = "testGroup";
        String attributeName = "uh-settings:attributes:for-groups:uh-grouping:anyone-can:opt-in";
        String assignOperation = "assign_attr";

        AssignAttributesResults result =
                ootbGroupingPropertiesService.manageAttributeAssignment(currentUser, groupPath, attributeName, assignOperation);
        assertNotNull(result);
    }

    @Test
    public void testManageAttributeAssignmentRemove() {
        String currentUser = "CURRENT_USER";
        String groupPath = "testGroup";
        String attributeName = "uh-settings:attributes:for-groups:uh-grouping:anyone-can:opt-in";
        String assignOperation = "remove_attr";

        AssignAttributesResults result =
                ootbGroupingPropertiesService.manageAttributeAssignment(currentUser, groupPath, attributeName, assignOperation);
        assertNotNull(result);
    }

    @Test
    public void testWsGetMembersResultsList() {
        WsGetMembersResult[] results = ootbGroupingPropertiesService.wsGetMembersResultsList();
        assertNotNull(results);
    }

    @Test
    public void testGetMembersByGroupPath() {
        GetMembersResult result = ootbGroupingPropertiesService.getMembersByGroupPath("someGroupPath");
        Assertions.assertNull(result);
    }

    @Test
    public void testGetWsSubjectListOfOotbUsers() {
        WsSubject[] subjects = ootbGroupingPropertiesService.getWsSubjectListOfOotbUsers();
        assertNotNull(subjects);
    }

    @Test
    public void testGetWsOotbSubject() {
        WsSubject subject = ootbGroupingPropertiesService.getWsOotbSubject("no identifier");
        Assertions.assertNull(subject);
    }

    @Test
    public void testGetWsOotbSubjects() {
        List<WsSubject> subjects = ootbGroupingPropertiesService.getWsOotbSubjects(Arrays.asList("id1", "id2"));
        assertNotNull(subjects);
    }

    @Test
    public void testIsValidOotbUhIdentifierSingle() {
        Boolean isValid = ootbGroupingPropertiesService.isValidOotbUhIdentifier("someIdentifier");
        assertNotNull(isValid);
    }

    @Test
    public void testIsValidOotbUhIdentifierList() {
        Boolean isValid = ootbGroupingPropertiesService.isValidOotbUhIdentifier(Arrays.asList("id1", "id2"));
        assertNotNull(isValid);
    }

    @Test
    public void testGetWsGroupFromFindGroupsResults() {
        FindGroupsResults findGroupsResults = new FindGroupsResults();
        WsGroup wsGroup = ootbGroupingPropertiesService.getWsGroupFromFindGroupsResults(findGroupsResults, "somePath");
        Assertions.assertNull(wsGroup);
    }

}
