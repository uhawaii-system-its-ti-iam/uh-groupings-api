package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.List;

import edu.hawaii.its.api.wrapper.GetMembersResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.UhIdentifierNotFoundException;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;
import edu.hawaii.its.api.wrapper.SubjectsResults;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class UpdateMemberServiceTest {

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private UpdateMemberService updateMemberService;

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @MockitoSpyBean
    private GroupingAssignmentService groupingAssignmentService;

    @MockitoSpyBean
    private GrouperService grouperService;

    @MockitoSpyBean
    private GroupingOwnerService groupingOwnerService;

    @MockitoSpyBean
    private UpdateTimestampService updateTimestampService;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.test.uids}")
    private List<String> TEST_UIDS;

    private final String groupPath = "group-path";

    private final String ownerGrouping = "owner-grouping";

    @Test
    public void addAdminTest() {
        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(1));

        SubjectsResults subjectsResults = groupingsTestConfiguration.getSubjectResultSuccessTestData();
        assertNotNull(subjectsResults);
        doReturn(subjectsResults).when(grouperService).getSubjects(TEST_UIDS.get(0));

        AddMemberResult addMemberResult = groupingsTestConfiguration.addMemberResultSuccessTestData();
        assertNotNull(addMemberResult);
        doReturn(addMemberResult).when(grouperService).addMember(TEST_UIDS.get(1), GROUPING_ADMINS, TEST_UIDS.get(0));

        assertNotNull(updateMemberService.addAdminMember(TEST_UIDS.get(1), TEST_UIDS.get(0)));

        subjectsResults = groupingsTestConfiguration.getSubjectResultUidFailureTestData();
        assertNotNull(subjectsResults);
        doReturn(subjectsResults).when(grouperService).getSubjects("bogus-identifier");

        assertThrows(UhIdentifierNotFoundException.class,
                () -> updateMemberService.addAdminMember(TEST_UIDS.get(1), "bogus-identifier"));
    }

    @Test
    public void removeAdminTest() {
        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(1));

        SubjectsResults subjectsResults = groupingsTestConfiguration.getSubjectResultSuccessTestData();
        assertNotNull(subjectsResults);
        doReturn(subjectsResults).when(grouperService).getSubjects(TEST_UIDS.get(0));

        RemoveMemberResult removeMemberResult = groupingsTestConfiguration.deleteMemberResultSuccessTestData();
        assertNotNull(removeMemberResult);
        doReturn(removeMemberResult).when(grouperService)
                .removeMember(TEST_UIDS.get(1), GROUPING_ADMINS, TEST_UIDS.get(0));

        assertNotNull(updateMemberService.removeAdminMember(TEST_UIDS.get(1), TEST_UIDS.get(0)));

        subjectsResults = groupingsTestConfiguration.getSubjectResultUidFailureTestData();
        assertNotNull(subjectsResults);
        doReturn(subjectsResults).when(grouperService).getSubjects("bogus-identifier");

        assertThrows(UhIdentifierNotFoundException.class,
                () -> updateMemberService.removeAdminMember(TEST_UIDS.get(1), "bogus-identifier"));
    }

    @Test
    public void addOwnershipsTest() {
        FindGroupsResults findGroupsResults = groupingsTestConfiguration.findGroupsResultsDescriptionTestData();
        assertNotNull(findGroupsResults);
        doReturn(findGroupsResults).when(grouperService).findGroupsResults(groupPath);

        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        SubjectsResults subjectsResults = groupingsTestConfiguration.getSubjectsResultsSuccessTestData();
        assertNotNull(subjectsResults);
        doReturn(subjectsResults).when(grouperService).getSubjects(TEST_UIDS);

        doReturn(1).when(groupingAssignmentService).numberOfAllOwners(TEST_UIDS.get(0), groupPath);

        AddMembersResults addMembersResults = groupingsTestConfiguration.addMemberResultsFailureTestData();
        assertNotNull(addMembersResults);
        List<String> validIdentifiers = subjectService.getValidUhUuids(TEST_UIDS);
        doReturn(addMembersResults).when(grouperService)
                .addMembers(TEST_UIDS.get(0), groupPath + GroupType.OWNERS.value(), validIdentifiers);

        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath, TEST_UIDS.get(0));
        GetMembersResult getMembersResult = groupingsTestConfiguration.getMembersResultsSuccessTestData().getMembersResults().get(0);
        doReturn(getMembersResult).when(grouperService)
                .getAllMembers(TEST_UIDS.get(0), groupPath);

        doReturn(null).when(updateTimestampService).update(any());

        assertNotNull(updateMemberService.addOwnerships(TEST_UIDS.get(0), groupPath, TEST_UIDS));
    }

    @Test
    public void addOwnerGroupingOwnerships() {
        FindGroupsResults findGroupsResults = groupingsTestConfiguration.findGroupsResultsDescriptionTestData();
        assertNotNull(findGroupsResults);
        doReturn(findGroupsResults).when(grouperService).findGroupsResults(groupPath);

        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        doReturn(1).when(groupingAssignmentService).numberOfAllOwners(TEST_UIDS.get(0), groupPath);
        doReturn(1).when(groupingOwnerService).numberOfGroupingMembers(TEST_UIDS.get(0), ownerGrouping);

        GetMembersResult getMembersResult = groupingsTestConfiguration.getMembersResultsSuccessTestData().getMembersResults().get(0);
        assertNotNull(getMembersResult);
        doReturn(getMembersResult).when(grouperService)
                .getAllMembers(TEST_UIDS.get(0), groupPath);

        AddMembersResults addMembersResults = groupingsTestConfiguration.addMemberResultsSuccessTestData();
        assertNotNull(addMembersResults);
        doReturn(addMembersResults).when(grouperService)
                .addOwnerGroupings(TEST_UIDS.get(0), groupPath + GroupType.OWNERS.value(), List.of(ownerGrouping));

        doReturn(null).when(updateTimestampService).update(any());

        assertNotNull(updateMemberService.addOwnerGroupingOwnerships(TEST_UIDS.get(0), groupPath, List.of(ownerGrouping)));
    }

    @Test
    public void removeOwnerGroupingOwnerships() {
        FindGroupsResults findGroupsResults = groupingsTestConfiguration.findGroupsResultsDescriptionTestData();
        assertNotNull(findGroupsResults);
        doReturn(findGroupsResults).when(grouperService).findGroupsResults(groupPath);

        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        RemoveMembersResults removeMembersResults = groupingsTestConfiguration.deleteMemberResultsSuccessTestData();
        assertNotNull(removeMembersResults);
        doReturn(removeMembersResults).when(grouperService)
                .removeOwnerGroupings(TEST_UIDS.get(0), groupPath + GroupType.OWNERS.value(), List.of(ownerGrouping));

        doReturn(null).when(updateTimestampService).update(any());

        assertNotNull(updateMemberService.removeOwnerGroupingOwnerships(TEST_UIDS.get(0), groupPath, List.of(ownerGrouping)));
    }

    @Test
    public void addOwnershipTest() {
        FindGroupsResults findGroupsResults = groupingsTestConfiguration.findGroupsResultsDescriptionTestData();
        assertNotNull(findGroupsResults);
        doReturn(findGroupsResults).when(grouperService).findGroupsResults(groupPath);

        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        SubjectsResults subjectsResults = groupingsTestConfiguration.getSubjectsResultsSuccessTestData();
        assertNotNull(subjectsResults);
        doReturn(subjectsResults).when(grouperService).getSubjects(TEST_UIDS.get(1));

        AddMemberResult addMemberResult = groupingsTestConfiguration.addMemberResultFailureTestData();
        assertNotNull(addMemberResult);
        String validIdentifier = subjectService.getValidUhUuid(TEST_UIDS.get(1));
        doReturn(addMemberResult).when(grouperService)
                .addMember(TEST_UIDS.get(0), groupPath + GroupType.OWNERS.value(), validIdentifier);

        assertNotNull(updateMemberService.addOwnership(TEST_UIDS.get(0), groupPath, TEST_UIDS.get(1)));
    }

    @Test
    public void removeOwnershipsTest() {
        FindGroupsResults findGroupsResults = groupingsTestConfiguration.findGroupsResultsDescriptionTestData();
        assertNotNull(findGroupsResults);
        doReturn(findGroupsResults).when(grouperService).findGroupsResults(groupPath);

        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults)
                .when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        SubjectsResults subjectsResults = groupingsTestConfiguration.getSubjectsResultsSuccessTestData();
        assertNotNull(subjectsResults);
        doReturn(subjectsResults).when(grouperService).getSubjects(TEST_UIDS);

        doReturn(TEST_UIDS.size() + 1).when(groupingAssignmentService)
                .numberOfDirectOwners(TEST_UIDS.get(0), groupPath);

        RemoveMembersResults removeMembersResults = groupingsTestConfiguration.deleteMemberResultsFailureTestData();
        assertNotNull(removeMembersResults);
        List<String> validIdentifiers = subjectService.getValidUhUuids(TEST_UIDS);
        doReturn(removeMembersResults).when(grouperService)
                .removeMembers(TEST_UIDS.get(0), groupPath + GroupType.OWNERS.value(), validIdentifiers);

        assertNotNull(updateMemberService.removeOwnerships(TEST_UIDS.get(0), groupPath, TEST_UIDS));
    }

    @Test
    public void addIncludeMembersTest() {
        FindGroupsResults findGroupsResults = groupingsTestConfiguration.findGroupsResultsDescriptionTestData();
        assertNotNull(findGroupsResults);
        doReturn(findGroupsResults).when(grouperService).findGroupsResults(groupPath);

        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        SubjectsResults subjectsResults = groupingsTestConfiguration.getSubjectsResultsSuccessTestData();
        doReturn(subjectsResults).when(grouperService).getSubjects(TEST_UIDS);

        List<String> validIdentifiers = subjectService.getValidUhUuids(TEST_UIDS);
        RemoveMembersResults removeMembersResults = groupingsTestConfiguration.deleteMemberResultsFailureTestData();
        doReturn(removeMembersResults).when(grouperService)
                .removeMembers(TEST_UIDS.get(0), groupPath + GroupType.EXCLUDE.value(), validIdentifiers);

        AddMembersResults addMembersResults = groupingsTestConfiguration.addMemberResultsFailureTestData();
        doReturn(addMembersResults).when(grouperService)
                .addMembers(TEST_UIDS.get(0), groupPath + GroupType.INCLUDE.value(), validIdentifiers);

        assertNotNull(updateMemberService.addIncludeMembers(TEST_UIDS.get(0), groupPath, TEST_UIDS));
    }

    @Test
    public void addExcludeMembersTest() {
        FindGroupsResults findGroupsResults = groupingsTestConfiguration.findGroupsResultsDescriptionTestData();
        assertNotNull(findGroupsResults);
        doReturn(findGroupsResults).when(grouperService).findGroupsResults(groupPath);

        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        SubjectsResults subjectsResults = groupingsTestConfiguration.getSubjectsResultsSuccessTestData();
        assertNotNull(subjectsResults);
        doReturn(subjectsResults).when(grouperService).getSubjects(TEST_UIDS);

        List<String> validIdentifiers = subjectService.getValidUhUuids(TEST_UIDS);
        RemoveMembersResults removeMembersResults = groupingsTestConfiguration.deleteMemberResultsFailureTestData();
        doReturn(removeMembersResults).when(grouperService)
                .removeMembers(TEST_UIDS.get(0), groupPath + GroupType.INCLUDE.value(), validIdentifiers);

        AddMembersResults addMembersResults = groupingsTestConfiguration.addMemberResultsFailureTestData();
        doReturn(addMembersResults).when(grouperService)
                .addMembers(TEST_UIDS.get(0), groupPath + GroupType.EXCLUDE.value(), validIdentifiers);

        assertNotNull(updateMemberService.addExcludeMembers(TEST_UIDS.get(0), groupPath, TEST_UIDS));
    }

    @Test
    public void removeIncludeMembersTest() {
        FindGroupsResults findGroupsResults = groupingsTestConfiguration.findGroupsResultsDescriptionTestData();
        assertNotNull(findGroupsResults);
        doReturn(findGroupsResults).when(grouperService).findGroupsResults(groupPath);

        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        RemoveMembersResults removeMembersResults = groupingsTestConfiguration.deleteMemberResultsFailureTestData();
        doReturn(removeMembersResults).when(grouperService)
                .removeMembers(TEST_UIDS.get(0), groupPath + GroupType.INCLUDE.value(), TEST_UIDS);

        assertNotNull(updateMemberService.removeIncludeMembers(TEST_UIDS.get(0), groupPath, TEST_UIDS));
    }

    @Test
    public void removeExcludeMembersTest() {
        FindGroupsResults findGroupsResults = groupingsTestConfiguration.findGroupsResultsDescriptionTestData();
        assertNotNull(findGroupsResults);
        doReturn(findGroupsResults).when(grouperService).findGroupsResults(groupPath);

        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        RemoveMembersResults removeMembersResults = groupingsTestConfiguration.deleteMemberResultsFailureTestData();
        doReturn(removeMembersResults).when(grouperService)
                .removeMembers(TEST_UIDS.get(0), groupPath + GroupType.EXCLUDE.value(), TEST_UIDS);

        assertNotNull(updateMemberService.removeExcludeMembers(TEST_UIDS.get(0), groupPath, TEST_UIDS));
    }

    @Test
    public void removeExcludeMemberTest() {
        FindGroupsResults findGroupsResults = groupingsTestConfiguration.findGroupsResultsDescriptionTestData();
        assertNotNull(findGroupsResults);
        doReturn(findGroupsResults).when(grouperService).findGroupsResults(groupPath);

        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        RemoveMemberResult removeMemberResult = groupingsTestConfiguration.deleteMemberResultFailureTestData();
        assertNotNull(removeMemberResult);
        doReturn(removeMemberResult).when(grouperService)
                .removeMember(TEST_UIDS.get(0), groupPath + GroupType.EXCLUDE.value(), TEST_UIDS.get(1));

        assertNotNull(updateMemberService.removeExcludeMember(TEST_UIDS.get(0), groupPath, TEST_UIDS.get(1)));
    }

    @Test
    public void optInTest() {
        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.INCLUDE.value(), TEST_UIDS.get(1));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        RemoveMemberResult removeMemberResult = groupingsTestConfiguration.deleteMemberResultFailureTestData();
        assertNotNull(removeMemberResult);
        doReturn(removeMemberResult).when(grouperService)
                .removeMember(TEST_UIDS.get(0), groupPath + GroupType.EXCLUDE.value(), TEST_UIDS.get(1));

        AddMemberResult addMemberResult = groupingsTestConfiguration.addMemberResultSuccessTestData();
        assertNotNull(addMemberResult);
        doReturn(addMemberResult).when(grouperService)
                .addMember(TEST_UIDS.get(0), groupPath + GroupType.INCLUDE.value(), TEST_UIDS.get(1));

        GroupAttributeResults groupAttributeResults =
                groupingsTestConfiguration.getAttributeAssignmentResultsSuccessTestData();
        assertNotNull(groupAttributeResults);
        doReturn(groupAttributeResults).when(grouperService).groupAttributeResult(TEST_UIDS.get(1), groupPath);

        assertNotNull(updateMemberService.optIn(TEST_UIDS.get(0), groupPath, TEST_UIDS.get(1)));
    }

    @Test
    public void optOutTest() {
        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.EXCLUDE.value(), TEST_UIDS.get(1));
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.INCLUDE.value(), TEST_UIDS.get(1));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        RemoveMemberResult removeMemberResult = groupingsTestConfiguration.deleteMemberResultFailureTestData();
        assertNotNull(removeMemberResult);
        doReturn(removeMemberResult).when(grouperService)
                .removeMember(TEST_UIDS.get(0), groupPath + GroupType.INCLUDE.value(), TEST_UIDS.get(1));

        AddMemberResult addMemberResult = groupingsTestConfiguration.addMemberResultSuccessTestData();
        assertNotNull(addMemberResult);
        doReturn(addMemberResult).when(grouperService)
                .addMember(TEST_UIDS.get(0), groupPath + GroupType.EXCLUDE.value(), TEST_UIDS.get(1));

        GroupAttributeResults groupAttributeResults =
                groupingsTestConfiguration.getAttributeAssignmentResultsSuccessTestData();
        assertNotNull(groupAttributeResults);
        doReturn(groupAttributeResults).when(grouperService).groupAttributeResult(TEST_UIDS.get(1), groupPath);

        assertNotNull(updateMemberService.optOut(TEST_UIDS.get(0), groupPath, TEST_UIDS.get(1)));
    }
}
