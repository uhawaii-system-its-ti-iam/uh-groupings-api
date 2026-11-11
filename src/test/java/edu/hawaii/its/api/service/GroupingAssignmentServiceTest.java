package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.groupings.GroupingPaths;
import edu.hawaii.its.api.groupings.OwnerResult;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

@ActiveProfiles("localTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
class GroupingAssignmentServiceTest {

    private static final String PATH_ROOT = "path:to:grouping";
    private static final String GROUPING_0_PATH = PATH_ROOT + 0;

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    @MockitoBean
    private GrouperService grouperService;

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @MockitoSpyBean
    private MemberService memberService;

    @MockitoSpyBean
    private GroupingsService groupingsService;

    @Value("${groupings.api.test.uids}")
    private List<String> TEST_UIDS;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    private final String groupPath = "group-path";

    @Test
    public void construction() {
        assertNotNull(groupingAssignmentService);
        assertNotNull(grouperService);
    }

    @Test
    public void allGroupingPathsTest() {

        GroupAttributeResults groupAttributeResults =
                groupingsTestConfiguration.getAttributeAssignmentResultsSuccessTestData();

        assertNotNull(groupAttributeResults);

        // an admin should be allowed to fetch all grouping paths.
        doReturn(groupAttributeResults)
                .when(groupingsService)
                .allGroupAttributeResults();

        doReturn(true)
                .when(memberService)
                .isCurrentUserAdmin();

        assertNotNull(
                groupingAssignmentService.allGroupingPaths());

        // a non-admin should trigger an access-denied exception.
        doReturn(false)
                .when(memberService)
                .isCurrentUserAdmin();

        assertThrows(
                AccessDeniedException.class,
                () -> groupingAssignmentService.allGroupingPaths());
    }

    @Test
    public void groupingAdminsTest() {

        // an admin should be able to retrieve grouping admins.
        doReturn(true)
                .when(memberService)
                .isCurrentUserAdmin();

        GetMembersResult getMembersResult =
                groupingsTestConfiguration
                        .getMembersResultsSuccessTestData()
                        .getMembersResults()
                        .get(0);

        assertNotNull(getMembersResult);

        doReturn(getMembersResult)
                .when(grouperService)
                .getMembersResult(ADMIN, GROUPING_ADMINS);

        assertNotNull(
                groupingAssignmentService.groupingAdmins(ADMIN));

        // a non-admin should not be allowed to view grouping admins.
        doReturn(false)
                .when(memberService)
                .isCurrentUserAdmin();

        assertThrows(
                AccessDeniedException.class,
                () -> groupingAssignmentService.groupingAdmins(ADMIN));

    }

    @Test
    public void groupingImmediateOwnersTest() {

        // the service should delegate to Grouper for immediate owners and return a non-null result.
        GetMembersResult getMembersResult =
                groupingsTestConfiguration
                        .getMembersResultsSuccessTestData()
                        .getMembersResults()
                        .get(2);

        assertNotNull(getMembersResult);

        doReturn(getMembersResult)
                .when(grouperService)
                .getImmediateMembers(
                        ADMIN,
                        groupPath + GroupType.OWNERS.value());

        assertNotNull(
                groupingAssignmentService.groupingImmediateOwners(
                        ADMIN,
                        groupPath));
    }

    @Test
    public void groupingAllOwnersTest() {

        // the service should delegate to Grouper for all owners and return a non-null result.
        GetMembersResult getMembersResult =
                groupingsTestConfiguration
                        .getMembersResultsSuccessTestData()
                        .getMembersResults()
                        .get(2);

        assertNotNull(getMembersResult);

        doReturn(getMembersResult)
                .when(grouperService)
                .getAllMembers(
                        ADMIN,
                        groupPath + GroupType.OWNERS.value());

        assertNotNull(
                groupingAssignmentService.groupingAllOwners(
                        ADMIN,
                        groupPath));
    }

    @Test
    public void optOutGroupingPathsTest() {

        // the user is already in an include subgroup, so the parent grouping should be considered.
        doReturn(new ArrayList<>(List.of(groupPath + GroupType.INCLUDE.value())))
                .when(groupingsService)
                .groupPaths(eq(TEST_UIDS.get(0)), any());

        // only the parent grouping and another grouping are opt-out enabled.
        doReturn(new ArrayList<>(List.of(groupPath, groupPath + ":other")))
                .when(groupingsService)
                .optOutEnabledGroupingPaths();

        // the resulting opt-out candidates should be reduced to the parent grouping path.
        assertEquals(
                List.of(groupPath),
                groupingAssignmentService.optOutGroupingsPaths(
                        ADMIN,
                        TEST_UIDS.get(0)));
    }

    @Test
    public void optInGroupingPathsTest() {

        // the user is already in the include subgroup, so the parent grouping should be excluded from opt-in results.
        doReturn(new ArrayList<>(List.of(groupPath + GroupType.INCLUDE.value())))
                .when(groupingsService).groupPaths(eq(TEST_UIDS.get(0)), any());

        // opt-in enabled groupings includes the parent and another grouping.
        doReturn(new ArrayList<>(List.of(groupPath, groupPath + ":other")))
                .when(groupingsService).optInEnabledGroupingPaths();

        // when service is asked to fetch grouping metadata for remaining opt-in paths, return metadata for ":other".
        doReturn(List.of(new GroupingPath(groupPath + ":other", "desc")))
                .when(groupingsService).getGroupingPaths(new ArrayList<>(List.of(groupPath + ":other")));

        GroupingPaths result = groupingAssignmentService.optInGroupingPaths(ADMIN, TEST_UIDS.get(0));
        assertNotNull(result);
        assertEquals(1, result.getGroupingPaths().size());
        assertEquals(groupPath + ":other", result.getGroupingPaths().get(0).getPath());
    }

    @Test
    public void numberOfAllOwnersTest() {

        // when the current user is an owner and admin, counting all owners should succeed.
        doReturn(true)
                .when(memberService)
                .isOwner(groupPath, ADMIN);

        doReturn(true)
                .when(memberService)
                .isCurrentUserAdmin();

        GetMembersResult getMembersResult =
                groupingsTestConfiguration
                        .getMembersResultsSuccessTestData()
                        .getMembersResults()
                        .get(2);

        assertNotNull(getMembersResult);

        doReturn(getMembersResult)
                .when(grouperService)
                .getAllMembers(
                        ADMIN,
                        groupPath + GroupType.OWNERS.value());

        // the number of owners should be correctly counted
        assertEquals(1,
                groupingAssignmentService.numberOfAllOwners(
                        ADMIN,
                        groupPath));

        // when the current user is not an admin, the service should still allow the call if the user is an owner.
        doReturn(false)
                .when(memberService)
                .isCurrentUserAdmin();

        assertNotNull(
                groupingAssignmentService.numberOfAllOwners(
                        ADMIN,
                        groupPath));

        // when the user is neither admin nor owner, access should be denied.
        doReturn(false)
                .when(memberService)
                .isOwner(groupPath, ADMIN);

        assertThrows(
                AccessDeniedException.class,
                () -> groupingAssignmentService.numberOfAllOwners(
                        ADMIN,
                        groupPath));
    }

    @Test
    public void numberOfDirectOwnersTest() {

        // when the current user is an owner and admin, counting direct owners should succeed.
        doReturn(true)
                .when(memberService)
                .isOwner(groupPath, ADMIN);

        doReturn(true)
                .when(memberService)
                .isCurrentUserAdmin();

        GetMembersResult getMembersResult =
                groupingsTestConfiguration
                        .getMembersResultsSuccessTestData()
                        .getMembersResults()
                        .get(2);

        assertNotNull(getMembersResult);

        doReturn(getMembersResult)
                .when(grouperService)
                .getImmediateMembers(
                        ADMIN,
                        groupPath + GroupType.OWNERS.value());

        // the number of direct owners should be counted correctly
        assertEquals(1,
                groupingAssignmentService.numberOfDirectOwners(
                        ADMIN,
                        groupPath));

        // when the current user is not an admin, the service should still allow the call if the user is an owner.
        doReturn(false)
                .when(memberService)
                .isCurrentUserAdmin();

        assertNotNull(
                groupingAssignmentService.numberOfDirectOwners(
                        ADMIN,
                        groupPath));

        // when the user is neither admin nor owner, access should be denied.
        doReturn(false)
                .when(memberService)
                .isOwner(groupPath, ADMIN);

        assertThrows(
                AccessDeniedException.class,
                () -> groupingAssignmentService.numberOfDirectOwners(
                        ADMIN,
                        groupPath));
    }

    @Test
    public void numberOfImmediateOwnersTest() {

        // when the current user is an owner, counting immediate owners should succeed.
        doReturn(true)
                .when(memberService)
                .isOwner(groupPath, ADMIN);

        GetMembersResult getMembersResult =
                groupingsTestConfiguration
                        .getMembersResultsSuccessTestData()
                        .getMembersResults()
                        .get(2);

        assertNotNull(getMembersResult);

        doReturn(getMembersResult)
                .when(grouperService)
                .getImmediateMembers(
                        ADMIN,
                        groupPath + GroupType.OWNERS.value());

        assertEquals(2,
                groupingAssignmentService.numberOfImmediateOwners(
                        ADMIN,
                        groupPath));

        // when the user is no longer an owner, the service should deny access.
        doReturn(false)
                .when(memberService)
                .isOwner(groupPath, ADMIN);

        assertThrows(
                AccessDeniedException.class,
                () -> groupingAssignmentService.numberOfImmediateOwners(
                        ADMIN,
                        groupPath));
    }
    @Test
    public void getDuplicateOwnersTest() {

        // the current user is an owner or admin, so the service should allow duplicate-owner detection.
        doReturn(true)
                .when(memberService)
                .isOwner(groupPath, ADMIN);

        doReturn(true)
                .when(memberService)
                .isCurrentUserAdmin();

        // immediate owners include one direct owner and two owner-grouping entries.
        GetMembersResult immediateOwners = new GetMembersResult();
        WsGetMembersResult immediateOwnersWs = immediateOwners.getWsGetMembersResult();
        WsGroup group = new WsGroup();
        group.setName(groupPath + GroupType.OWNERS.value());
        immediateOwnersWs.setWsGroup(group);

        WsSubject directOwnerSubject = new WsSubject();
        directOwnerSubject.setId("direct-owner-uuid");
        directOwnerSubject.setIdentifierLookup("direct-owner");
        directOwnerSubject.setName("Direct Owner");
        directOwnerSubject.setAttributeValues(new String[] { "", "", "", "Direct", "Owner" });
        directOwnerSubject.setResultCode("SUCCESS");

        WsSubject ownerGroupingOne = new WsSubject();
        ownerGroupingOne.setId("owner-grouping-1");
        ownerGroupingOne.setIdentifierLookup("owner-grouping-1");
        ownerGroupingOne.setName("tmp:testiwta:testiwta-many");
        ownerGroupingOne.setAttributeValues(new String[] { "", "", "", "Owner", "Grouping" });
        ownerGroupingOne.setResultCode("SUCCESS");

        WsSubject ownerGroupingTwo = new WsSubject();
        ownerGroupingTwo.setId("owner-grouping-2");
        ownerGroupingTwo.setIdentifierLookup("owner-grouping-2");
        ownerGroupingTwo.setName("tmp:testiwta:testiwta-two");
        ownerGroupingTwo.setAttributeValues(new String[] { "", "", "", "Owner", "Grouping" });
        ownerGroupingTwo.setResultCode("SUCCESS");

        immediateOwnersWs.setWsSubjects(new WsSubject[] {
                directOwnerSubject,
                ownerGroupingOne,
                ownerGroupingTwo
        });

        doReturn(immediateOwners)
                .when(grouperService)
                .getImmediateMembers(
                        ADMIN,
                        groupPath + GroupType.OWNERS.value());

        // first owner-grouping contains the duplicate owner and one new owner.
        GetMembersResult ownerGroupingMembersOne = new GetMembersResult();
        WsGetMembersResult ownerGroupingWsOne = ownerGroupingMembersOne.getWsGetMembersResult();
        ownerGroupingWsOne.setWsGroup(group);

        WsSubject duplicateOwner = new WsSubject();
        duplicateOwner.setId("direct-owner-uuid");
        duplicateOwner.setIdentifierLookup("direct-owner");
        duplicateOwner.setName("Direct Owner");
        duplicateOwner.setAttributeValues(new String[] { "", "", "", "Direct", "Owner" });
        duplicateOwner.setResultCode("SUCCESS");

        WsSubject newOwner = new WsSubject();
        newOwner.setId("new-owner-uuid");
        newOwner.setIdentifierLookup("new-owner");
        newOwner.setName("New Owner");
        newOwner.setAttributeValues(new String[] { "", "", "", "New", "Owner" });
        newOwner.setResultCode("SUCCESS");

        ownerGroupingWsOne.setWsSubjects(new WsSubject[] { duplicateOwner, newOwner });

        // second owner-grouping contains the same duplicate owner again.
        GetMembersResult ownerGroupingMembersTwo = new GetMembersResult();
        WsGetMembersResult ownerGroupingWsTwo = ownerGroupingMembersTwo.getWsGetMembersResult();
        ownerGroupingWsTwo.setWsGroup(group);

        WsSubject duplicateOwnerAgain = new WsSubject();
        duplicateOwnerAgain.setId("direct-owner-uuid");
        duplicateOwnerAgain.setIdentifierLookup("direct-owner");
        duplicateOwnerAgain.setName("Direct Owner");
        duplicateOwnerAgain.setAttributeValues(new String[] { "", "", "", "Direct", "Owner" });
        duplicateOwnerAgain.setResultCode("SUCCESS");

        ownerGroupingWsTwo.setWsSubjects(new WsSubject[] { duplicateOwnerAgain });

        doReturn(ownerGroupingMembersOne)
                .when(grouperService)
                .getMembersResult(
                        ADMIN,
                        "tmp:testiwta:testiwta-many");

        doReturn(ownerGroupingMembersTwo)
                .when(grouperService)
                .getMembersResult(
                        ADMIN,
                        "tmp:testiwta:testiwta-two");

        // the service should detect the duplicate owner from the first owner-grouping
        // and then add a second path when the same duplicate appears again.
        Map<String, OwnerResult> duplicates = groupingAssignmentService.getDuplicateOwners(
                ADMIN,
                groupPath);

        assertEquals(1, duplicates.size());
        assertTrue(duplicates.containsKey("direct-owner-uuid"));
        assertEquals("Direct Owner", duplicates.get("direct-owner-uuid").getName());
        assertEquals(2, duplicates.get("direct-owner-uuid").getPaths().size());
        assertTrue(duplicates.get("direct-owner-uuid").getPaths().contains("tmp:testiwta:testiwta-many"));
        assertTrue(duplicates.get("direct-owner-uuid").getPaths().contains("tmp:testiwta:testiwta-two"));

        // if the current user is no longer an admin, the service should still continue.
        doReturn(false)
                .when(memberService)
                .isCurrentUserAdmin();

        assertNotNull(
                groupingAssignmentService.getDuplicateOwners(
                        ADMIN,
                        groupPath));

        // if the user is no longer an owner, access should be denied.
        doReturn(false)
                .when(memberService)
                .isOwner(groupPath, ADMIN);

        assertThrows(
                AccessDeniedException.class,
                () -> groupingAssignmentService.getDuplicateOwners(
                        ADMIN,
                        groupPath));
    }
}