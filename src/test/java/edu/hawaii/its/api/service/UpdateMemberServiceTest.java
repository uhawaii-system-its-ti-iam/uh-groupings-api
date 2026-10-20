package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

import edu.hawaii.its.api.wrapper.GetMembersResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.UhIdentifierNotFoundException;
import edu.hawaii.its.api.groupings.GroupingMoveMembersResult;
import edu.hawaii.its.api.type.AsyncJobProgress;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.type.UhIdentifierValidationResult;
import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

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

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.grouper.update-batch-size}")
    private int BATCH_SIZE;

    @Value("${groupings.api.grouper.person-source-id}")
    private String PERSON_SOURCE_ID;

    private final String groupPath = "group-path";

    private final String ownerGrouping = "owner-grouping";

    @BeforeEach
    public void setUpSecurityContext() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                TEST_UIDS.get(0),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

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
        doReturn(subjectsResults).when(grouperService).getSubjects("bogusIdentifier");

        assertThrows(UhIdentifierNotFoundException.class,
                () -> updateMemberService.addAdminMember(TEST_UIDS.get(1), "bogusIdentifier"));
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
        doReturn(subjectsResults).when(grouperService).getSubjects("bogusIdentifier");

        assertThrows(UhIdentifierNotFoundException.class,
                () -> updateMemberService.removeAdminMember(TEST_UIDS.get(1), "bogusIdentifier"));
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
        List<String> validIdentifiers = subjectService.getValidUhUuids(ADMIN, TEST_UIDS);
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
        String validIdentifier = subjectService.getValidUhUuid(ADMIN, TEST_UIDS.get(1));
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
        List<String> validIdentifiers = subjectService.getValidUhUuids(ADMIN, TEST_UIDS);
        doReturn(removeMembersResults).when(grouperService)
                .removeMembers(TEST_UIDS.get(0), groupPath + GroupType.OWNERS.value(), validIdentifiers);

        doReturn(null).when(updateTimestampService).update(any());

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

        // The first identifier is unknown to Grouper and the other three are found.
        List<String> uhIdentifiersToAdd = TEST_UIDS.subList(0, 4);
        SubjectsResults subjectsResults = subjectsResultsWithFirstIdentifierUnknown(uhIdentifiersToAdd);
        stubLookup(uhIdentifiersToAdd, subjectsResults);

        UhIdentifierValidationResult validationResult =
                subjectService.validateUhIdentifiers(ADMIN, uhIdentifiersToAdd);
        List<String> validIdentifiers = validationResult.getValidIdentifiers();
        RemoveMembersResults removeMembersResults = groupingsTestConfiguration.deleteMemberResultsFailureTestData();
        doReturn(removeMembersResults).when(grouperService)
                .removeMembers(eq(TEST_UIDS.get(0)), eq(groupPath + GroupType.EXCLUDE.value()), eq(validIdentifiers), anyMap(), any());

        AddMembersResults addMembersResults = groupingsTestConfiguration.addMemberResultsFailureTestData();
        doReturn(addMembersResults).when(grouperService)
                .addMembers(eq(TEST_UIDS.get(0)), eq(groupPath + GroupType.INCLUDE.value()), eq(validIdentifiers), anyMap(), any());

        doReturn(null).when(updateTimestampService).update(any());

        GroupingMoveMembersResult result =
                updateMemberService.addIncludeMembers(TEST_UIDS.get(0), groupPath, uhIdentifiersToAdd);
        assertNotNull(result);
        assertEquals(validationResult.getInvalidIdentifiers(), result.getInvalidUhIdentifiers());
        assertEquals(1, result.getInvalidUhIdentifiers().size());
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

        // The first identifier is unknown to Grouper and the other three are found.
        List<String> uhIdentifiersToAdd = TEST_UIDS.subList(0, 4);
        SubjectsResults subjectsResults = subjectsResultsWithFirstIdentifierUnknown(uhIdentifiersToAdd);
        assertNotNull(subjectsResults);
        stubLookup(uhIdentifiersToAdd, subjectsResults);

        UhIdentifierValidationResult validationResult =
                subjectService.validateUhIdentifiers(ADMIN, uhIdentifiersToAdd);
        List<String> validIdentifiers = validationResult.getValidIdentifiers();
        RemoveMembersResults removeMembersResults = groupingsTestConfiguration.deleteMemberResultsFailureTestData();
        doReturn(removeMembersResults).when(grouperService)
                .removeMembers(eq(TEST_UIDS.get(0)), eq(groupPath + GroupType.INCLUDE.value()), eq(validIdentifiers), anyMap(), any());

        AddMembersResults addMembersResults = groupingsTestConfiguration.addMemberResultsFailureTestData();
        doReturn(addMembersResults).when(grouperService)
                .addMembers(eq(TEST_UIDS.get(0)), eq(groupPath + GroupType.EXCLUDE.value()), eq(validIdentifiers), anyMap(), any());

        doReturn(null).when(updateTimestampService).update(any());

        GroupingMoveMembersResult result =
                updateMemberService.addExcludeMembers(TEST_UIDS.get(0), groupPath, uhIdentifiersToAdd);
        assertNotNull(result);
        assertEquals(validationResult.getInvalidIdentifiers(), result.getInvalidUhIdentifiers());
        assertEquals(1, result.getInvalidUhIdentifiers().size());
    }

    /**
     * When every submitted identifier is unknown to Grouper, the valid-identifiers list passed down to
     * grouperService.addMembers/removeMembers ends up empty. Grouper's GcAddMember/GcDeleteMember clients
     * reject an empty subject list outright, so this must be short-circuited before reaching them rather
     * than surfacing as an unhandled exception.
     */
    @Test
    public void addIncludeMembersAllInvalidIdentifiersTest() {
        FindGroupsResults findGroupsResults = groupingsTestConfiguration.findGroupsResultsDescriptionTestData();
        assertNotNull(findGroupsResults);
        doReturn(findGroupsResults).when(grouperService).findGroupsResults(groupPath);

        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        // getSubjectsResultsFailureTestData carries 4 subjects, all SUBJECT_NOT_FOUND, so every submitted
        // identifier ends up invalid and the valid-identifiers list handed to grouperService is empty.
        List<String> uhIdentifiersToAdd = TEST_UIDS.subList(0, 4);
        SubjectsResults subjectsResults = groupingsTestConfiguration.getSubjectsResultsFailureTestData();
        stubLookup(uhIdentifiersToAdd, subjectsResults);

        UhIdentifierValidationResult validationResult =
                subjectService.validateUhIdentifiers(ADMIN, uhIdentifiersToAdd);
        assertTrue(validationResult.getValidIdentifiers().isEmpty());
        assertEquals(4, validationResult.getInvalidIdentifiers().size());

        doReturn(null).when(updateTimestampService).update(any());

        // grouperService.addMembers/removeMembers are intentionally left unstubbed here: the spy's real
        // (fixed) implementation must run and short-circuit on the empty list instead of calling Grouper.
        GroupingMoveMembersResult result = assertDoesNotThrow(() ->
                updateMemberService.addIncludeMembers(TEST_UIDS.get(0), groupPath, uhIdentifiersToAdd));
        assertNotNull(result);
        assertEquals(validationResult.getInvalidIdentifiers(), result.getInvalidUhIdentifiers());
        assertEquals(4, result.getInvalidUhIdentifiers().size());
        assertTrue(result.getAddResults().getResults().isEmpty());
        assertTrue(result.getRemoveResults().getResults().isEmpty());
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

        doReturn(null).when(updateTimestampService).update(any());

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

        doReturn(null).when(updateTimestampService).update(any());

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

    @Test
    public void addIncludeMembersRemovesOnlyTheMembersListedInExcludeFromALongList() {
        stubOwnerChecks();
        // More identifiers than fit in one Grouper request, one of them a uid rather than a UH number.
        List<String> uhIdentifiersToAdd = new ArrayList<>(uhNumbers(BATCH_SIZE));
        uhIdentifiersToAdd.add("iamtst01");
        stubLookup(uhIdentifiersToAdd, foundSubjectsResults(uhIdentifiersToAdd));

        String excludePath = groupPath + GroupType.EXCLUDE.value();
        String includePath = groupPath + GroupType.INCLUDE.value();
        List<String> listedInExclude = List.of(uhIdentifiersToAdd.get(0), uhIdentifiersToAdd.get(5));
        doReturn(getMembersResult(excludePath, listedInExclude)).when(grouperService)
                .getImmediateMembers(TEST_UIDS.get(0), excludePath, false);
        // The uid can't be matched to a member id, so it is removed too.
        List<String> membersToRemove = List.of(uhIdentifiersToAdd.get(0), uhIdentifiersToAdd.get(5), "iamtst01");
        doReturn(groupingsTestConfiguration.deleteMemberResultsFailureTestData()).when(grouperService)
                .removeMembers(eq(TEST_UIDS.get(0)), eq(excludePath), eq(membersToRemove), anyMap(), any());
        doReturn(groupingsTestConfiguration.addMemberResultsFailureTestData()).when(grouperService)
                .addMembers(eq(TEST_UIDS.get(0)), eq(includePath), eq(uhIdentifiersToAdd), anyMap(), any());
        doReturn(null).when(updateTimestampService).update(any());

        GroupingMoveMembersResult result =
                updateMemberService.addIncludeMembers(TEST_UIDS.get(0), groupPath, uhIdentifiersToAdd);

        assertNotNull(result);
        assertTrue(result.getInvalidUhIdentifiers().isEmpty());
        verify(grouperService, times(1)).getImmediateMembers(TEST_UIDS.get(0), excludePath, false);
        verify(grouperService).removeMembers(eq(TEST_UIDS.get(0)), eq(excludePath), eq(membersToRemove), anyMap(), any());
        verify(grouperService).addMembers(eq(TEST_UIDS.get(0)), eq(includePath), eq(uhIdentifiersToAdd), anyMap(), any());
    }

    @Test
    public void addExcludeMembersRemovesNothingFromALongListThatIsNotInInclude() {
        stubOwnerChecks();
        List<String> uhIdentifiersToAdd = uhNumbers(BATCH_SIZE + 1);
        stubLookup(uhIdentifiersToAdd, foundSubjectsResults(uhIdentifiersToAdd));

        String includePath = groupPath + GroupType.INCLUDE.value();
        String excludePath = groupPath + GroupType.EXCLUDE.value();
        doReturn(getMembersResult(includePath, List.of("99999999"))).when(grouperService)
                .getImmediateMembers(TEST_UIDS.get(0), includePath, false);
        doReturn(groupingsTestConfiguration.addMemberResultsFailureTestData()).when(grouperService)
                .addMembers(eq(TEST_UIDS.get(0)), eq(excludePath), eq(uhIdentifiersToAdd), anyMap(), any());
        doReturn(null).when(updateTimestampService).update(any());

        // removeMembers is left unstubbed: the spy's real implementation must send nothing for the empty list.
        GroupingMoveMembersResult result =
                updateMemberService.addExcludeMembers(TEST_UIDS.get(0), groupPath, uhIdentifiersToAdd);

        assertNotNull(result);
        verify(grouperService).removeMembers(eq(TEST_UIDS.get(0)), eq(includePath), eq(List.of()), anyMap(), any());
        assertTrue(result.getRemoveResults().getResults().isEmpty());
    }

    @Test
    public void addIncludeMembersRemovesAListThatFitsInOneRequestWithoutCheckingExclude() {
        stubOwnerChecks();
        List<String> uhIdentifiersToAdd = uhNumbers(BATCH_SIZE);
        stubLookup(uhIdentifiersToAdd, foundSubjectsResults(uhIdentifiersToAdd));

        String excludePath = groupPath + GroupType.EXCLUDE.value();
        String includePath = groupPath + GroupType.INCLUDE.value();
        doReturn(groupingsTestConfiguration.deleteMemberResultsFailureTestData()).when(grouperService)
                .removeMembers(eq(TEST_UIDS.get(0)), eq(excludePath), eq(uhIdentifiersToAdd), anyMap(), any());
        doReturn(groupingsTestConfiguration.addMemberResultsFailureTestData()).when(grouperService)
                .addMembers(eq(TEST_UIDS.get(0)), eq(includePath), eq(uhIdentifiersToAdd), anyMap(), any());
        doReturn(null).when(updateTimestampService).update(any());

        assertNotNull(updateMemberService.addIncludeMembers(TEST_UIDS.get(0), groupPath, uhIdentifiersToAdd));

        verify(grouperService, never()).getImmediateMembers(any(), any(), anyBoolean());
        verify(grouperService).removeMembers(eq(TEST_UIDS.get(0)), eq(excludePath), eq(uhIdentifiersToAdd), anyMap(), any());
    }

    @Test
    public void addIncludeMembersAddsAndRemovesEachMemberWithItsSubjectSource() {
        stubOwnerChecks();
        List<String> uhIdentifiersToAdd = List.of("10000001", "10000002");
        SubjectsResults found = foundSubjectsResults(uhIdentifiersToAdd);
        for (WsSubject subject : found.getWsGetSubjectsResults().getWsSubjects()) {
            subject.setSourceId(PERSON_SOURCE_ID);
        }
        stubLookup(uhIdentifiersToAdd, found);
        Map<String, String> sourceIds = Map.of("10000001", PERSON_SOURCE_ID, "10000002", PERSON_SOURCE_ID);
        String includePath = groupPath + GroupType.INCLUDE.value();
        String excludePath = groupPath + GroupType.EXCLUDE.value();
        doReturn(groupingsTestConfiguration.deleteMemberResultsFailureTestData()).when(grouperService)
                .removeMembers(eq(TEST_UIDS.get(0)), eq(excludePath), eq(uhIdentifiersToAdd), eq(sourceIds), any());
        doReturn(groupingsTestConfiguration.addMemberResultsFailureTestData()).when(grouperService)
                .addMembers(eq(TEST_UIDS.get(0)), eq(includePath), eq(uhIdentifiersToAdd), eq(sourceIds), any());
        doReturn(null).when(updateTimestampService).update(any());

        updateMemberService.addIncludeMembers(TEST_UIDS.get(0), groupPath, uhIdentifiersToAdd);

        verify(grouperService).getSubjects(eq(uhIdentifiersToAdd), eq(PERSON_SOURCE_ID), any());
        verify(grouperService).removeMembers(eq(TEST_UIDS.get(0)), eq(excludePath), eq(uhIdentifiersToAdd),
                eq(sourceIds), any());
        verify(grouperService).addMembers(eq(TEST_UIDS.get(0)), eq(includePath), eq(uhIdentifiersToAdd),
                eq(sourceIds), any());
    }

    @Test
    public void addIncludeMembersAsyncRunsOnTheAsyncExecutorAndReportsEachPhase() {
        stubOwnerChecks();
        // A long list, one member of which is listed in Exclude, so the move removes one member, then adds them all.
        List<String> uhIdentifiersToAdd = uhNumbers(BATCH_SIZE + 1);
        stubLookup(uhIdentifiersToAdd, foundSubjectsResults(uhIdentifiersToAdd));
        String includePath = groupPath + GroupType.INCLUDE.value();
        String excludePath = groupPath + GroupType.EXCLUDE.value();
        doReturn(getMembersResult(excludePath, List.of(uhIdentifiersToAdd.get(0)))).when(grouperService)
                .getImmediateMembers(TEST_UIDS.get(0), excludePath, false);
        AsyncJobProgress progress = new AsyncJobProgress();
        List<AsyncJobProgress.Snapshot> phases = new ArrayList<>();
        List<String> threads = new ArrayList<>();
        doAnswer(invocation -> {
            phases.add(progress.snapshot());
            invocation.<IntConsumer>getArgument(4).accept(1);
            return groupingsTestConfiguration.deleteMemberResultsFailureTestData();
        }).when(grouperService).removeMembers(eq(TEST_UIDS.get(0)), eq(excludePath), any(), anyMap(), any());
        doAnswer(invocation -> {
            phases.add(progress.snapshot());
            threads.add(Thread.currentThread().getName());
            invocation.<IntConsumer>getArgument(4).accept(uhIdentifiersToAdd.size());
            return groupingsTestConfiguration.addMemberResultsFailureTestData();
        }).when(grouperService).addMembers(eq(TEST_UIDS.get(0)), eq(includePath), any(), anyMap(), any());
        doReturn(null).when(updateTimestampService).update(any());

        GroupingMoveMembersResult result = updateMemberService
                .addIncludeMembersAsync(TEST_UIDS.get(0), groupPath, uhIdentifiersToAdd, progress).join();

        assertNotNull(result);
        // The move runs on the thread the async executor gave the job, not on the JVM's shared common pool.
        assertTrue(threads.get(0).startsWith("async-thread-"), threads.get(0));
        assertEquals(List.of(AsyncJobProgress.Phase.REMOVING, AsyncJobProgress.Phase.ADDING),
                phases.stream().map(AsyncJobProgress.Snapshot::getPhase).toList());
        assertEquals(1, phases.get(0).getTotal());
        assertEquals(uhIdentifiersToAdd.size(), phases.get(1).getTotal());
        AsyncJobProgress.Snapshot done = progress.snapshot();
        assertEquals(AsyncJobProgress.Phase.ADDING, done.getPhase());
        assertEquals(uhIdentifiersToAdd.size(), done.getDone());
    }

    /**
     * Makes the validation lookup of uhIdentifiers in the subject source of UH people answer with subjectsResults, and
     * the lookup in every source of whatever that leaves unresolved find nothing.
     */
    private void stubLookup(List<String> uhIdentifiers, SubjectsResults subjectsResults) {
        doReturn(subjectsResults).when(grouperService).getSubjects(eq(uhIdentifiers), eq(PERSON_SOURCE_ID), any());
        WsSubject notFound = new WsSubject();
        notFound.setResultCode("SUBJECT_NOT_FOUND");
        WsResultMeta resultMetadata = new WsResultMeta();
        resultMetadata.setResultCode("SUCCESS");
        WsGetSubjectsResults nothingFound = new WsGetSubjectsResults();
        nothingFound.setResultMetadata(resultMetadata);
        nothingFound.setWsSubjects(new WsSubject[] { notFound });
        doReturn(new SubjectsResults(nothingFound)).when(grouperService).getSubjects(anyList(), isNull(), any());
    }

    /**
     * Makes TEST_UIDS.get(0) an owner of the grouping at groupPath.
     */
    private void stubOwnerChecks() {
        doReturn(groupingsTestConfiguration.findGroupsResultsDescriptionTestData()).when(grouperService)
                .findGroupsResults(groupPath);
        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));
    }

    private List<String> uhNumbers(int count) {
        List<String> uhNumbers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            uhNumbers.add(String.format("1%07d", i));
        }
        return uhNumbers;
    }

    /**
     * The answer real Grouper gives to a bulk lookup in which every identifier is found: a UH number is found as the
     * subject with that id, and a uid as a subject carrying that uid (here without an id).
     */
    private SubjectsResults foundSubjectsResults(List<String> uhIdentifiers) {
        List<WsSubject> wsSubjects = new ArrayList<>();
        for (String uhIdentifier : uhIdentifiers) {
            boolean isUhNumber = uhIdentifier.matches("^\\d{8}$");
            WsSubject subject = new WsSubject();
            subject.setResultCode("SUCCESS");
            subject.setId(isUhNumber ? uhIdentifier : null);
            String uid = isUhNumber ? "uid" + uhIdentifier : uhIdentifier;
            subject.setAttributeValues(new String[] { uid, "name-" + uhIdentifier, "Last", "First", "email" });
            wsSubjects.add(subject);
        }

        WsResultMeta resultMetadata = new WsResultMeta();
        resultMetadata.setResultCode("SUCCESS");
        WsGetSubjectsResults wsGetSubjectsResults = new WsGetSubjectsResults();
        wsGetSubjectsResults.setResultMetadata(resultMetadata);
        wsGetSubjectsResults.setWsSubjects(wsSubjects.toArray(new WsSubject[0]));
        return new SubjectsResults(wsGetSubjectsResults);
    }

    /**
     * The members of the group at path listed by subject id only, as Grouper lists them without subject details.
     */
    private GetMembersResult getMembersResult(String path, List<String> uhUuids) {
        WsGroup wsGroup = new WsGroup();
        wsGroup.setName(path);
        WsGetMembersResult wsGetMembersResult = new WsGetMembersResult();
        wsGetMembersResult.setWsGroup(wsGroup);
        wsGetMembersResult.setWsSubjects(uhUuids.stream().map(uhUuid -> {
            WsSubject subject = new WsSubject();
            subject.setResultCode("SUCCESS");
            subject.setId(uhUuid);
            return subject;
        }).toArray(WsSubject[]::new));
        return new GetMembersResult(wsGetMembersResult);
    }

    /**
     * The answer real Grouper gives to a bulk lookup in which the first identifier is unknown and the others are found:
     * one SUBJECT_NOT_FOUND entry, plus a found subject (uhUuid "uhuuid-N") carrying each of the other uids.
     */
    private SubjectsResults subjectsResultsWithFirstIdentifierUnknown(List<String> uids) {
        List<WsSubject> wsSubjects = new ArrayList<>();
        WsSubject notFound = new WsSubject();
        notFound.setResultCode("SUBJECT_NOT_FOUND");
        wsSubjects.add(notFound);
        for (int i = 1; i < uids.size(); i++) {
            WsSubject subject = new WsSubject();
            subject.setResultCode("SUCCESS");
            subject.setId("uhuuid-" + i);
            subject.setAttributeValues(new String[] { uids.get(i), "name-" + i, "Last", "First", "email" });
            wsSubjects.add(subject);
        }

        WsResultMeta resultMetadata = new WsResultMeta();
        resultMetadata.setResultCode("SUCCESS");
        WsGetSubjectsResults wsGetSubjectsResults = new WsGetSubjectsResults();
        wsGetSubjectsResults.setResultMetadata(resultMetadata);
        wsGetSubjectsResults.setWsSubjects(wsSubjects.toArray(new WsSubject[0]));
        return new SubjectsResults(wsGetSubjectsResults);
    }
}
